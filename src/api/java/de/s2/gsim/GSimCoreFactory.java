package de.s2.gsim;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
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
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				dir = new File(token);
				if (dir.isDirectory()) {
					return lookInDirectory(factoryName, "", dir, classloader);
				}
				if (dir.isFile()) {
					name = dir.getName().toLowerCase();
					if (name.endsWith(".zip") || name.endsWith(".jar")) {
						return lookInArchive(factoryName, dir, classloader);
					}
				}
			}
			return null;
		}

		/**
		 * Search archive files for required resource.
		 * 
		 * @param archive Jar or zip to be searched for classes or other resources.
		 */
		@SuppressWarnings("unchecked")
		private static GSimCoreFactory lookInArchive(String factoryName, File archive, ClassLoader classloader) {

			JarFile jarFile = null;
			try {
				jarFile = new JarFile(archive);
			} catch (IOException e) {
				LOG.debug("Non fatal error. Unable to read jar item.", e);
				return null;
			}
			Enumeration<?> entries = jarFile.entries();
			JarEntry entry;
			String entryName;
			while (entries.hasMoreElements()) {
				entry = (JarEntry) entries.nextElement();
				entryName = entry.getName();
				if (entryName.toLowerCase().endsWith(".class")) {
					try {
						entryName = entryName.substring(0, entryName.length() - 6);
						entryName = entryName.replace('/', '.');

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

						try {

							Class<?> clazz = classloader.loadClass(theName);

							if (clazz.isAnnotationPresent(CoreFactory.class)) {
								CoreFactory ann = clazz.getAnnotation(CoreFactory.class);
								if (ann.isDefault() && factoryName == null || ann.name().equals(factoryName)) {
									return (GSimCoreFactory) clazz.newInstance();
								}
							}

						} catch (NoClassDefFoundError e) {
							LOG.debug("Ignore Error");
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
		
		// private void lookInDirectory(String name, File dir) {
		// LOG.debug("Looking in directory [" + dir.getName() + "].");
		// File[] files = dir.listFiles();
		// File file;
		// String fileName;
		// final int size = files.length;
		// for (int i = 0; i < size; i++) {
		// file = files[i];
		// fileName = file.getName();
		// if (file.isFile() && fileName.toLowerCase().endsWith(extension)) {
		// try {
		// if (extension.equalsIgnoreCase(".class")) {
		// fileName = fileName.substring(0, fileName.length() - 6);
		//
		// String theName = name + fileName;
		// if ((name + fileName).endsWith(postfix)) {
		// LOG.debug("Found class: [" + theName + "].");
		// try {
		// list.add(classloader.loadClass(theName));
		// } catch (NoClassDefFoundError e) {
		// LOG.debug("Ignore Error");
		// }
		//
		// }
		//
		// } else {
		// list.add(classloader.getResource(name.replace('.', File.separatorChar) + fileName));
		// }
		// } catch (ClassNotFoundException | NoClassDefFoundError e) {
		// LOG.trace("Class not found/defined , ignore", e);
		// }
		// }
		// if (file.isDirectory()) {
		// lookInDirectory(name + fileName + ".", file);
		// }
		// }
	}


}
