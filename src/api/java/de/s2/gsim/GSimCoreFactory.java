package de.s2.gsim;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

/**
 * The GSimCoreFactory creates a {@link GSimCore} for creating and managing simulations. A default factory must be provided by any implementation. A
 * factory must be annotated with a {@link CoreFactory} annotation.
 *
 * Service providers may implement their own factories and implementations of the API (e.g. a clustered instead of a standalone version). The name
 * must correspond to an existing {@link CoreFactory} annotation.
 *
 * @author stephan
 *
 */
public abstract class GSimCoreFactory {

    /**
     * Abstract method that must be implemented by an CoreFactory implementation.
     *
     * @return the {@link GSimCore} created by the factory
     */
    public abstract GSimCore createCore();

    /**
     * Finds the first default factory in the classpath. Throws a {@link GSimException} if no factory was found.
     *
     * @return the default factory
     */
    public static GSimCoreFactory defaultFactory() {

        GSimCoreFactory factory = ClassSearchUtils.find(null, GSimCoreFactory.class.getClassLoader());

        if (factory == null) {
            throw new GSimException(String.format("No default factory found in classpath!"));
        }

        return factory;
    }

    /**
     * Finds the first factory with the given name in the classpath. Throws a {@link GSimException} if no factory was found.
     *
     * @param name the name of the factory
     * @return the factory
     */
    public static GSimCoreFactory customFactory(String name) {

        GSimCoreFactory factory = ClassSearchUtils.find(name, GSimCoreFactory.class.getClassLoader());

        if (factory == null) {
            throw new GSimException(String.format("No factory %s found in classpath!", name));
        }

        return factory;
    }

    private static final Logger LOG = Logger.getLogger(ClassSearchUtils.class);

    public static void main(String... args) {
        ClassSearchUtils utils = new ClassSearchUtils();
        System.setProperty("java.class.path", "/home/stephan/projects/discrimination/discrimination-scenario/build/libs/discrimination-scenario-1.0.jar");
        utils.find(null, GSimCoreFactory.class.getClassLoader());
    }

    private static class ClassSearchUtils {

        private static GSimCoreFactory find(String factoryName, ClassLoader classloader) {
            String classpath = System.getProperty("java.class.path");

            try {
                Method method = classloader.getClass().getMethod("getClassPath", (Class<?>) null);
                if (method != null) {
                    classpath = (String) method.invoke(classloader, (Object) null);
                }
            } catch (Exception e) {
                LOG.trace("Igoring exception", e);
            }
            if (classpath == null) {
                classpath = System.getProperty("java.class.path");
            }

            StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
            String token;
            File dir;
            String name;

            GSimCoreFactory foundFactory = null;

            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                dir = new File(token);
                if (dir.isDirectory()) {
                    foundFactory = lookInDirectory(factoryName, "", dir, classloader);
                    if (foundFactory != null) {
                        return foundFactory;
                    }
                }
                if (dir.isFile()) {
                    name = dir.getName().toLowerCase();
                    if (name.endsWith(".zip") || name.endsWith(".jar")) {
                        foundFactory = lookInArchiveRek(factoryName, dir, classloader);
                        if (foundFactory != null) {
                            return foundFactory;
                        }
                    } 
                }
            }
            return foundFactory;
        }

        private static GSimCoreFactory lookInArchiveRek(String factoryName, File archive, ClassLoader classloader) {

            try {
                URI uri = new URI("jar:file:" + archive.getAbsolutePath());

                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                Path myPath = fileSystem.getRootDirectories().iterator().next();
                Stream<Path> walk = Files.walk(myPath, 10);

                for (Iterator<Path> it = walk.iterator(); it.hasNext();){
                    Path p = it.next();
                    if (p.toString().endsWith(".jar")) {
                        GSimCoreFactory f = lookInArchive0(factoryName, uri, p, classloader);
                        if (f != null) {
                            return f;
                        }
                    } else if (p.toFile().isDirectory()) {//geht nicht so
                        lookInArchiveRek(factoryName, archive, classloader);
                    } else if (p.toString().endsWith(".class")) {
                        GSimCoreFactory factory =  loadClassIfFactoryAnnotationPresent(factoryName, classloader, p.toString().replace('/', '.'));
                        if (factory != null) {
                            return factory;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Search archive files for required resource.
         *
         * @param archive Jar or zip to be searched for classes or other resources.
         */
        @SuppressWarnings("unchecked")
        private static GSimCoreFactory lookInArchive0(String factoryName, URI base, Path archive, ClassLoader classloader) {

            JarInputStream jarFile = null;
            try {
                URL url = new URL(base.toString()+"!"+archive.toString());
                JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
                jarFile  = new JarInputStream(jarConnection.getInputStream());
                JarEntry entry;
                String entryName;
                while ((entry = jarFile.getNextJarEntry()) != null) {
                    entryName = entry.getName();

                    if (entryName.toLowerCase().endsWith(".class")) {
                        try {
                            entryName = entryName.substring(0, entryName.length() - 6);
                            entryName = entryName.replace('/', '.');

                            GSimCoreFactory clazz = loadClassIfFactoryAnnotationPresent(factoryName, classloader, entryName);
                            if (clazz != null) return clazz;
                        } catch (Throwable e) {
                            LOG.warn("Unable to load resource [" + entryName + "] from file [" + archive.toString() + "].", e);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.debug("Non fatal error. Unable to read jar item.", e);
                return null;
            }

            try {
                jarFile.close();
            } catch (IOException e) {
                LOG.debug("Non fatal error. Unable to close jar.", e);
            }
            return null;
        }

        @Nullable
        private static GSimCoreFactory loadClassIfFactoryAnnotationPresent(String factoryName, ClassLoader classloader, String entryName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            try {
                Class<?> clazz = classloader.loadClass(entryName);
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    CoreFactory ann = clazz.getAnnotation(CoreFactory.class);
                    if (ann.isDefault() && factoryName == null || ann.name().equals(factoryName)) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }

            } catch (NoClassDefFoundError e) {
                LOG.debug("Ignore Error");
            }
            return null;
        }

        /**
         * @param name Name of to parent directories in java class notation (dot separator)
         * @param dir Directory to be searched for classes.
         */

        private static GSimCoreFactory lookInDirectory(String factoryName, String name, File dir, ClassLoader classloader) {
            LOG.debug("Looking in directory [" + dir.getName() + "].");
            File[] files = dir.listFiles();
            File file;
            String fileName;
            final int size = files.length;
            for (int i = 0; i < size; i++) {
                file = files[i];
                fileName = file.getName();
                if (file.isFile() && fileName.toLowerCase().endsWith(".class")) {
                    try {
                        fileName = fileName.substring(0, fileName.length() - 6);

                        String theName = name + fileName;

                        GSimCoreFactory factory =  loadClassIfFactoryAnnotationPresent(factoryName, classloader, theName);
                        if (factory != null) {
                            return factory;
                        }

                    } catch (Throwable e) {
                        LOG.trace("Class not found/defined , ignore", e);
                    }
                }
                if (file.isDirectory()) {
                    return lookInDirectory(factoryName, name + fileName + ".", file, classloader);
                }
            }

            return null;

        }
    }


}
