package verifyCbr;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import connection.MysqlConnect;

public class VerifyCbrUserBased {

    private static MysqlConnect conn = null;
    public String OWL_FILE_URL = "data/cbr_training_";
    public String namespace = "http://www.example.com/ontologies/recommend.owl#";
    public String caseClassName = "RECOMMEND_CASE";
    public String trainingDataTable = "user_track_egitim_1_";
    public String usersTable = "lastfm_users";

    public static boolean ASC = true;
    public static boolean DESC = false;

    OntModel model = null;
    OntClass caseClass = null;
    Set<Property> propertySet = new HashSet<Property>();
    List<String> propertyList = new ArrayList<String>();
    List<String> instanceList = new ArrayList<String>();
    Map<String, Integer> distanceMap = new HashMap<String, Integer>();

    InputStream in = null;

    public VerifyCbrUserBased() {
        for (int i = 1; i <= 3; i++) {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            try {
                in = new FileInputStream(OWL_FILE_URL + i + ".owl");
            } catch (Exception e) {
                e.printStackTrace();
            }
            model.read(in, null);
            caseClass = (OntClass) model.getOntClass(namespace + caseClassName);
            setInstanceList(caseClass);
            verifyReasonings(i);
        }
    }

    public static void main(String[] args) {
        conn = MysqlConnect.getDbCon();
        new VerifyCbrUserBased();
    }

    public void setInstanceList(OntClass caseClass) {
        ExtendedIterator instances = caseClass.listInstances();
        while (instances.hasNext()) {
            instanceList.add(instances.next().toString());
        }
        System.out.println("instance list size: " + instanceList.size());
    }

    public void verifyReasonings(int index) {
        try {
            String sql = "select distinct(user_id) from test_user_track_" + index;
            ResultSet users = conn.query(sql);
            int counter = 0;
            while (users.next()) {
                int userId = users.getInt("user_id");
                counter++;
                
                sql = "select * from test_user_track_" + index + " where user_id = " + userId;
                ResultSet tracks = conn.query(sql);
                int trackCounter = 0;
                while (tracks.next()) {
                    int trackId = tracks.getInt("track_id");
                    int listenCount = tracks.getInt("listen_count");
                    trackCounter++;
                }
                System.out.println(userId + " => " + trackCounter);
                
                //clear distanceMap
                //create instance
                //compare with training instances
                //save to file with the found order
            }
            System.out.println(counter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void compareWithOtherIntances(Individual testIndividual) {
        setComparedPropertyList(testIndividual);
        for (String instance : instanceList) {
            Individual selectedIndividual = model.getIndividual(instance);//TODO static individual list
            //if (!selectedIndividual.equals(testIndividual)) {
            compareProperties(selectedIndividual, testIndividual);
            //}
        }
    }
    
    public void setComparedPropertyList(Individual individual) {
        propertySet.clear();
        StmtIterator props = individual.listProperties();
        while (props.hasNext()){
            Statement s = (Statement) props.next();
            if (s.getObject().isLiteral()) {
                propertySet.add(s.getPredicate());
            }
        }
    }

    public void compareProperties(Individual selectedIndividual, Individual testedIndividual) {
        int value = 0;
        
        for (Property p : propertySet) {
            String propertyLocalName = p.getLocalName(); 
            if ("HAS-COUNTRY".equals(propertyLocalName) || 
                "HAS-GENDER".equals(propertyLocalName) || 
                "HAS-REGISTER".equals(propertyLocalName) || 
                "HAS-AGE".equals(propertyLocalName)) {
                
                //multi valued properties comparison
                StmtIterator testedProps = testedIndividual.listProperties(p);
                StmtIterator comparedProps = selectedIndividual.listProperties(p);
                while (testedProps.hasNext()) {
                    Statement testedStatement = (Statement) testedProps.next();
                    Object testedObj = testedStatement.getLiteral().getValue();
                    while(comparedProps.hasNext()) {
                        Statement comparedStatement = (Statement) comparedProps.next();
                        Object comparedObj = comparedStatement.getLiteral().getValue();
                        if (comparedStatement.toString().length() > 0 && comparedObj.equals(testedObj)) {
                            value += 2;
                        }
                    }
                }
            } else {
                RDFNode selectedInstancePropVal = selectedIndividual.getPropertyValue(p);
                RDFNode newInstancePropVal = testedIndividual.getPropertyValue(p);
                if (selectedInstancePropVal != null && newInstancePropVal != null && selectedInstancePropVal.equals(newInstancePropVal)) {
                    value += 1;
                }
            }
        }
        distanceMap.put(selectedIndividual.getLocalName(), value);
    }
    
    public void printDistances(String trackId, int i) {
        System.out.println(trackId);
        Util util = new Util();
        Map<String, Integer> sortedMapDesc = util.sortByComparator(distanceMap, DESC);
        System.out.println(sortedMapDesc.size());
        System.out.println(sortedMapDesc);
        int counter = 0;
        Iterator it = sortedMapDesc.entrySet().iterator();
        boolean found = false;
        while (it.hasNext()) {
            counter++;
            Map.Entry pair = (Map.Entry) it.next();
            String key = pair.getKey() + "";
            String recommendTrackId = key.substring(1, key.length());
            if (recommendTrackId.equals(trackId)) {
                printResultsToFile(trackId + "," + "1" + "," + pair.getValue() + "," + counter, "data\\cbr_test_results_1.csv");
                found = true;
                break;
            }
        }
        if (!found) {
            printResultsToFile(trackId + "," + "0,0,0", "data\\cbr_test_results_1.csv");
        }
        System.out.println("===================");
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
    
    public Individual createNewIndividiual(int trackId, int listenCount) {
        OntClass obj = model.createClass(namespace + "RECOMMEND_CASE");
        Individual newInd = obj.createIndividual(namespace + "I" + trackId);
        ObjectProperty age = model.getObjectProperty(namespace + "HAS-AGE");
        ObjectProperty country = model.getObjectProperty(namespace + "HAS-COUNTRY");
        ObjectProperty gender = model.getObjectProperty(namespace + "HAS-GENDER");
        ObjectProperty register = model.getObjectProperty(namespace + "HAS-REGISTER");
        ObjectProperty selfView = model.getObjectProperty(namespace + "HAS-SELF_VIEW");
        ObjectProperty duration = model.getObjectProperty(namespace + "HAS-DURATION");
        ObjectProperty listener = model.getObjectProperty(namespace + "HAS-LISTENER");
        ObjectProperty playcount = model.getObjectProperty(namespace + "HAS-PLAY_COUNT");
        ObjectProperty artist = model.getObjectProperty(namespace + "HAS-ARTIST");
        
        String viewCol = "";
        if (listenCount <= 23) {
            viewCol = "few";
        } else if (listenCount > 23 && listenCount <= 100) {
            viewCol = "normal";
        } else {
            viewCol = "many";
        }
        newInd.addProperty(selfView, model.createTypedLiteral(viewCol));
        
        String sql = "select user_id from test_user_track_1 where track_id = " + trackId;
        
        try {
            String userIdsText = "";
            ResultSet userIds = conn.query(sql);
            while (userIds.next()) {
                if (!userIds.isLast()) {
                    userIdsText += userIds.getInt("user_id") + ",";
                } else {
                    userIdsText += userIds.getInt("user_id");
                }
            }
            
            sql = "select distinct(gender) from  lastfm_users where id in ( " + userIdsText + ")";
            ResultSet genderResult = conn.query(sql);
            while (genderResult.next()) {
                newInd.addProperty(gender, model.createTypedLiteral(genderResult.getString("gender")));
            }
            sql = "select distinct(country) from  lastfm_users where id in ( " + userIdsText + ")";
            ResultSet countryResult = conn.query(sql);
            while (countryResult.next()) {
                newInd.addProperty(country, model.createTypedLiteral(countryResult.getString("country")));
            }
            sql = "select distinct(age_col) from  lastfm_users where id in ( " + userIdsText + ")";
            ResultSet ageResult = conn.query(sql);
            while (ageResult.next()) {
                newInd.addProperty(age, model.createTypedLiteral(ageResult.getString("age_col")));
            }
            sql = "select distinct(register_col) from  lastfm_users where id in ( " + userIdsText + ")";
            ResultSet registerResult = conn.query(sql);
            while (registerResult.next()) {
                newInd.addProperty(register, model.createTypedLiteral(registerResult.getString("register_col")));
            }
            sql = "select * from track where id = " + trackId;
            ResultSet trackInfo = conn.query(sql);
            while (trackInfo.next())  {
                String durationCol = trackInfo.getString("duration");
                String listenerCol = trackInfo.getString("listener");
                String playCountCol = trackInfo.getString("play_count");
                String tagsCol = trackInfo.getString("tags");
                String artistMbidCol = trackInfo.getString("artist_mbid");
                
                newInd.addProperty(duration, model.createTypedLiteral(durationCol));
                newInd.addProperty(listener, model.createTypedLiteral(listenerCol));
                newInd.addProperty(playcount, model.createTypedLiteral(playCountCol));
                newInd.addProperty(artist, model.createTypedLiteral(artistMbidCol));
                
                String[] tagArray = tagsCol.split(",");
                for (int k = 0; k < tagArray.length; k++) {
                    ObjectProperty tagProp = model.getObjectProperty(namespace + tagArray[k].replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                    if (tagProp != null) {
                        newInd.addProperty(tagProp, "true");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return newInd;
    }
    
}