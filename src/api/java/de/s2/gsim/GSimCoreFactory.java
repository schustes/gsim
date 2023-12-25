package de.s2.gsim;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.util.*;
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

        GSimCoreFactory factory = null;
        try {
            factory = ClassSearchUtils.find(null, GSimCoreFactory.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        GSimCoreFactory factory = null;
        try {
            factory = ClassSearchUtils.find(name, GSimCoreFactory.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (factory == null) {
            throw new GSimException(String.format("No factory %s found in classpath!", name));
        }

        return factory;
    }

    private static List<String> getRootUrls () {
        List<String> result = new ArrayList<> ();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                for (URL u: urls) {
                    result.add(u.getFile());
                }
            }
            cl = cl.getParent();
        }
        return result;
    }

    private static final Logger LOG = Logger.getLogger(ClassSearchUtils.class);

    private static class ClassSearchUtils {

        private static Map<String, GSimCoreFactory> factories = new HashMap<>();

        private static GSimCoreFactory find(String factoryName, ClassLoader classloader) throws IOException, URISyntaxException {

            if (factories.containsKey(factoryName)) {
                return factories.get(factoryName);
            }

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

            List<String> urls = getRootUrls();
            //classpath = String.join(";", urls);

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
                        factories.put(factoryName, foundFactory);
                        return foundFactory;
                    }
                }
                if (dir.isFile()) {
                    name = dir.getName().toLowerCase();

                    URI uri = new URI("jar:" + dir.toURI());
                    FileSystem system = getFileSystem(uri);

                    if ( (name.endsWith(".zip") || name.endsWith(".jar")
                            && !token.contains("javax")  && !token.contains("org") && !token.contains("common") && !token.contains("google") && !token.contains("jre") && !token.contains("jdk") && !token.contains("spring") && !token.contains("apache"))) {
                        foundFactory = lookInArchiveRek(factoryName, uri, system, classloader);
                        if (foundFactory != null) {
                            factories.put(factoryName, foundFactory);
                            return foundFactory;
                        }
                    }
                }
            }
            if (foundFactory != null) {
                factories.put(factoryName, foundFactory);
            }
            return foundFactory;
        }

        private static FileSystem getFileSystem(URI uri) throws IOException {
            try {
                return FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                return FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            }
        }

        public static void main(String... args) throws Exception {
            //System.setProperty("java.class.path", "/home/stephan/projects/discrimination/discrimination-simulator/build/libs/discrimination-simulator-1.0.jar");
            System.setProperty("java.class.path", "file:///home/stephan/projects/gsim/build/libs/gsim-2.0-SNAPSHOT.jar");
            ClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("file:///home/stephan/projects/gsim/build/libs/gsim-2.0-SNAPSHOT.jar")});
            GSimCoreFactory f = find(null, urlClassLoader);
            System.out.println(f);
            Class<?> beanClass = urlClassLoader.loadClass("de.s2.gsim.api.impl.StandaloneFactory");
            System.out.println(beanClass);
        }

        private static GSimCoreFactory lookInArchiveRek(String factoryName, URI uri, FileSystem fileSystem, ClassLoader classloader) {

            try {

                Path myPath = fileSystem.getRootDirectories().iterator().next();
                Stream<Path> walk = Files.walk(myPath, 10);

                for (Iterator<Path> it = walk.iterator(); it.hasNext();){
                    Path p = it.next();
                    if (p.toString().endsWith(".jar")
                            && !p.toString().contains("google")
                            && !p.toString().contains("jre")
                            && !p.toString().contains("jdk")
                            && !p.toString().contains("spring")
                            && !p.toString().contains("org")
                            && !p.toString().contains("jackson")
                            && !p.toString().contains("swagger")
                            && !p.toString().contains("sun")
                            && !p.toString().contains("commons")
                            && !p.toString().contains("rabbitmq")
                            && !p.toString().contains("h2database")
                            && !p.toString().contains("javax")
                            && !p.toString().contains("junit")
                            && !p.toString().contains("glassfish")
                            && !p.toString().contains("jboss")
                            && !p.toString().contains("apache") ) {
                        GSimCoreFactory f = lookInArchive0(factoryName, uri, p, classloader);
                        if (f != null) {
                            return f;
                        }
                    } else if (!p.toString().contains("google")
                            && !p.toString().contains("oracle")
                            && !p.toString().contains("javax")
                            && !p.toString().contains("spring")
                            && !p.toString().contains("apache")
                            && !p.toString().contains("sun")
                            && !p.toString().contains("jackson")
                            && !p.toString().contains("swagger")
                            && !p.toString().contains("sun")
                            && !p.toString().contains("commons")
                            && !p.toString().contains("rabbitmq")
                            && !p.toString().contains("h2database")
                            && !p.toString().contains("net.sf")
                            && !p.toString().contains("org")
                            && !p.toString().contains("junit")
                            && !p.toString().contains("glassfish")
                            && p.toString().endsWith(".class")) {
                        String entry = pathToClassName(p.toString());
                        GSimCoreFactory factory =  loadClassIfFactoryAnnotationPresent(factoryName, classloader, entry);
                        if (factory != null) {
                            return factory;
                        }
                    } else if (!p.toString().contains(".") && !p.toString().contains("/")) {
                        URI nextUri = new URI("jar:file:" + p.toString());
                        FileSystem nextSystem = FileSystems.newFileSystem(nextUri, Collections.<String, Object>emptyMap());
                        lookInArchiveRek(factoryName, nextUri, nextSystem, classloader);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @NotNull
        private static String pathToClassName(String entry) {
            entry = entry.substring(0, entry.length() - 6);
            if (entry.startsWith("/")) {
                entry = entry.substring(1);
            }
            entry = entry.replace('/', '.');
            return entry;
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
                            entryName = pathToClassName(entryName);

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
            if (entryName.contains("StandaloneFactory")) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>" + entryName + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>><");
            }
            try {
                Class<?> clazz = classloader.loadClass(entryName);
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    CoreFactory ann = clazz.getAnnotation(CoreFactory.class);
                    if (ann.isDefault() && factoryName == null || ann.name().equals(factoryName)) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }

            } catch (NoClassDefFoundError | Exception e) {
                if (entryName.contains("StandaloneFactory")) {
                    System.out.println(">>>PROBLEM>>>" + e.getMessage() + " [" + e.getClass().getSimpleName() + "]");
                    LOG.debug("Ignore Error");
                }
            } catch (Error e) {
               // System.out.println(">>>>"  +  entryName);
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
                    GSimCoreFactory inDepth = lookInDirectory(factoryName, name + fileName + ".", file, classloader);
                    if (inDepth != null) {
                        return inDepth;
                    }
                }
            }

            return null;

        }
    }


}
