package dataProcess;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import connection.MysqlConnect;

public class CreateCbrInstanceFile {

    private static MysqlConnect conn = null;
    public static String inputFileName = "file:src/dataProcess/recommend.owl";
    public static String camNS = "http://www.example.com/ontologies/recommend.owl#";
    public static String usersTable = "lastfm_users";
    public static String userTrackTable = "training_user_track_";
    public static String outputFile = "cbr_training_";
    public static String cbrTableName = "cbr_training_table_";
    public static final int testTableCount = 3;
    
    public static OntModel m;
    public static ObjectProperty age;
    public static ObjectProperty country;
    public static ObjectProperty gender;
    public static ObjectProperty register;
    public static ObjectProperty listenWay;
    public static ObjectProperty selfView;
    public static ObjectProperty track;
    public static ObjectProperty duration;
    public static ObjectProperty listener;
    public static ObjectProperty playcount;
    public static ObjectProperty tag;
    public static ObjectProperty artist;
    
    public OntModel loadOntologyModel() {
        OntDocumentManager mgr = new OntDocumentManager();
        OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);
        s.setDocumentManager(mgr);
        m = ModelFactory.createOntologyModel(s, null);
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }
        m.read(in, "");
        return m;
    }
    
    public static void main(String args[]) {
        CreateCbrInstanceFile ins = new CreateCbrInstanceFile();
        conn = MysqlConnect.getDbCon();
        m = ins.loadOntologyModel();
        age = m.getObjectProperty(camNS + "HAS-AGE");
        country = m.getObjectProperty(camNS + "HAS-COUNTRY");
        gender = m.getObjectProperty(camNS + "HAS-GENDER");
        register = m.getObjectProperty(camNS + "HAS-REGISTER");
        listenWay = m.getObjectProperty(camNS + "HAS-LISTEN_WAY");
        selfView = m.getObjectProperty(camNS + "HAS-SELF_VIEW");
        track = m.getObjectProperty(camNS + "HAS-TRACK");
        duration = m.getObjectProperty(camNS + "HAS-DURATION");
        listener = m.getObjectProperty(camNS + "HAS-LISTENER");
        playcount = m.getObjectProperty(camNS + "HAS-PLAY_COUNT");
        artist = m.getObjectProperty(camNS + "HAS-ARTIST");
        tag = m.getObjectProperty(camNS + "HAS-TAG"); 
        //create training dbs
        ins.createTrainingTables();
        //create instance files
        ins.createInstanceFiles();
    }
    
    public void createTrainingTables() {
        for (int i = 1; i <= testTableCount; i++) {
            try {
                String sql = "drop table IF EXISTS " + cbrTableName + i;
                conn.insert(sql);
                
                sql = "create table " + cbrTableName + i + " (select track_id, sum(listen_count) as listen_count from " + 
                        userTrackTable + i + "  group by track_id having listen_count >= 10 and count(user_id) >= 10)";
                conn.insert(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void createInstanceFiles() {
        for (int i = 1; i <= testTableCount; i++) {
            String sql = "select * from " + cbrTableName + i;
            try {
                ResultSet results = conn.query(sql);
                int counter = 0;
                while (results.next()) {
                    int trackId = results.getInt("track_id");
                    int listenCount = results.getInt("listen_count");
                    OntClass obj = m.createClass(camNS + "RECOMMEND_CASE");
                    Individual instance = obj.createIndividual(camNS + "I" + trackId);
                    counter++;
                    
                    String viewCol = "";
                    if (listenCount <= 23) {
                        viewCol = "few";
                    } else if (listenCount > 23 && listenCount <= 100) {
                        viewCol = "normal";
                    } else {
                        viewCol = "many";
                    }
                    instance.addProperty(selfView, m.createTypedLiteral(viewCol));
                    
                    
                    sql = "select user_id from " + userTrackTable + i + " where track_id = " + trackId;
                    ResultSet userIds = conn.query(sql);
                    String userIdsText = "";
                    while (userIds.next()) {
                        if (!userIds.isLast()) {
                            userIdsText += userIds.getInt("user_id") + ",";
                        } else {
                            userIdsText += userIds.getInt("user_id");
                        }
                    }
                    System.out.println(userIdsText);
                    
                    //System.out.println(trackId);
                    sql = "select distinct(gender) from  lastfm_users where id in ( " + userIdsText + ")";
                    ResultSet genderResult = conn.query(sql);
                    while (genderResult.next()) {
                        instance.addProperty(gender, m.createTypedLiteral(genderResult.getString("gender")));
                    }
                    sql = "select distinct(country) from  lastfm_users where id in ( " + userIdsText + ")";
                    ResultSet countryResult = conn.query(sql);
                    while (countryResult.next()) {
                        instance.addProperty(country, m.createTypedLiteral(countryResult.getString("country")));
                    }
                    sql = "select distinct(age_col) from  lastfm_users where id in ( " + userIdsText + ")";
                    ResultSet ageResult = conn.query(sql);
                    while (ageResult.next()) {
                        instance.addProperty(age, m.createTypedLiteral(ageResult.getString("age_col")));
                    }
                    sql = "select distinct(register_col) from  lastfm_users where id in ( " + userIdsText + ")";
                    ResultSet registerResult = conn.query(sql);
                    while (registerResult.next()) {
                        instance.addProperty(register, m.createTypedLiteral(registerResult.getString("register_col")));
                    }
                    sql = "select * from track where id = " + trackId;
                    ResultSet trackInfo = conn.query(sql);
                    while (trackInfo.next())  {
                        String durationCol = trackInfo.getString("duration");
                        String listenerCol = trackInfo.getString("listener");
                        String playCountCol = trackInfo.getString("play_count");
                        String tagsCol = trackInfo.getString("tags");
                        String artistMbidCol = trackInfo.getString("artist_mbid");
                        
                        instance.addProperty(duration, m.createTypedLiteral(durationCol));
                        instance.addProperty(listener, m.createTypedLiteral(listenerCol));
                        instance.addProperty(playcount, m.createTypedLiteral(playCountCol));
                        instance.addProperty(artist, m.createTypedLiteral(artistMbidCol));
                        
                        String[] tagArray = tagsCol.split(",");
                        for (int k = 0; k < tagArray.length; k++) {
                            ObjectProperty tagProp = m.getObjectProperty(camNS + tagArray[k].replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                            if (tagProp != null) {
                                instance.addProperty(tagProp, "true");
                            }
                        }
                    }
                    System.out.println(counter);
                }
                saveIntances(i);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveIntances(int i) {
        FileWriter out;
        try {
            out = new FileWriter(outputFile + i + ".owl");
            m.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
