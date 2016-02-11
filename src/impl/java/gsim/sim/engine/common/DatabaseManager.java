package gsim.sim.engine.common;

import java.sql.Connection;

public interface DatabaseManager {
    public Connection getConnection(String host, String db, String user, String pw);

    public void releaseConnection(String host, String db, Connection con);
}
