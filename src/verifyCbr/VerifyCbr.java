package verifyCbr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.*;
import util.Constants;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import connection.DBOperation;

public class VerifyCbr {

    CbrUtil cbrUtil;
    DBOperation dbUtil;
    public String testTable;
    public String trainingCbrFile;

    public static void main(String[] args) {
        VerifyCbr verifyCbr = new VerifyCbr("test_user_track_1", "training_1.owl");
        verifyCbr.runOperations();
    }

    public VerifyCbr(String testTable, String trainingCbrFile) {
        this.testTable = testTable;
        this.trainingCbrFile = trainingCbrFile;
        cbrUtil = new CbrUtil();
        dbUtil = new DBOperation();
    }

    public void runOperations() {

        OntModel model = cbrUtil.loadCbrModel(Constants.dataLocations + trainingCbrFile);

        List<Integer> uniqueUserIds = dbUtil.getDistinctColumnValuesOfTable(testTable, "user_id");

        for (int userId : uniqueUserIds) {
            System.out.println(userId);
            User user = dbUtil.getUserInfo(userId);
            System.out.println(user);
            List<UserTrack> userTracks = dbUtil.getUserTracks(testTable, userId);
            System.out.println(userTracks.size());
            //Individual userIndividual = createIndividualForUser(user, userTracks);
            Individual userIndividual = createIndividualForUser(model, user, userTracks);
        }
        
        // create a individual for user
        
        // compare this individual to others

        // save values to an array

        //

    }
    
    public Individual createIndividualForUser(OntModel model, User user, List<UserTrack> userTracks) {
        OntClass obj = model.createClass(Constants.namespace + "RECOMMEND_CASE");
        Individual newInd = obj.createIndividual(Constants.namespace + "Inew" + user.getUserId());
        
        newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-AGE"), user.getAgeCol());
        newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-COUNTRY"), user.getCountry());
        newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-GENDER"), user.getGender());
        newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-REGISTER"), user.getRegisterCol());
        
        Set<String> selfViewSet = new HashSet<String>();
        Set<String> durationSet = new HashSet<String>();
        Set<String> listenerSet = new HashSet<String>();
        Set<String> playCountSet = new HashSet<String>();
        Set<String> artistSet = new HashSet<String>();
        Set<String> tagSet = new HashSet<String>();
        
        for (UserTrack userTrack : userTracks) {
            
            if (userTrack.getListenCount() <= 23) {
                selfViewSet.add("few");
            } else if (userTrack.getListenCount() > 23 && userTrack.getListenCount() <= 100) {
                selfViewSet.add("normal");
            } else {
                selfViewSet.add("many");
            }
            
            Track track = dbUtil.getTrackInfo("track", userTrack.getTrackId());
            durationSet.add(track.getDuration());
            listenerSet.add(track.getListener());
            playCountSet.add(track.getPlayCount());
            artistSet.add(track.getArtistMbid());
            
            String[] tagArray = track.getTags().split(",");
            for (int i = 0; i < tagArray.length; i++) {
                String taggo = tagArray[i].trim();
                if (taggo != null && taggo.length() != 0) {
                    tagSet.add(taggo.replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                }
            }
        }
        
        for (String selfViewVal: selfViewSet)  {
            newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-SELF_VIEW"), model.createTypedLiteral(selfViewVal));
        }
        selfViewSet = null;
        
        for (String durationVal: durationSet)  {
            newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-DURATION"), model.createTypedLiteral(durationVal));
        }
        System.out.println(durationSet);
        durationSet = null;
        
        for (String listenerVal: listenerSet)  {
            newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-LISTENER"), model.createTypedLiteral(listenerVal));
        }
        System.out.println(listenerSet);
        listenerSet = null;
        
        for (String playCountVal: playCountSet)  {
            newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-PLAY_COUNT"), model.createTypedLiteral(playCountVal));
        }
        System.out.println(playCountSet);
        playCountSet = null;
        
        for (String artistVal: artistSet)  {
            newInd.addProperty(model.getObjectProperty(Constants.namespace + "HAS-ARTIST"), model.createTypedLiteral(artistVal));
        }
        System.out.println(artistSet);
        artistSet = null;
        
        for (String tag : tagSet) {
            ObjectProperty tagProp = model.getObjectProperty(Constants.namespace + tag);
            if (tagProp != null) {
                newInd.addProperty(tagProp, "true");
            }
        }
        System.out.println(tagSet);
        tagSet = null;
        
        return newInd;
    }
}
