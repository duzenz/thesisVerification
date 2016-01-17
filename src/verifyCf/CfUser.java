package verifyCf;

import java.io.File;
import java.util.List;

import model.UserTrack;

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

import util.Constants;
import util.Util;
import connection.DBOperation;

public class CfUser {

    DBOperation dbUtil;
    Util util;

    public CfUser() {
        util = new Util();
        dbUtil = new DBOperation();
    }
    
    public void runOperations(int index) {
        String trainingTable = Constants.trainingUserTrackTable + index;
        String testTable = Constants.testUserTrackTable + index;
        String columns = "user_id,track_id,listen_count";
        String filename = Constants.absoluteDataLocation + Constants.trainingUserTrackTable + index + ".csv";
        String resultFilename = Constants.absoluteDataLocation + Constants.cfPrecisionResults + index + ".csv";
        int historyLimit = Constants.historyLimit;
        
        util.deleteFile(filename);
        List<Integer> userIds = dbUtil.getDistinctColumnValuesOfTable(testTable, "user_id");
        String line = "userId" + "," + "history" + "," + "recommendationCount" + "," + "testTrackCount" + "," + "founded" + "," + "precision" + "," + "recall";
        util.printResultsToFile(line, resultFilename);
        
        for (int userId : userIds) {
            System.out.println("Recommend for user " + userId + " started");
            try {
                dbUtil.createCfTrainingFile(columns, filename, trainingTable, userId);
                List<UserTrack> trainingUserTracks = dbUtil.getUserTracks(trainingTable, userId);
                List<UserTrack> testUserTracks = dbUtil.getUserTracks(testTable, userId, 5);
                int counter = 0;
                for (UserTrack ut : trainingUserTracks) {
                    counter++;
                    String text = ut.getUserId() + "," + ut.getTrackId() + "," + ut.getListenCount();
                    util.printResultsToFile(text, filename);
                    List<RecommendedItem> recommendations = makeRecommendation(userId, filename, index);
                    calculatePrecisionRecall(recommendations, testUserTracks, userId, counter, index);
                    if (counter == historyLimit) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            util.deleteFile(filename);
        }
    }

    public List<RecommendedItem> makeRecommendation(int userId, String filename, int index) {
        List<RecommendedItem> recommendations = null;
        try {
            DataModel dm = new FileDataModel(new File(Constants.absoluteDataLocation + Constants.trainingUserTrackTable + index + ".csv"));
            UserSimilarity similarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(dm), dm);
            UserNeighborhood neighborhood = new CachingUserNeighborhood(new NearestNUserNeighborhood(100, similarity, dm), dm);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(dm, neighborhood, similarity);
            recommendations = recommender.recommend(userId, Constants.historyRecommendCount);
            System.gc();
            System.out.println(recommendations.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recommendations;
    }
    
    public void calculatePrecisionRecall(List<RecommendedItem> recommendations, List<UserTrack> userTracks, int userId, int history, int index) {
        int precisionBase = recommendations.size();
        int recallBase = userTracks.size();
        
        int counter = 0;
        for (RecommendedItem recommendation: recommendations) {
            for (UserTrack ut : userTracks) {
                if (recommendation.getItemID() == ut.getTrackId()) {
                    counter++;
                }
            }
        }
        
        float precision = 0;
        float recall = 0;
        if (precisionBase != 0) {
            precision = (float) counter / precisionBase;
        }
        if (recallBase != 0) {
            recall = (float) counter / recallBase;
        }
        String line = userId + "," + history + "," + precisionBase + "," + recallBase + "," + counter + "," + precision + "," + recall;
        util.printResultsToFile(line, Constants.absoluteDataLocation + Constants.cfPrecisionResults + index + ".csv");
    }

}
