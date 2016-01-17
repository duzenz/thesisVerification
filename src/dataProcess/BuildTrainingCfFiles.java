package dataProcess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import model.UserTrack;
import util.Constants;
import util.Util;
import connection.DBOperation;

public class BuildTrainingCfFiles {

    private Util util;
    private DBOperation dbUtil;

    public BuildTrainingCfFiles() {
        util = new Util();
        dbUtil = new DBOperation();
    }

    public void runOperations(int tableIndex) {
        try {
            
            int count = getCountOfBaseTable();
            
            dbUtil.getConn().removeTable(Constants.trainingTable + tableIndex);
            System.out.println("Removed table : " + Constants.trainingTable + tableIndex);

            dbUtil.getConn().removeTable(Constants.testTable + tableIndex);
            System.out.println("Removed table : " + Constants.testTable + tableIndex);

            dbUtil.getConn().removeTable(Constants.trainingUserTrackTable + tableIndex);
            System.out.println("Removed table : " + Constants.trainingUserTrackTable + tableIndex);

            dbUtil.getConn().removeTable(Constants.testUserTrackTable + tableIndex);
            System.out.println("Removed table : " + Constants.testUserTrackTable + tableIndex);

            int limitThreshold = 0;
            if (tableIndex == 1) {
                limitThreshold = (count * 7) / 10;
            } else if (tableIndex == 2) {
                limitThreshold = (count * 8) / 10;
            } else {
                limitThreshold = (count * 9) / 10;
            }

            dbUtil.getConn().insert("create table " + Constants.trainingTable + tableIndex + " (SELECT * FROM " + Constants.dataTable + " order by time limit 0, " + limitThreshold + ")");
            System.out.println("Created table : " + Constants.trainingTable + tableIndex);

            dbUtil.getConn().insert("create table " + Constants.testTable + tableIndex + " (SELECT * FROM " + Constants.dataTable + " order by time limit " + (limitThreshold + 1) + ", " + count + ")");
            System.out.println("Created table : " + Constants.testTable + tableIndex);

            dbUtil.getConn().insert("create table " + Constants.trainingUserTrackTable + tableIndex + " SELECT user_id, track_id, count(track_id) as listen_count, max(time) as time FROM training_" + tableIndex + " group by user_id, track_id " + " having listen_count >= " + Constants.listenThreshold);
            System.out.println("Created table : " + Constants.trainingUserTrackTable + tableIndex);

            dbUtil.getConn().insert("create table " + Constants.testUserTrackTable + tableIndex + " SELECT user_id, track_id, count(track_id) as listen_count, max(time) as time FROM test_" + tableIndex + " group by user_id, track_id " + " having listen_count >= " + Constants.listenThreshold);
            System.out.println("Created table : " + Constants.testUserTrackTable + tableIndex);

            ResultSet rs = dbUtil.getConn().query("select max(listen_count) as max_listen, min(listen_count) as min_listen from " + Constants.trainingUserTrackTable + tableIndex);
            int minCount = 0;
            int maxCount = 0;
            while (rs.next()) {
                minCount = rs.getInt("min_listen");
                maxCount = rs.getInt("max_listen");
            }

            dbUtil.getConn().insert("ALTER TABLE " + Constants.trainingUserTrackTable + tableIndex + " ADD COLUMN rating FLOAT(4, 2)");

            dbUtil.getConn().insert("update " + Constants.trainingUserTrackTable + tableIndex + " set rating = (1 + (listen_count - 1) * 9 / ( " + maxCount + "- " + minCount + " ))");

            dbUtil.getConn().removeDifferentValuesFromTable(Constants.testUserTrackTable + tableIndex, Constants.trainingUserTrackTable + tableIndex, "user_id");
            System.out.println("Remove different users of table : " + Constants.testUserTrackTable + tableIndex);
            
            dbUtil.getConn().removeDifferentValuesFromTable(Constants.testUserTrackTable + tableIndex, Constants.trainingUserTrackTable + tableIndex, "track_id");
            System.out.println("Remove different tracks of table : " + Constants.testUserTrackTable + tableIndex);
            
            // dbUtil.getConn().removeDifferentValuesFromTable(Constants.trainingUserTrackTable
            // + tableIndex, Constants.testUserTrackTable + tableIndex,
            // "user_id");
            // System.out.println("Remove different users of table : " +
            // Constants.trainingUserTrackTable + tableIndex);

            List<Integer> distinctUserIds = dbUtil.getDistinctColumnValuesOfTable(Constants.trainingUserTrackTable + tableIndex, "user_id");
            for (int userId : distinctUserIds) {
                List<UserTrack> userTracks = dbUtil.getUserTracks(Constants.trainingUserTrackTable + tableIndex, userId);
                String trackString = "";
                for (UserTrack ut : userTracks) {
                    trackString += ut.getTrackId() + ",";
                }
                trackString = trackString.replaceAll(",$", "");
                dbUtil.getConn().insert("delete from " + Constants.testUserTrackTable + tableIndex + " where track_id in (" + trackString + ") and user_id = " + userId);
                System.out.println("deleted from " + Constants.testUserTrackTable + tableIndex + " userId : " + userId + " trackString : " + trackString);
            }

            // List<Integer> tempDistinctUsers =
            // dbUtil.getDistinctColumnValuesOfTable(Constants.trainingUserTrackTable
            // + tableIndex, "user_id");

            /*
             * if (tableIndex == 1) { // get distinct users of table
             * distinctUsers = tempDistinctUsers; } else { // synchronize users
             * of tables distinctUsers.retainAll(tempDistinctUsers); }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Creating csv file TODO add tablename TODO add fields
     */
    public void createCsvDatas(int index) {
        String filename = Constants.absoluteDataLocation + Constants.trainingUserTrackTable + index + ".csv";
        System.out.println("Deleted file : " + filename);
        util.deleteFile(filename);
        System.out.println("Creating file : " + filename);
        // tempUsers = distinctUsers.toString().substring(1);
        // tempUsers = tempUsers.substring(0, tempUsers.length() - 1);
        try {
            // dbUtil.getConn().insert("delete from " +
            // Constants.testUserTrackTable + index + " where user_id not in ("
            // + tempUsers + ")");
            // System.out.println("Different users are deleted from : " +
            // Constants.testUserTrackTable + index);
            // dbUtil.getConn().insert("delete from " +
            // Constants.trainingUserTrackTable + index +
            // " where user_id not in (" + tempUsers + ")");
            // System.out.println("Different users are deleted from : " +
            // Constants.trainingUserTrackTable + index);
            // dbUtil.getConn().createCsvFiles(Constants.trainingUserTrackTable
            // + index, "user_id,track_id,rating", filename);
            dbUtil.getConn().createCsvFiles(Constants.trainingUserTrackTable + index, "user_id,track_id,listen_count", filename);
            System.out.println("Csv file is created for " + Constants.trainingUserTrackTable + index);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
     * Base table creation for cf and cbr validation
     */
    public void createBaseTable() {
        try {
            dbUtil.getConn().removeTable(Constants.dataTable);
            System.out.println("Base table deleted");
            dbUtil.getConn().insert("create table " + Constants.dataTable + " select * from " + Constants.rawTable + " where time between '" + Constants.startDate + "' and '" + Constants.endDate + "'");
            System.out.println("Base table created : " + Constants.dataTable + " between dates : " + Constants.startDate + " -- " + Constants.endDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int getCountOfBaseTable() {
        int count = 0;
        try {
            count = dbUtil.getConn().getTableCount(Constants.dataTable);
            System.out.println("Base count : " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
