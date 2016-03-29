package de.s2.gsim.sim.engine.common;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

public class SimpleClassLoader extends ClassLoader implements Serializable {

    private static Logger logger = Logger.getLogger(SimpleClassLoader.class);

    private static final long serialVersionUID = 1L;

    private Hashtable classes = new Hashtable();

    private String[] files = new String[0];

    public SimpleClassLoader(String pathToJarList) {
        files = pathToJarList.split(",");
    }

    public SimpleClassLoader(String[] pathToJarList) {
        files = pathToJarList;
    }

    /**
     * This is a simple version for external clients since they will always want the class resolved before it is returned to them.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return (this.loadClass(className, true));
    }

    /**
     * This is the required version of loadClass which is called both from loadClass above and from the internal function FindClassFromClass.
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        Class result;
        byte classData[];

        // logger.debug(" >>>>>> Load class : "+className);

        /* Check our local cache of classes */
        result = (Class) classes.get(className);
        if (result != null) {
            return result;
        }

        /* Check with the primordial class loader */
        try {
            result = super.findSystemClass(className);
            // logger.debug(" >>>>>> returning system class (in CLASSPATH).");
            return result;
        } catch (ClassNotFoundException e) {
            // logger.debug(" >>>>>> Not a system class.");
        }

        /* Try to load it from our repository */
        classData = getClassImplFromDataBase(className);
        if (classData == null) {
            throw new ClassNotFoundException();
        }

        /* Define it (parse the class file) */
        result = this.defineClass(classData, 0, classData.length);
        if (result == null) {
            throw new ClassFormatError();
        }

        if (resolveIt) {
            resolveClass(result);
        }

        classes.put(className, result);
        // logger.debug(" >>>>>> Returning newly loaded class.");
        return result;
    }

    public void test() {
        try {
            @SuppressWarnings("unused")
            Class c = Class.forName("gsim.sim.common.Executable", false, this);

            // logger.debug(c.newInstance().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This sample function for reading class implementations reads them from the local file system
     */
    private byte getClassImplFromDataBase(String className)[] {
        // logger.debug(" >>>>>> Fetching the implementation of "+className);
        byte result[] = null;
        try {
            for (String file : files) {
                JarFile jar = new JarFile(file);

                String str = className.replace(".", "/") + ".class";
                logger.debug(">>>>>>>>>>>>>>>>" + str);
                ZipEntry e = jar.getEntry(str);

                if (e != null) {
                    InputStream in = jar.getInputStream(e);
                    BufferedInputStream bi = new BufferedInputStream(in);
                    result = new byte[bi.available()];
                    bi.read(result);
                }
            }
            return result;
        } catch (Exception e) {
            /*
             * If we caught an exception, either the class wasnt found or it was unreadable by our process.
             */
            return null;
        }
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        String list = new String("C:/projects/newties/development/current/trunk/target/dist/framework/framework-api/framework-api.jar,"
                + "C:/projects/newties/development/current/trunk/target/dist/framework/framework-environmentmanager/framework-environmentmanager.jar,"
                + "C:/projects/newties/development/soap_app/lib/JTS-1.4.jar");
        new SimpleClassLoader(new String[] { "c:/projects/phd/software/dev-jonas/models/networks/dist/net-sim.jar" }).test();
    }
}
