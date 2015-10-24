package connection;

import com.mysql.jdbc.Connection;

import java.sql.*;

public final class MysqlConnect {
    public Connection conn;
    private Statement statement;
    public static MysqlConnect db;

    private MysqlConnect() {
        String url = "jdbc:mysql://localhost:3306/";
        String dbName = "training";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "";
        try {
            Class.forName(driver).newInstance();
            this.conn = (Connection) DriverManager.getConnection(url + dbName, userName, password);
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
    }

    public static synchronized MysqlConnect getDbCon() {
        if (db == null) {
            db = new MysqlConnect();
        }
        return db;
    }

    public ResultSet query(String query) throws SQLException {
        statement = db.conn.createStatement();
        ResultSet res = statement.executeQuery(query);
        return res;
    }

    public int insert(String insertQuery) throws SQLException {
        statement = db.conn.createStatement();
        int result = statement.executeUpdate(insertQuery);
        return result;
    }

    public void exportData(String query) {
        Statement stmt;
        try {
            stmt = db.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
            stmt = null;
        }
    }

    public void createCsvFiles(String tablename, String columns, String filename) {
        String query = "SELECT " + columns + " into OUTFILE  '" + filename + "" + "' FIELDS TERMINATED BY ',' FROM " + tablename + " where order by " + columns;
        exportData(query);
    }
}