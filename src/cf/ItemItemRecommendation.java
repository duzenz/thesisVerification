package cf;

import java.io.File;
import java.util.List;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class ItemItemRecommendation {

    public static void main(String[] args) {
        try {
            DataModel dm = new FileDataModel(new File("data/training_user_track_2.csv"));
            ItemSimilarity im = new LogLikelihoodSimilarity(dm);
            
            GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dm, im);
            
            int counter = 1;
            
            for (LongPrimitiveIterator items = dm.getItemIDs(); items.hasNext();) {
                long itemId = items.nextLong();
                
                System.out.println(itemId);
                List<RecommendedItem> recommendations = recommender.mostSimilarItems(538, 5);
                for (RecommendedItem rm : recommendations) {
                    System.out.println(rm.getItemID() + "    " + rm.getValue());
                }
                counter++;
                if (counter > 10) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
