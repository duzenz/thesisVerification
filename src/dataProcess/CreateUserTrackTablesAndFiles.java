package dataProcess;

import java.sql.ResultSet;

import util.Constants;
import cbr.BuildTrainingCbrFiles;
import connection.MysqlConnect;

public class CreateUserTrackTablesAndFiles {

    public static MysqlConnect conn = null;
    public static String dataTableName = "yearly_table";
    public static String startDate = "2008-01-01";
    public static String endDate = "2008-01-31";

    public static void main(String[] args) {
        conn = MysqlConnect.getDbCon();

        try {
            
            conn.removeTable(dataTableName);
            System.out.println("Removed table : " + dataTableName);

            conn.insert("create table " + dataTableName + " select * from lastfm where time between '" + startDate + "' and '" + endDate + "'");
            System.out.println("Created data table");

            int count = conn.getTableCount(dataTableName);
            System.out.println("Got the count of table : " + count);
            
            for (int i = 1; i <= 3; i++) {
                conn.removeTable("training_" + i);
                System.out.println("Removed table : " + "training_" + i);
                
                conn.removeTable("test_" + i);
                System.out.println("Removed table : " + "test_" + i);
                
                conn.removeTable("training_user_track_" + i);
                System.out.println("Removed table : " + "training_user_track_" + i);
                
                conn.removeTable("test_user_track_" + i);
                System.out.println("Removed table : " + "test_user_track_" + i);

                int limitThreshold = (count * (i + 1)) / 6;

                conn.insert("create table training_" + i + " (SELECT * FROM " + dataTableName + " order by time limit 0, " + limitThreshold + ")");
                System.out.println("Created table : " + "training_" + i);

                conn.insert("create table test_" + i + " (SELECT * FROM " + dataTableName + " order by time limit " + (limitThreshold + 1) + ", " + count + ")");
                System.out.println("Created table : " + "test_" + i);

                conn.insert("create table training_user_track_" + i + " SELECT user_id, track_id, count(track_id) as listen_count FROM training_" + i + " group by user_id, track_id");
                System.out.println("Created table : " + "training_user_track_" + i);

                conn.insert("create table test_user_track_" + i + " SELECT user_id, track_id, count(track_id) as listen_count FROM test_" + i + " group by user_id, track_id");
                System.out.println("Created table : " + "test_user_track_" + i);

                conn.removeDifferentValuesFromTable("test_user_track_" + i, "training_user_track_" + i, "user_id");
                System.out.println("removing user_id from " + "test_user_track_" + i + " base table : " + "training_user_track_" + i);

                conn.removeDifferentValuesFromTable("training_user_track_" + i, "test_user_track_" + i, "user_id");
                System.out.println("removing user_id from " + "training_user_track_" + i + " base table : " + "test_user_track_" + i);

                //TODO remove old csv files before creation
                conn.createCsvFiles("training_user_track_" + i, "user_id,track_id,listen_count", Constants.absoluteDataLocation + "training_user_track_" + i + ".csv");
                System.out.println("Created csv file for table : " + "training_user_track_" + i);
                
                BuildTrainingCbrFiles bt = new BuildTrainingCbrFiles("training_" + i + ".owl", "training_user_track_" + i);
                bt.runOperations();
            }

            conn = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}