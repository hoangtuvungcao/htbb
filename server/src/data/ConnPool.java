/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ConnPool {
    public String url;
    public String user;
    public String password;
    public int countConnection = 0;
    public static String driver = "com.mysql.jdbc.Driver";
    List<Connection> connsNap = new Vector<>();
    int timeout = 30000;
    int maxConn;
    public volatile LinkedList<Connection> pool = new LinkedList<>();

    public ConnPool(String url, String user, String password, int max) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxConn = max;
        System.out.println("URL: " + this.url + " | USER: " + this.user + " | PASS: " + this.password);
        this.initPool();
    }

    public static Connection newConnectBadwords() throws SQLException {
        try {
            Class.forName(driver);
            return DriverManager.getConnection("jdbc:mysql://localhost/webnew", "root", "web1!2@3#bew");
        } catch (Exception var1) {
            return null;
        }
    }

    public Connection newConnection() throws SQLException {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(this.url, this.user, this.password);
        } catch (ClassNotFoundException var2) {
            throw new SQLException("Can't find class for driver: " + driver);
        }
    }

    public void initPool() {
        for (int i = 0; i < this.maxConn; i++) {
            try {
                Connection c = this.newConnection();
                if (c != null) {
                    this.addConToPool(c);
                }
            } catch (Exception var3) {
            }
        }
    }

    public void addConToPool(Connection con) {
        if (this.pool.size() < this.maxConn) {
            if (!this.pool.contains(con)) {
                synchronized (this.pool) {
                    this.pool.addLast(con);
                    this.pool.notifyAll();
                }
            }
        } else {
            try {
                con.close();
            } catch (SQLException var3) {
            }

            Connection var5 = null;
        }
    }

    public void closeIdleConnection() {
        int poolSize = this.pool.size();

        for (int i = 0; i < poolSize; i++) {
            Connection idleCon = null;
            synchronized (this.pool) {
                idleCon = this.pool.removeFirst();
                this.pool.notifyAll();
            }

            try {
                idleCon.close();
                idleCon = null;
            } catch (SQLException var7) {
                var7.printStackTrace();
            }
        }

        if (poolSize > this.maxConn) {
            poolSize = this.maxConn;
        }

        for (int i = 0; i < poolSize; i++) {
            synchronized (this.pool) {
                try {
                    this.pool.add(this.newConnection());
                } catch (Exception var5) {
                }
            }
        }
    }

    public Connection getConFromPool() throws SQLException {
        if (this.pool.size() == 0) {
            this.initPool();
        }

        Connection con = null;
        synchronized (this.pool) {
            con = this.pool.removeFirst();
        }

        try {
            if (con != null && !con.isClosed()) {
                this.countConnection++;
                return con;
            } else {
                return this.getConnection();
            }
        } catch (Exception var3) {
            return null;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.getConFromPool();
    }

    public void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException var3) {
        }
    }

    public void freeAll() {
        for (int i = 0; i < this.pool.size(); i++) {
            try {
                this.pool.remove(i).close();
            } catch (Exception var4) {
            }
        }

        try {
            this.pool.removeAll(this.pool);
        } catch (Exception var3) {
        }
    }

    public void free(Connection conn) {
        if (conn != null) {
            this.addConToPool(conn);
        }

        this.countConnection--;
        if (this.countConnection < 0) {
            this.countConnection = 0;
        }
    }
}
