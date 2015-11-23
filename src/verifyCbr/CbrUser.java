package verifyCbr;

import java.util.List;
import java.util.Map;

import model.CbrUtil;
import model.CustomIndividual;
import model.User;
import model.UserTrack;
import util.Constants;
import util.Util;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import connection.DBOperation;

public class CbrUser {

    public DBOperation dbUtil;
    public Util util;
    public CbrUtil cbrUtil;

    private String testUserTrackTable;

    public CbrUser() {
        dbUtil = new DBOperation();
        util = new Util();
        cbrUtil = new CbrUtil();
    }

    public void runOperations(int index) {
        testUserTrackTable = Constants.testUserTrackTable + index;
        OntModel model = cbrUtil.loadCbrModel(Constants.dataLocations + Constants.trainingOwlFileName + index + ".owl");
        OntClass caseObj = model.createClass(Constants.namespace + "RECOMMEND_CASE");
        System.out.println("Model loaded for " + Constants.trainingOwlFileName + index + ".owl");
        List<Individual> modelInstances = cbrUtil.getInstanceList(model, caseObj);

        String line = "userId" + "," + "precisionBase" + "," + "recallBase" + "," + "founded" + "," + "precision" + "," + "recall";
        util.printResultsToFile(line, Constants.cbrPrecisionResults + index + ".csv");
        
        List<Integer> userIds = dbUtil.getDistinctColumnValuesOfTable(testUserTrackTable, "user_id");
        for (int userId : userIds) {
            System.out.println(userId);
            List<UserTrack> userTracks = dbUtil.getUserTracks(testUserTrackTable, userId);
            User userInfo = dbUtil.getUserInfo(userId);
            CustomIndividual customIndividiual = cbrUtil.createIndividualForUser(model, userInfo, userTracks);
            Map<String, Integer> distanceMap = util.sortByComparator(cbrUtil.compareInstanceWithModelInstances(model, modelInstances, customIndividiual), Constants.DESC);
            printRecommendationsToFile(distanceMap, userTracks, userId, index);
        }
    }

    public void printRecommendationsToFile(Map<String, Integer> distanceMap, List<UserTrack> userTracks, int userId, int index) {
        int counter = 0;
        int foundCounter = 0;
        int recallBase = userTracks.size();

        for (Map.Entry<String, Integer> entry : distanceMap.entrySet()) {
            counter++;
            for (UserTrack userTrack : userTracks) {
                if (("I" + userTrack.getTrackId()).equals(entry.getKey())) {
                    foundCounter++;
                }
            }
            if (counter == Constants.cbrRecommendationCount) {
                break;
            }
        }

        float precision = 0;
        float recall = 0;
        if (counter != 0) {
            precision = (float) foundCounter / counter;
        }
        if (recallBase != 0) {
            recall = (float) counter / recallBase;
        }

        String line = userId + "," + counter + "," + recallBase + "," + foundCounter + "," + precision + "," + recall;
        util.printResultsToFile(line, Constants.absoluteDataLocation + Constants.cbrPrecisionResults + index + ".csv");
    }

}
