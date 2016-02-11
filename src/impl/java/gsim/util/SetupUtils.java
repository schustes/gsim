package gsim.util;

//import gsim.sim.engine.remote.ServerRegistryInitializer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

public class SetupUtils {

    private static final String DBPROPS_FILE = "conf/default-db.properties";// "c:/projects/phd/software/dev-jonas/gsim/conf/default-db.properties";

    private static Logger logger = Logger.getLogger(SetupUtils.class);

    @SuppressWarnings("unused")
    private final static String POLICY_PARALLEL = "parallel";

    private final static String POLICY_SERIAL = "serial";

    private static final String PROPS_FILE = "../../conf/server.properties";// "c:/projects/phd/software/dev-jonas/gsim/conf/server.properties";//"../../conf/server.properties";

    public SetupUtils() {
        super();
    }

    /*
     * public static String getDataSourceString() {
     * 
     * try { InputStream s = new FileInputStream(PROPS_FILE); Properties props = new Properties(); props.load(s);
     * 
     * String source = props.getProperty("sim.datasource", "java:gsim-ds"); return source; } catch (Exception e) { e.printStackTrace(); }
     * 
     * return null; }
     */
    @SuppressWarnings("unchecked")
    public static Hashtable<String, String> createClientContextEnvironment() {

        Hashtable<String, String> env = new Hashtable<String, String>();

        // env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "localhost:1100");
        // env.put(Context.URL_PKG_PREFIXES,
        // "org.jboss.naming:org.jnp.interfaces:org.jboss.invocation.jnp.interfaces");

        InputStream s = null;
        try {
            s = new FileInputStream(PROPS_FILE);
        } catch (Exception e) {
        }

        try {

            Properties props = new Properties();
            if (s != null) {
                props.load(s);
            }

            String jndiPort = props.getProperty("cluster.jndi.port", "1099");
            String jndiPrURL = props.getProperty("client.jndi.provider", "localhost");
            String pre = props.getProperty("cluster.jndi.url_pkg_prefixes", null);
            String provider = props.getProperty("cluster.jndi.initial_factory", "org.jnp.interfaces.NamingContextFactory");

            //
            if (jndiPrURL.equals("localhost")) {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                jndiPrURL = addr.getHostAddress();
            }
            //

            env.put(Context.INITIAL_CONTEXT_FACTORY, provider);
            env.put(Context.PROVIDER_URL, jndiPrURL + ":" + jndiPort); // cluster-port
            env.put("jnp.timeout", "60000");
            env.put("jnp.sotimeout", "120000");
            env.put("jnp.maxRetries", "10");

            if (pre != null) {
                env.put(Context.URL_PKG_PREFIXES, pre);
            }

            try {
                s.close();
            } catch (Exception e) {
            }

            return env;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * This is the context used by the gsim engine to initialise most internal services. Basically the JBoss default HAJNDI configuration.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static javax.naming.Context createClusterContext() {

        try {
            Hashtable<String, String> env = new Hashtable<String, String>();

            String jndiPort = "1100";

            try {
                InputStream s = new FileInputStream(PROPS_FILE);

                Properties props = new Properties();
                props.load(s);

                jndiPort = props.getProperty("cluster.jndi.port", "1100");
                String jndiPrURL = props.getProperty("cluster.jndi.provider", "localhost");

                //
                if (jndiPrURL.equals("localhost")) {
                    java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                    jndiPrURL = addr.getHostAddress();
                }
                //

                env.put(Context.PROVIDER_URL, jndiPrURL + ":" + jndiPort); // cluster-port
                String pre = props.getProperty("cluster.jndi.url_pkg_prefixes", null);
                if (pre != null) {
                    env.put(Context.URL_PKG_PREFIXES, pre);
                }
                String provider = props.getProperty("cluster.jndi.initial_factory", null);
                env.put("jnp.timeout", "60000");
                env.put("jnp.sotimeout", "120000");
                env.put("jnp.maxRetries", "10");
                env.put(Context.INITIAL_CONTEXT_FACTORY, provider);

                s.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            Context ctx = new InitialContext(env);

            return ctx;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static javax.naming.Context createDedicatedCacheSaverJMSContext() {

        Hashtable<String, String> env = new Hashtable<String, String>();

        try {

            InputStream s = new FileInputStream(PROPS_FILE);

            Properties props = new Properties();
            props.load(s);

            String jndiPort = props.getProperty("cache.jndi.port", "1099");
            String jndiPrURL = props.getProperty("cache.jndi.provider", "localhost");
            String pre = props.getProperty("cluster.jndi.url_pkg_prefixes", "org.jboss.naming:org.jnp.interfaces");
            String provider = props.getProperty("cluster.jndi.initial_factory", "org.jnp.interfaces.NamingContextFactory");

            //
            if (jndiPrURL.equals("localhost")) {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                jndiPrURL = addr.getHostAddress();
            }
            //

            env.put(Context.PROVIDER_URL, jndiPrURL + ":" + jndiPort); // cluster-port
            env.put("jnp.timeout", "60000");
            env.put("jnp.sotimeout", "120000");
            env.put("jnp.maxRetries", "10");
            env.put(Context.URL_PKG_PREFIXES, pre);
            env.put(Context.INITIAL_CONTEXT_FACTORY, provider);

            Context ctx = new InitialContext(env);

            s.close();

            return ctx;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;// very well..
        }
    }

    /*
     * public static boolean prefetch() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("model.prefetch", "false"); logger.debug(p); boolean b = Boolean.parseBoolean(p);
     * 
     * return b;
     * 
     * } catch (Exception e) { return false; }
     * 
     * }
     * 
     * public static int getPersistentCacheThreshold() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("model.persistentCacheThreshold", "1000"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 1000; }
     * 
     * }
     * 
     * public static int getMaxCacheSize() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("model.maxCacheSize", "1000"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 1000; }
     * 
     * }
     * 
     * public static int getLocalJMSPoolSize() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.localJMSConnections", "10"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 10; }
     * 
     * }
     * 
     * public static double getMessagingFraction() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.agentMessagingFraction", "1"); return Double.parseDouble(p);
     * 
     * } catch (Exception e) { return 10; }
     * 
     * }
     * 
     * public static int getIncompleteTimeout() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.incompleteTimout", "1"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 10; }
     * 
     * }
     * 
     * public static int getDeadlockTimeout() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.deadlockTimout", "10"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 10; }
     * 
     * }
     * 
     * public static int getNotificationOption() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("message.notification", "NEVER"); if (p.equals("NEVER")) return -1; if (p.equals("ALWAYS")) return -2; return
     * Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 10; }
     * 
     * }
     * 
     * public static String getTopicConnectionFactoryName() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("messaging.TopicConnectionFactory", null);
     * 
     * return p;
     * 
     * } catch (Exception e) { return null; }
     * 
     * }
     * 
     * public static String getLocalTopicConnectionFactoryName() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("messaging.LocalTopicConnectionFactory", null);
     * 
     * return p;
     * 
     * } catch (Exception e) { return null; }
     * 
     * }
     * 
     * public static String getQueueConnectionFactoryName() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("messaging.QueueConnectionFactory", null);
     * 
     * return p;
     * 
     * } catch (Exception e) { return null; }
     * 
     * }
     * 
     * public static int getPartitionSize() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("sim.partition", "10"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * }
     * 
     * public static int getClockTime() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("clock.interval", "10"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * }
     * 
     * public static int getMaxParallelSimulations() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("sim.parallel.lower", "30"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * }
     * 
     * public static int getCriticalParallelSimulations() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("sim.parallel.upper", "31"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * }
     * 
     * public static int getConnectionCount() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.connections", "10"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * } public static int getSessionCount() { try { InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("resource.sessions", "100"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 3; }
     * 
     * }
     * 
     * public static int getEnvironmentDelegatesCount() { try {
     * 
     * InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String p = props.getProperty("env.delegateCount", "1"); return Integer.parseInt(p);
     * 
     * } catch (Exception e) { return 1; }
     * 
     * }
     */
    /**
     * 
     * Creates default properties to initialise a model from a - possibly remote - client. This means, an explicit jndi-provider is expected from
     * server.properties, because it cannot be assumed that autodiscovery will work.
     * 
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static GSimProperties createDefaultClientModelProperties() {

        try {
            InputStream s = null;

            try {
                s = new FileInputStream(DBPROPS_FILE);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            GSimProperties gprops = new GSimProperties();

            Properties props = new Properties();
            if (s != null) {
                props.load(s);
            }

            // only for local mode - must usually always be configured by implementor
            gprops.put(GSimProperties.DB_HOST, props.getProperty("db.host", "socnt08"));
            gprops.put(GSimProperties.DB_NAME, props.getProperty("db.name", "test"));
            gprops.put(GSimProperties.DB_USER, props.getProperty("db.user", "gsim"));
            gprops.put(GSimProperties.DB_PASSWORD, props.getProperty("db.password", "gsim1"));

            gprops.put(GSimProperties.DB_MANAGER, props.getProperty("db.connectionpool", "java:gsim-ds"));

            // also specified by implementor, but not in the model, but on the server
            // props.put(GSimProperties.DB_CONNECTIONPOOL, "java:gsim-ds");

            gprops.put(GSimProperties.JNDICONTEXT, createClientContextEnvironment());

            // don't need to be specified if don't care
            gprops.put(GSimProperties.ACTION_POLICY, POLICY_SERIAL);
            gprops.put(GSimProperties.PARTITION_SIZE, "100");
            gprops.put(GSimProperties.DB_CLEAR, new Boolean(true));

            gprops.put(GSimProperties.MAX_REWARD, String.valueOf(1));

            gprops.put(GSimProperties.SIM_ID, new String("parameter-setting-1"));

            s.close();

            return gprops;

        } catch (Exception e) {
            e.printStackTrace();
            return new GSimProperties();
        }

    }

    /**
     * Creates default properties for models executed from WITHIN the cluster, for example when started via a batch job. In this case, autodiscovery
     * is assumed to work, so actually no further modifications should be necessary, just use this context within any cluster where gsim is running.
     * 
     * @param dataSourceName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static GSimProperties createDefaultServerModelProperties(String dataSourceName) {

        try {
            InputStream s = null;

            try {
                s = new FileInputStream(DBPROPS_FILE);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            GSimProperties gprops = new GSimProperties();

            Properties props = new Properties();
            if (s != null) {
                props.load(s);
            }

            // only for local mode - must usually always be configured by implementor
            gprops.put(GSimProperties.DB_HOST, props.getProperty("db.host", "socnt08"));
            gprops.put(GSimProperties.DB_NAME, props.getProperty("db.name", "test"));
            gprops.put(GSimProperties.DB_USER, props.getProperty("db.user", "gsim"));
            gprops.put(GSimProperties.DB_PASSWORD, props.getProperty("db.password", "gsim1"));

            gprops.put(GSimProperties.DB_MANAGER, props.getProperty("db.connectionpool", "java:gsim-ds"));

            gprops.put(GSimProperties.DB_CONNECTIONPOOL, dataSourceName);

            // also specified by implementor, but not in the model, but on the server
            // props.put(GSimProperties.DB_CONNECTIONPOOL, "java:gsim-ds");

            gprops.put(GSimProperties.JNDICONTEXT, createClientContextEnvironment());

            // don't need to be specified if don't care
            gprops.put(GSimProperties.ACTION_POLICY, POLICY_SERIAL);
            gprops.put(GSimProperties.PARTITION_SIZE, "100");
            gprops.put(GSimProperties.DB_CLEAR, new Boolean(false));

            gprops.put(GSimProperties.MAX_REWARD, String.valueOf(1));

            gprops.put(GSimProperties.SIM_ID, new String("parameter-setting-1"));

            try {
                s.close();
            } catch (Exception e) {
            }
            ;

            return gprops;

        } catch (Exception e) {
            e.printStackTrace();
            return new GSimProperties();
        }

    }

    @SuppressWarnings("unchecked")
    public static javax.naming.Context createLocalJMSContext() {

        Hashtable<String, String> env = new Hashtable<String, String>();

        try {

            InputStream s = new FileInputStream(PROPS_FILE);

            Properties props = new Properties();
            props.load(s);

            String jndiPort = props.getProperty("local.jndi.port", "1099");
            String jndiPrURL = props.getProperty("local.jndi.provider", "localhost");
            String pre = props.getProperty("cluster.jndi.url_pkg_prefixes", "org.jboss.naming:org.jnp.interfaces");
            String provider = props.getProperty("cluster.jndi.initial_factory", "org.jnp.interfaces.NamingContextFactory");

            //
            if (jndiPrURL.equals("localhost")) {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                jndiPrURL = addr.getHostAddress();
            }
            //

            env.put(Context.PROVIDER_URL, jndiPrURL + ":" + jndiPort); // cluster-port
            env.put("jnp.timeout", "60000");
            env.put("jnp.sotimeout", "120000");
            env.put("jnp.maxRetries", "10");
            env.put(Context.URL_PKG_PREFIXES, pre);
            env.put(Context.INITIAL_CONTEXT_FACTORY, provider);

            Context ctx = new InitialContext(env);

            s.close();

            return ctx;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;// very well..
        }
    }

    /*
     * public static javax.naming.Context createLocalJMSContext() {
     * 
     * Hashtable env = new Hashtable();
     * 
     * try {
     * 
     * InputStream s = new FileInputStream(PROPS_FILE);
     * 
     * Properties props = new Properties(); props.load(s);
     * 
     * String jndiPort = props.getProperty("jndi.port", "1099"); String jndiPrURL = props.getProperty("jndi.provider", "localhost"); String pre =
     * props.getProperty("jndi.url_pkg_prefixes", "org.jboss.naming:org.jnp.interfaces"); String provider = props.getProperty("jndi.initial_factory",
     * "org.jnp.interfaces.NamingContextFactory");
     * 
     * env.put(Context.PROVIDER_URL, jndiPrURL + ":" + jndiPort); // cluster-port env.put(Context.URL_PKG_PREFIXES, pre);
     * env.put(Context.INITIAL_CONTEXT_FACTORY, provider);
     * 
     * Context ctx = new InitialContext(env);
     * 
     * return ctx; } catch (Exception e) { e.printStackTrace(); System.exit(0); return null;// very well.. } }
     */
}
