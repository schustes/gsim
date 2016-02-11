package gsim.sim.engine.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DatabaseManagerHSQL implements DatabaseManager {

    private final static String HOST = "localhost";

    private static Logger logger = Logger.getLogger(DatabaseManagerHSQL.class);

    private static DatabaseManagerHSQL manager;

    public Connection getConnection(String db, String user, String pw) {

        Connection con = null;
        try {
            // String s = "jdbc:hsqldb:hsql://localhost/"+db+","+user+","+pw;
            // System.out.println(s);
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/" + db, "sa", "");
            // con = DriverManager.getConnection("jdbc:hsqldb:file:"+db,user,pw);
            // con = DriverManager.getConnection("jdbc:postgresql://" + HOST + "/"
            // + db + "?user=" + user + "&password=" + pw);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return con;
    }

    @Override
    public Connection getConnection(String host, String db, String user, String pw) {
        Connection con = null;
        try {
            // String s = "jdbc:hsqldb:hsql://localhost/"+db+","+user+","+pw;
            System.out.println("jdbc:hsqldb:hsql://" + host + "/" + db);
            con = DriverManager.getConnection("jdbc:hsqldb:hsql://" + host + "/" + db, "sa", "");
            // con = DriverManager.getConnection("jdbc:hsqldb:file:"+db,user,pw);
            // con = DriverManager.getConnection("jdbc:postgresql://" + HOST + "/"
            // + db + "?user=" + user + "&password=" + pw);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return con;

    }

    @Override
    public void releaseConnection(String host, String db, Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseManagerHSQL getInstance() {
        if (manager == null) {
            manager = new DatabaseManagerHSQL();
        }
        return manager;
    }

    static {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (Exception e) {
            System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
        }
    }

}
