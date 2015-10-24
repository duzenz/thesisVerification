package dataProcess;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.mysql.jdbc.Connection;

import connection.MysqlConnect;

public class CreateUserTrackTablesAndFiles {

    public static MysqlConnect conn = null;
    public static String dataTableName = "yearly_table";
    public static String startDate = "2008-01-01";
    public static String endDate = "2008-12-31";

    public static void main(String[] args) {
        conn = MysqlConnect.getDbCon();

        try {
            //removing old base table
            String sql = "drop table IF EXISTS " + dataTableName;
            conn.insert(sql);
            
            //creating new base table
            sql = "CREATE table " + dataTableName + " SELECT * from lastfm where time between '" + startDate + "' and '" + endDate + "'";
            conn.insert(sql);
            
            sql = "SELECT count(*) as count from " + dataTableName;
            ResultSet countRes = conn.query(sql);
            int count = 0;
            while (countRes.next()) {
                count = countRes.getInt("count");
            }
            
            //creating training and test tables
            for (int i = 1; i <= 3; i++) {
            	
            	//remove old training and test and user track tables
            	sql = "drop table IF EXISTS " + " training_" + i;
                conn.insert(sql);
                sql = "drop table IF EXISTS " + " test_" + i;
                conn.insert(sql);
                sql = "drop table IF EXISTS " + " training_user_track_" + i;
                conn.insert(sql);
                sql = "drop table IF EXISTS " + " test_user_track_" + i;
                conn.insert(sql);
                
                
                int limitThreshold = (count * (i + 1)) / 6;
                
                sql = "create table training_" + i + " (SELECT * FROM " + dataTableName + " order by time limit 0, " + limitThreshold + ")";
                System.out.println(sql);
                conn.insert(sql);
                
                sql = "create table test_" + i + " (SELECT * FROM " + dataTableName + " order by time limit " + (limitThreshold + 1) + ", " + count + ")";
                System.out.println(sql);
                conn.insert(sql);
                
                sql = "create table training_user_track_" + i + " SELECT user_id, track_id, count(track_id) as listen_count FROM training_" + i + " group by user_id, track_id";
                System.out.println(sql);
                conn.insert(sql);
                
                sql = "create table test_user_track_" + i + " SELECT user_id, track_id, count(track_id) as listen_count FROM test_" + i + " group by user_id, track_id";
                System.out.println(sql);
                conn.insert(sql);
                
                sql = "select distinct(user_id) from test_user_track_" + i + " where user_id not in (select distinct(user_id) from training_user_track_" + i + ")";
                System.out.println(sql);
                ResultSet uniqueResultSet = conn.query(sql);
                while(uniqueResultSet.next()) {
                    int userId = uniqueResultSet.getInt("user_id");
                    System.out.println(userId);
                    sql = "delete from test_user_track_" + i + " where user_id = " + userId;
                    System.out.println(sql);
                    conn.insert(sql);
                }
                
                sql = "select distinct(user_id) from training_user_track_" + i + " where user_id not in (select distinct(user_id) from test_user_track_" + i + ")";
                System.out.println(sql);
                uniqueResultSet = conn.query(sql);
                while(uniqueResultSet.next()) {
                    int userId = uniqueResultSet.getInt("user_id");
                    System.out.println(userId);
                    sql = "delete from training_user_track_" + i + " where user_id = " + userId;
                    System.out.println(sql);
                    conn.insert(sql);
                }
            }
            
            conn = null;
            createCsvFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void createCsvFiles() {
         Connection conn = connect("jdbc:mysql://localhost:3306/training", "root", "");
         
         int limit = 0;
         String columns = "user_id,track_id,listen_count";
         
         for (int i = 1; i <= 3 ; i++) {
             String filename = "D://thesisWorkspace//thesisVerification//data//training_user_track_" + i + ".csv";
             String table = "training_user_track_" + i;
             String query = "SELECT " + columns + " into OUTFILE  '" + filename + "' FIELDS TERMINATED BY ',' FROM " + table + " where listen_count > " + limit + " order by " + columns;
             exportData(conn, query);
         }
    }
    
    public static Connection connect(String db_connect_str, String db_userid, String db_password) {
        Connection conn;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = (Connection) DriverManager.getConnection(db_connect_str, db_userid, db_password);
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }
        return conn;
    }
    
    public static void exportData(Connection conn, String query) {
        Statement stmt;
        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
            stmt = null;
        }
    }

}
