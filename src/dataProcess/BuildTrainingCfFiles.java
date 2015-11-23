package dataProcess;

import java.sql.SQLException;

import util.Constants;
import util.Util;
import connection.DBOperation;

public class BuildTrainingCfFiles {

    private Util util;
    private DBOperation dbUtil;
    private int count;

    public BuildTrainingCfFiles() {
        util = new Util();
        dbUtil = new DBOperation();
    }

    public void runOperations(int tableIndex) {
        try {
            DBOperation.conn.removeTable(Constants.trainingTable + tableIndex);

            DBOperation.conn.removeTable(Constants.testTable + tableIndex);

            DBOperation.conn.removeTable(Constants.trainingUserTrackTable + tableIndex);

            DBOperation.conn.removeTable(Constants.testUserTrackTable + tableIndex);

            int limitThreshold = (count * (tableIndex + 1)) / 6;

            DBOperation.conn.insert("create table " + Constants.trainingTable + tableIndex + " (SELECT * FROM " + Constants.dataTable + " order by time limit 0, " + limitThreshold + ")");

            DBOperation.conn.insert("create table " + Constants.testTable + tableIndex + " (SELECT * FROM " + Constants.dataTable + " order by time limit " + (limitThreshold + 1) + ", " + count + ")");

            DBOperation.conn.insert("create table " + Constants.trainingUserTrackTable + tableIndex + " SELECT user_id, track_id, count(track_id) as listen_count FROM training_" + tableIndex + " group by user_id, track_id " + " having listen_count >= " + Constants.listenThreshold);

            DBOperation.conn.insert("create table " + Constants.testUserTrackTable + tableIndex + " SELECT user_id, track_id, count(track_id) as listen_count FROM test_" + tableIndex + " group by user_id, track_id " + " having listen_count >= " + Constants.listenThreshold);

            DBOperation.conn.removeDifferentValuesFromTable(Constants.testUserTrackTable + tableIndex, Constants.trainingUserTrackTable + tableIndex, "user_id");

            DBOperation.conn.removeDifferentValuesFromTable(Constants.trainingUserTrackTable + tableIndex, Constants.testUserTrackTable + tableIndex, "user_id");

            String filename = Constants.absoluteDataLocation + Constants.trainingUserTrackTable + tableIndex + ".csv";
            util.deleteFile(filename);
            DBOperation.conn.createCsvFiles(Constants.trainingUserTrackTable + tableIndex, "user_id,track_id,listen_count", filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createBaseTable() {
        try {
            DBOperation.conn.removeTable(Constants.dataTable);
            DBOperation.conn.insert("create table " + Constants.dataTable + " select * from " + Constants.rawTable + " where time between '" + Constants.startDate + "' and '" + Constants.endDate
                    + "'");
            count = DBOperation.conn.getTableCount(Constants.dataTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
