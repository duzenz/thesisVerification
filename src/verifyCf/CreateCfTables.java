package verifyCf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;

import model.Recommendation;
import connection.MysqlConnect;
import util.*;

public class CreateCfTables {
    
    public int threshold = 10;
    public MysqlConnect conn = null;
    public String csvFileName = "data\\cf_test_results_";
    public String trainingTableName = "training_user_track_";
    public String testTableName = "test_user_track_";
    public String outputFileName = "results\\cf_validation_user_tables_";
    public Util util = new Util();
    
    public static void main(String[] args) {
        
        CreateCfTables cf = new CreateCfTables();
        cf.conn = MysqlConnect.getDbCon();
        
        List<Integer> testUsers = new ArrayList<Integer>();
        for (int i = 1; i <= 3 ; i++) {
            //print headers
            cf.printHeaders(cf.outputFileName + i + "_" + cf.threshold + ".csv");
            
            //print results
            List<Recommendation> recommendationList = cf.readCsvFile(i);
            testUsers = cf.getDistinctUsersOfTable(cf.testTableName + i);
            
            List<Integer> testTrackIdsOfUser = new ArrayList<Integer>();
            List<Integer> trainingTrackIdsOfUser = new ArrayList<Integer>();
            for (int userId : testUsers) {
                System.out.println(userId);
                
                testTrackIdsOfUser = cf.getTracksOfUserInATable(userId, cf.testTableName + i);
                trainingTrackIdsOfUser = cf.getTracksOfUserInATable(userId, cf.trainingTableName + i);
                
                int testTrackCount = testTrackIdsOfUser.size();
                int trainingTrackCount = trainingTrackIdsOfUser.size();
                testTrackIdsOfUser.removeAll(trainingTrackIdsOfUser);
                int testDiffTrainingCount = testTrackIdsOfUser.size();
                
                int foundCounter = 0; 
                for (Recommendation recm : recommendationList) {
                    if (recm.getUserId() == userId && recm.getRecommended() == 1 && recm.getOrder() <= cf.threshold) {
                        foundCounter++;
                    }
                }
                
                float precision = (float) foundCounter / cf.threshold;
                float recall = (float) foundCounter / testTrackIdsOfUser.size();
                float fMeasure = (float) 2 * precision * recall / (precision + recall);
              
                String row = userId + "," + testTrackCount + "," + trainingTrackCount + "," + 
                        testDiffTrainingCount + "," + cf.threshold + "," + foundCounter + "," +
                        precision + "," + recall + "," + fMeasure;
                cf.util.printResultsToFile(row, cf.outputFileName + i + "_" + cf.threshold + ".csv");
                System.out.println(row);
                System.out.println("=====================");
            }
        } 
    }
    
    public List<Integer> getDistinctUsersOfTable(String tableName) {
        String sql = "select distinct(user_id) from " + tableName + " order by user_id";
        List<Integer> userIds = new ArrayList<Integer>();
        try {
            ResultSet rs = conn.query(sql);
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return userIds;
    }
    
    public List<Integer> getTracksOfUserInATable(int userId, String tableName) {
        String sql = "select track_id from " + tableName + " where user_id = " + userId + " order by track_id";
        List<Integer> trackIds = new ArrayList<Integer>();
        try {
            ResultSet rs = conn.query(sql);
            while (rs.next()) {
                trackIds.add(rs.getInt("track_id"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return trackIds;
    }
    
    public List<Recommendation> readCsvFile(int i) {
        String csvFile = csvFileName + i + ".csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<Recommendation> objList = new ArrayList<Recommendation>();
        
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                int userId = Integer.parseInt(row[0]);
                int trackId = Integer.parseInt(row[1]);
                int found = Integer.parseInt(row[2]);
                String value = row[3];
                int foundedLine = Integer.parseInt(row[4]);
                Recommendation rmd = new Recommendation(userId, trackId, found, value, foundedLine);
                objList.add(rmd);
            }
            System.out.println(objList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return objList;
    }
    
    public void printHeaders(String filename) {
        util.printResultsToFile("User ID,"
                + "Test Track Sayýsý,"
                + "Training Track Sayýsý,"
                + "Test - Training,"
                + "Öneri Sayýsý,"
                + "Doðru Öneri Sayýsý,"
                + "Precision,"
                + "Recall,"
                + "F-Measure", filename);
    }

}
