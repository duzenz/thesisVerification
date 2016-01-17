package util;

import java.io.File;
import java.util.List;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.CachingUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class RecommendationUtil {

    public UserBasedRecommender getRecommender(String filename) {
        try {
            DataModel dm = new FileDataModel(new File(filename));
            UserSimilarity similarity = new CachingUserSimilarity(new  LogLikelihoodSimilarity(dm), dm);
            UserNeighborhood neighborhood = new CachingUserNeighborhood(new NearestNUserNeighborhood(10, similarity, dm), dm);
            return new GenericBooleanPrefUserBasedRecommender(dm, neighborhood, similarity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RecommendedItem> recommendTrack(UserBasedRecommender recommender, int userId, int recommendationCount) {
        try {
            return recommender.recommend(userId, recommendationCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
