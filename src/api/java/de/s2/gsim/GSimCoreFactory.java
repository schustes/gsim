package de.s2.gsim;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

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

        try {
            List<Class<?>> classes = ClassSearchUtils.searchClassPath();
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    if (clazz.getAnnotation(CoreFactory.class).isDefault()) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }
            }
            throw new GSimException("No default factory found in classpath!");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GSimException(e);
        }

    }

    /**
     * Finds the first factory with the given name in the classpath. Throws a {@link GSimException} if no factory was found.
     * 
     * @param name the name of the factory
     * @return the factory
     */
    public static GSimCoreFactory customFactory(String name) {

        try {
            List<Class<?>> classes = ClassSearchUtils.searchClassPath();
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(CoreFactory.class)) {
                    CoreFactory ann = clazz.getAnnotation(CoreFactory.class);
                    if (ann.isDefault() && ann.name().equals(name)) {
                        return (GSimCoreFactory) clazz.newInstance();
                    }
                }
            }
            throw new GSimException(String.format("No factory %s found in classpath!", name));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GSimException(e);
        }

    }

    private static class ClassSearchUtils {

        public static String postfix = "";

        private static final Logger LOG = Logger.getLogger(ClassSearchUtils.class);

        /**
         * Classloader to be used to obtain resources from file system.
         */
        private ClassLoader classloader;

        /**
         * Extension of the resource to be found in the classpath.
         */
        private String extension;

        /**
         * List of the resource found in the classpath.
         */
        @SuppressWarnings("rawtypes")
        private ArrayList list;

        /**
         * Search for the resource with the extension in the classpath.
         * 
         * @param extension Mandatory extension of the resource. If all resources are required extension should be empty string. Null extension is not
         * allowed and will cause method to fail.
         * @return List of all resources with specified extension.
         */
        @SuppressWarnings("unchecked")
        private List<Class<?>> find(String extension) {
            this.extension = extension;
            list = new ArrayList<>();
            classloader = this.getClass().getClassLoader();
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
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                dir = new File(token);
                if (dir.isDirectory()) {
                    lookInDirectory("", dir);
                }
                if (dir.isFile()) {
                    name = dir.getName().toLowerCase();
                    if (name.endsWith(".zip") || name.endsWith(".jar")) {
                        lookInArchive(dir);
                    }
                }
            }
            return list;
        }

        /**
         * Search archive files for required resource.
         * 
         * @param archive Jar or zip to be searched for classes or other resources.
         */
        @SuppressWarnings("unchecked")
        private void lookInArchive(File archive) {
            LOG.debug("Looking in archive [" + archive.getName() + "] for extension [" + extension + "].");
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(archive);
            } catch (IOException e) {
                LOG.debug("Non fatal error. Unable to read jar item.", e);
                return;
            }
            Enumeration<?> entries = jarFile.entries();
            JarEntry entry;
            String entryName;
            while (entries.hasMoreElements()) {
                entry = (JarEntry) entries.nextElement();
                entryName = entry.getName();
                if (entryName.toLowerCase().endsWith(extension)) {
                    try {
                        if (extension.equalsIgnoreCase(".class")) {
                            entryName = entryName.substring(0, entryName.length() - 6);
                            entryName = entryName.replace('/', '.');

                            LOG.debug("Found class: [" + entryName + "]. ");
                            try {
                                list.add(classloader.loadClass(entryName));
                            } catch (NoClassDefFoundError e) {
                                LOG.debug("Ignore Error");
                            }
                        } else {
                            list.add(classloader.getResource(entryName));
                            LOG.debug(
                                    "Found appropriate resource with name [" + entryName + "]. Resource instance:" + classloader.getResource(entryName));
                        }
					} catch (Throwable e) {
                        LOG.debug("Unable to load resource [" + entryName + "] form file [" + archive.getAbsolutePath() + "].", e);
                    }
                }
            }
            try {
                jarFile.close();
            } catch (IOException e) {
                LOG.debug("Non fatal error. Unable to close jar.", e);
            }
        }

        /**
         * @param name Name of to parent directories in java class notation (dot separator)
         * @param dir Directory to be searched for classes.
         */
        @SuppressWarnings("unchecked")
        private void lookInDirectory(String name, File dir) {
            LOG.debug("Looking in directory [" + dir.getName() + "].");
            File[] files = dir.listFiles();
            File file;
            String fileName;
            final int size = files.length;
            for (int i = 0; i < size; i++) {
                file = files[i];
                fileName = file.getName();
                if (file.isFile() && fileName.toLowerCase().endsWith(extension)) {
                    try {
                        if (extension.equalsIgnoreCase(".class")) {
                            fileName = fileName.substring(0, fileName.length() - 6);

                            String theName = name + fileName;
                            if ((name + fileName).endsWith(postfix)) {
                                LOG.debug("Found class: [" + theName + "].");
                                try {
                                    list.add(classloader.loadClass(theName));
                                } catch (NoClassDefFoundError e) {
                                    LOG.debug("Ignore Error");
                                }

                            }

                        } else {
                            list.add(classloader.getResource(name.replace('.', File.separatorChar) + fileName));
                        }
					} catch (Throwable e) {
                        LOG.trace("Class not found/defined , ignore", e);
                    }
                }
                if (file.isDirectory()) {
                    lookInDirectory(name + fileName + ".", file);
                }
            }

        }

        /**
         * Search for the resource with the extension in the classpath. Method self-instantiate factory for every call to ensure thread safety.
         * 
         * @param extension Mandatory extension of the resource. If all resources are required extension should be empty string. Null extension is not
         * allowed and will cause method to fail.
         * @return List of all resources with specified extension.
         */
        public static List<Class<?>> searchClassPath() {
            return searchClassPath(".class");
        }

        /**
         * Search for the resource with the extension in the classpath. Method self-instantiate factory for every call to ensure thread safety.
         * 
         * @param extension Mandatory extension of the resource. If all resources are required extension should be empty string. Null extension is not
         * allowed and will cause method to fail.
         * @return List of all resources with specified extension.
         */
        private static List<Class<?>> searchClassPath(String extension) {
            ClassSearchUtils factory = new ClassSearchUtils();
            return factory.find(extension);
        }
    }
    

}
