package verifyCf;

import java.util.List;

import model.UserTrack;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;

import util.Constants;
import util.RecommendationUtil;
import util.Util;
import connection.DBOperation;

public class VerifyCf {

    private Util util;
    private DBOperation dbUtil;
    private RecommendationUtil recUtil;

    public VerifyCf() {
        util = new Util();
        dbUtil = new DBOperation();
        recUtil = new RecommendationUtil();
    }

    public static void main(String[] args) {
        VerifyCf verify = new VerifyCf();
        for (int i = 1; i <= 3; i++) {
            verify.runOperations(i);
        }
    }

    public void runOperations(int index) {
        long startTime = System.currentTimeMillis();

        UserBasedRecommender recommender = recUtil.getRecommender(Constants.dataLocations + Constants.trainingUserTrackTable + index + ".csv");
        verifyCF(index, recommender);

        long stopTime = System.currentTimeMillis();
        System.out.println((stopTime - startTime) / 1000 + " sec");
    }

    public void getUserRecommendations(int userId, int index, UserBasedRecommender recommender) {
        List<UserTrack> testUserTracks = dbUtil.getUserTracks(Constants.testUserTrackTable + index, userId);
        List<UserTrack> trainingUserTracks = dbUtil.getUserTracks(Constants.trainingUserTrackTable + index, userId);
        List<RecommendedItem> recommendations = recUtil.recommendTrack(recommender, userId, Constants.recommendationCount);
        System.out.println("user id: " + userId + "  recom size: " + recommendations.size());
        //printResults(userTracks, recommendations, index);
        calculatePrecisionRecall(recommendations, testUserTracks, trainingUserTracks, userId, index);
    }

    public void verifyCF(int index, UserBasedRecommender recommender) {
        List<Integer> distinctUserIds = dbUtil.getDistinctColumnValuesOfTable(Constants.testUserTrackTable + index, "user_id");
        String headerLine = "User Id, Listen Threshold, Recommendation Size, Training Size, Test Size, Found Count, Precision, Recall";
        String cfResultFileName = Constants.absoluteDataLocation + Constants.cfPrecisionResults + index + ".csv";
        util.deleteFile(cfResultFileName);
        util.printResultsToFile(headerLine, cfResultFileName);
        for (int userId : distinctUserIds) {
            getUserRecommendations(userId, index, recommender);
        }
    }

    public void printResults(List<UserTrack> userTracks, List<RecommendedItem> recommendations, int index) {
        for (UserTrack ut : userTracks) {
            boolean found = false;
            int counter = 0;
            for (RecommendedItem item : recommendations) {
                counter++;
                if (item.getItemID() == ut.getTrackId()) {
                    found = true;
                    util.printResultsToFile(ut.getUserId() + "," + ut.getTrackId() + "," + "1," + item.getValue() + "," + counter, Constants.dataLocations + Constants.cfOutputFileName + index
                            + ".csv");
                    break;
                }
            }
            if (!found) {
                util.printResultsToFile(ut.getUserId() + "," + ut.getTrackId() + "," + "0," + 0 + "," + counter, Constants.dataLocations + Constants.cfOutputFileName + index + ".csv");
            }
        }
    }

    public void calculatePrecisionRecall(List<RecommendedItem> recommendations, List<UserTrack> testUserTracks, List<UserTrack> trainingUserTracks, int userId, int index) {
        int precisionBase = recommendations.size();
        int recallBase = testUserTracks.size();

        int counter = 0;
        for (RecommendedItem recommendation : recommendations) {
            for (UserTrack ut : testUserTracks) {
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
        String line = userId + "," + Constants.listenThreshold + "," + precisionBase + "," + trainingUserTracks.size() + "," + recallBase + "," + counter + "," + precision + "," + recall;
        util.printResultsToFile(line, Constants.absoluteDataLocation + Constants.cfPrecisionResults + index + ".csv");
    }
}