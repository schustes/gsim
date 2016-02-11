package gsim.sim.engine.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

public class DatabaseManagerPostgres implements DatabaseManager {

    private static Logger logger = Logger.getLogger(DatabaseManagerPostgres.class);

    private static DatabaseManagerPostgres manager;

    private static ConcurrentHashMap<String, ConcurrentHashMap<Long, Connection>> map = new ConcurrentHashMap<String, ConcurrentHashMap<Long, Connection>>();

    private static int maxAge = 10;

    private static int maxPoolSize = 10;

    private static int maxTolerance = 10;

    private long closed = 0;
    private long created = 0;
    private long fetched;
    private long released = 0;

    private Semaphore sema;

    public DatabaseManagerPostgres() {
        String s1 = System.getProperty("db_maxPool");
        if (s1 != null) {
            maxPoolSize = Integer.parseInt(s1);
        }
        String s2 = System.getProperty("db_maxTolerance");
        if (s2 != null) {
            maxTolerance = Integer.parseInt(s2);
        }
        sema = new Semaphore(1);
    }

    @Override
    public Connection getConnection(String host, String db, String user, String pw) {

        try {

            // sema.acquire();

            ConcurrentHashMap<Long, Connection> stack = map.get(host + "_" + db);
            if (stack == null) {
                stack = new ConcurrentHashMap<Long, Connection>();
                map.put(host, stack);
            }

            if (!stack.isEmpty()) {

                Connection con = this.getConnection(stack);

                if (con == null || con != null && con.isClosed()) {
                    con = DriverManager.getConnection("jdbc:postgresql://" + host + "/" + db + "?user=" + user + "&password=" + pw);
                    created++;
                }
                return con;
            } else {
                Properties props = new Properties();
                props.setProperty("user", user);

                Connection con = null;
                boolean failed = true;
                for (int i = 0; i < 10 && failed; i++) {
                    try {
                        synchronized (this) {
                            con = DriverManager.getConnection("jdbc:postgresql://" + host + "/" + db + "?user=" + user + "&password=" + pw);
                            failed = false;
                        }
                    } catch (Exception e) {
                        logger.warn("Problem during connection-create, attempt=" + (i + 1));
                        e.printStackTrace();
                    }
                }

                if (con == null) {
                    logger.error("Could not create exception!");
                }

                created++;

                return con;
            }
        } catch (Exception e) {
            logger.error("Exception", e);
            return null;
        } finally {
            // sema.release();
        }
    }

    @Override
    public void releaseConnection(String host, String db, Connection con) {

        try {

            // sema.acquire();

            ConcurrentHashMap<Long, Connection> stack = map.get(host + "_" + db);

            if (stack == null) {
                stack = new ConcurrentHashMap<Long, Connection>();
                map.put(host + "_" + db, stack);
            }

            if (created - closed <= maxPoolSize + maxTolerance && stack.size() < maxPoolSize && !con.isClosed()) {
                long now = System.currentTimeMillis();
                stack.put(now, con);
                map.put(host + "_" + db, stack);
                released++;
            } else if (created - closed > maxPoolSize + maxTolerance && !con.isClosed()) {
                con.close();
                con = null;
                closed++;
            } else {
                con.close();
                con = null;
                closed++;
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            // sema.release();
        }

    }

    private Connection getConnection(ConcurrentHashMap<Long, Connection> m) {

        long now = System.currentTimeMillis();
        for (long time : m.keySet()) {
            double h = ((now - time) / 1000d) / 60;
            if (h > maxAge) {
                try {
                    Connection c = m.remove(time);
                    c.close();
                    closed++;
                } catch (Exception e) {
                    logger.info("Could not close connection " + e.getMessage());
                }
            } else {
                Connection con = m.remove(time);
                fetched++;
                return con;
            }
        }
        return null;
    }

    public static DatabaseManagerPostgres getInstance() {
        if (manager == null) {
            manager = new DatabaseManagerPostgres();
        }
        return manager;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
