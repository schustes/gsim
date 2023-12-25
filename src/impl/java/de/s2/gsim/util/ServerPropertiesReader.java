package de.s2.gsim.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class ServerPropertiesReader {

    private static final String PROPS_FILE = "../../conf/server.properties";

    private Properties props;

    public ServerPropertiesReader() {
        try {

            props = new Properties();

            InputStream s = new FileInputStream(PROPS_FILE);
            props.load(s);

            s.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getDeadlockTimeout() {
        try {

            String p = props.getProperty("system.resolver.deadlockTimeout", "5");
            return Integer.parseInt(p);

        } catch (Exception e) {
            e.printStackTrace();
            return 5;
        }

    }

    public int getDefinitionCount() {
        try {

            String p = props.getProperty("common.definition.count", "1");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 3;
        }

    }

    public int getIncompleteTimeout() {
        try {

            String p = props.getProperty("system.resolver.incompleteTimeout", "1");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 10;
        }

    }

    public double getListenerFraction() {
        try {

            String p = props.getProperty("common.messaging.agentListenerFactor", "0");
            return Double.parseDouble(p);

        } catch (Exception e) {
            return 10;
        }

    }

    public String getLocalTopicConnectionFactoryName() {
        try {

            String p = props.getProperty("system.messaging.LocalTopicConnectionFactory", null);

            return p;

        } catch (Exception e) {
            return null;
        }

    }

    public int getMaxCacheSize() {
        try {

            String p = props.getProperty("common.data.maxCacheSize", "1000");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 1000;
        }

    }

    public int getMaxParallelSimulations() {
        try {

            String p = props.getProperty("system.resource.upper", "40");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 3;
        }

    }

    public double getMessagingFraction() {
        try {

            String p = props.getProperty("common.messaging.agentMessagingFactor", "1");
            return Double.parseDouble(p);

        } catch (Exception e) {
            return 10;
        }

    }

    public int getMinParallelSimulations() {
        try {

            String p = props.getProperty("system.resource.lower", "30");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 3;
        }

    }

    public int getNotificationOption() {
        try {

            String p = props.getProperty("common.messaging.notification", "NEVER");
            if (p.equals("NEVER")) {
                return -1;
            }
            if (p.equals("ALWAYS")) {
                return -2;
            }
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 10;
        }

    }

    public int getPersistentCacheThreshold() {
        try {

            String p = props.getProperty("common.data.persistentCacheThreshold", "1000");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 1000;
        }

    }

    public String getQueueConnectionFactoryName() {
        try {

            String p = props.getProperty("system.messaging.QueueConnectionFactory", null);

            return p;

        } catch (Exception e) {
            return null;
        }

    }

    public int getSimulationPartitionSize() {
        try {

            String p = props.getProperty("common.runtime.partition", "100");
            return Integer.parseInt(p);

        } catch (Exception e) {
            return 3;
        }

    }

    public String getTopicConnectionFactoryName() {
        try {

            String p = props.getProperty("system.messaging.TopicConnectionFactory", null);

            return p;

        } catch (Exception e) {
            return null;
        }

    }

}
