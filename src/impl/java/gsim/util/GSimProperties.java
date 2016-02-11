package gsim.util;

import java.io.Serializable;
import java.util.HashMap;

public class GSimProperties<Sring, Object> extends HashMap<String, Object> implements Serializable {

    public static String ACTION_POLICY = "ACTION_POLICY";

    public static String DB_CLEAR = "DB_CLEAR";

    public static String DB_CONNECTIONPOOL = "DB_CONNECTIONPOOL";

    public static String DB_HOST = "DB_HOST";

    public static String DB_MANAGER = "DB_MANAGER";

    public static String DB_NAME = "DB_NAME";

    public static String DB_PASSWORD = "DB_PASS";

    public static String DB_USER = "DB_USER";

    public static String ENV_DELEGATES = "ENV_DELEGATES";

    public static String JNDICONTEXT = "CONTEXT";

    public static String MAX_REWARD = "MAX_REWARD";

    public static String PARTITION_SIZE = "PARTITION_SIZE";

    public static String SIM_ID = "SIM_ID";

    private static final long serialVersionUID = 1L;

    public enum POLICY {
        PARALLEL, SERIAL
    }

    public enum RES_POLICY {
        ALWAYS_WAIT, NEVER_WAIT
    }

}
