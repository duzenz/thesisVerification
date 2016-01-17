package connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Connection;

public final class MysqlConnect {
    private Connection conn;
    private Statement statement;
    private static MysqlConnect db;

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
        return statement.executeQuery(query);
    }

    public int insert(String insertQuery) throws SQLException {
        statement = db.conn.createStatement();
        return statement.executeUpdate(insertQuery);
    }

    public void removeTable(String tablename) throws SQLException {
        String sql = "drop table IF EXISTS " + tablename;
        insert(sql);
    }

    public void createCsvFiles(String tablename, String columns, String filename) throws SQLException {
        query("SELECT " + columns + " into OUTFILE  '" + filename + "" + "' FIELDS TERMINATED BY ',' FROM " + tablename + " order by " + columns);
    }

    public void createCsvQuery(String query) throws SQLException {
        query(query);
    }

    public void removeDifferentValuesFromTable(String targetTable, String baseTable, String column) throws SQLException {
        System.out.println("removing " + column + " from " + targetTable + " base table : " + baseTable);
        String sql = "select distinct(" + column + ") from " + targetTable + " where " + column + " not in (select distinct(" + column + ") from " + baseTable + ")";
        ResultSet rs = query(sql);
        String s = "";
        while (rs.next()) {
            s += rs.getInt(column) + ",";
        }
        s = s.replaceAll(",$", "");
        insert("delete from " + targetTable + " where " + column + " in (" + s + ")");
    }

    public int getTableCount(String tablename) throws SQLException {
        String sql = "SELECT count(*) as count from " + tablename;
        ResultSet countRes = query(sql);
        int count = 0;
        while (countRes.next()) {
            count = countRes.getInt("count");
        }
        return count;
    }
}