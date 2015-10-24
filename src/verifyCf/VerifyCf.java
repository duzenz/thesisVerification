package verifyCf;
import connection.MysqlConnect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.List;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.CachingUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class VerifyCf {

    public static int threshold = 15;
    public static int recommendationCount = 1000;
    public static MysqlConnect conn = null;
    public static DataModel dm;
    public static UserSimilarity similarity;
    public static UserNeighborhood neighborhood;
    public static UserBasedRecommender recommender;
    public static String trainingTableName = "training_user_track_";
    public static String testTableName = "test_user_track_";
    public static String outputFileName = "cf_test_results_";
    public static int  i = 0;
    public static int startIndex = 1;
    public static int endIndex = 3;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        conn = MysqlConnect.getDbCon();
        for (i = startIndex; i <= endIndex; i++) {
            try {
                dm = new FileDataModel(new File("data\\" + trainingTableName + i + ".csv"));
                similarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(dm), dm);
                neighborhood = new CachingUserNeighborhood(new NearestNUserNeighborhood(VerifyCf.threshold, similarity, dm), dm);
                recommender = new GenericUserBasedRecommender(dm, neighborhood, similarity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            VerifyCf verify = new VerifyCf();
            verify.verifyCF();
            long stopTime = System.currentTimeMillis();
            System.out.println((stopTime - startTime) / 1000 + " sec");
        }
    }
    
    public void getUserRecommendations(int userId) {
        try {
            String sql = "select * from " + testTableName + i + " where user_id = " + userId;
            ResultSet resultset = conn.query(sql);
            List<RecommendedItem> recommendations = recommender.recommend(userId, VerifyCf.recommendationCount);
            System.out.println("recom size: " + recommendations.size());
            
            while (resultset.next()) {
                int trackId = resultset.getInt("track_id");
                boolean found = false;
                int counter = 0;
                for (RecommendedItem item : recommendations) {
                    counter++;
                    if (item.getItemID() == trackId) {
                        found = true;
                        printResultsToFile(userId + "," + trackId + "," + "1," + item.getValue() + ","+ counter, "data\\"  + outputFileName + i + ".csv");
                        break;
                    }
                }
                if (!found) {
                    printResultsToFile(userId + "," + trackId + "," + "0," + 0 + ","+ counter, "data\\" + outputFileName + i + ".csv");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verifyCF() {
        try {
            ResultSet resultSet = conn.query("select distinct(user_id) from " + testTableName + i + " order by user_id");
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                System.out.println("user id : " + userId);
                long startTime = System.currentTimeMillis();
                getUserRecommendations(userId);
                long stopTime = System.currentTimeMillis();
                System.out.println((stopTime - startTime) / 1000 + " sec");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void printResultsToFile(String text, String filename) {
        File inputFile = new File(filename);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile, true));
            writer.append(text + System.getProperty("line.separator"));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}