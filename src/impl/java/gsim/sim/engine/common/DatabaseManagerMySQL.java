package gsim.sim.engine.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class DatabaseManagerMySQL implements DatabaseManager {

    private static Logger logger = Logger.getLogger(DatabaseManagerMySQL.class);

    private static DatabaseManagerMySQL manager = new DatabaseManagerMySQL();

    private static HashMap<String, HashMap<Long, Connection>> map = new HashMap<String, HashMap<Long, Connection>>();

    @Override
    public Connection getConnection(String host, String db, String user, String pw) {

        // if (true) return null;

        try {

            HashMap<Long, Connection> stack = map.get(host);
            if (stack == null) {
                stack = new HashMap<Long, Connection>();
                map.put(host, stack);
            }

            if (!stack.isEmpty()) {

                Connection con = this.getConnection(stack);

                if (con == null || con != null && con.isClosed()) {
                    con = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + db + "?user=" + user + "&password=" + pw);
                }
                map.put(host, stack);
                return con;
            } else {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + db + "?user=" + user + "&password=" + pw);
                return con;
            }
        } catch (SQLException e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    public void releaseConnection(String host, String db, Connection con) {
        String key = host + "_" + db;
        HashMap<Long, Connection> stack = map.get(key);
        if (stack == null) {
            stack = new HashMap<Long, Connection>();
            map.put(key, stack);
        }

        try {
            if (stack.size() < 10 && !con.isClosed()) {
                long now = System.currentTimeMillis();
                stack.put(now, con);
                map.put(key, stack);
            } else {
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    private Connection getConnection(HashMap<Long, Connection> m) {

        long now = System.currentTimeMillis();
        for (long time : m.keySet()) {
            double h = ((now - time) / 1000d) / 60;
            if (h > 10) {
                logger.debug("Detected that a session was " + h + " minutes old --> remove");
                try {
                    Connection c = m.remove(time);
                    c.close();
                } catch (Exception e) {
                    logger.debug("Could not close connection " + e.getMessage());
                }
            } else {
                Connection con = m.remove(time);
                return con;
            }
        }
        return null;
    }

    public static DatabaseManagerMySQL getInstance() {
        return manager;
    }

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            logger.error(e);
        }
    }

}
