package dataProcess;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class CreateTimeBasedInstanceFile {

    public static String cbrTrainingTableName = "training_";
    public static String cbrTempTableName = "cbr_table";
    public static String cbrTempUserTrackTableName = "cbr_user_track";
    public static String usersTable = "lastfm_users";
    public static String trackTable = "track";
    public static String inputFileName = "file:src/recommend.owl";
    public static String camNS = "http://www.example.com/ontologies/recommend.owl#";

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

    public int trainingTableCount = 1;
    public int monthPartitionCount = 3;
    private static MysqlConnect conn = null;
    public static int partition = 0;

    public static void setObjectProps() {
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
    }

    public static void main(String args[]) {
        String previousDate = "";
        String nextDate = "";
        m = loadOntologyModel();
        setObjectProps();
        CreateTimeBasedInstanceFile cbrIns = new CreateTimeBasedInstanceFile();
        conn = MysqlConnect.getDbCon();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate, finishDate;

        for (int i = 1; i <= cbrIns.trainingTableCount; i++) {
            System.out.println(cbrTrainingTableName + i);

            try {
                String sql = "select min(time) as minTime, max(time) as maxTime from " + cbrTrainingTableName + i;
                ResultSet timeRs = conn.query(sql);
                
                partition = 0;
                while (timeRs.next()) {
                    partition++;
                    String minTime = timeRs.getString("minTime");
                    String maxTime = timeRs.getString("maxTime");
                    startDate = df.parse(minTime);
                    finishDate = df.parse(maxTime);
                    String startDateString = df.format(startDate);
                    String finishDateString = df.format(finishDate);
                    System.out.println("startdate : " + startDateString);
                    System.out.println("finishdate : " + finishDateString);

                    Calendar cal = Calendar.getInstance();
                    Calendar finishTime = Calendar.getInstance();
                    cal.setTime(startDate);
                    finishTime.setTime(finishDate);
                    while (cal.getTime().compareTo(finishTime.getTime()) <= 0) {
                        previousDate = df.format(cal.getTime());
                        cal.add(Calendar.MONTH, cbrIns.monthPartitionCount);
                        nextDate = df.format(cal.getTime());
                        cbrIns.createTempTables(previousDate, nextDate);

                        cbrIns.createCbrData(i);
                        FileWriter out;
                        try {
                            out = new FileWriter("recommend.owl");
                            m.write(out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cbrIns.deleteTempTables();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("bitti");
        }
    }

    public void createTempTables(String previousDate, String nextDate) throws SQLException {
        System.out.println("ilk : " + previousDate + " son : " + nextDate);
        String sql = "create table cbr_table select * from training_1 where time between '" + previousDate + "' and '" + nextDate + "'";
        conn.insert(sql);
        sql = "create table cbr_user_track select user_id, track_id, count(track_id) as listen_count from cbr_table group by user_id, track_id order by user_id, track_id";
        conn.insert(sql);
    }

    public void deleteTempTables() throws SQLException {
        String sql = "drop table " + CreateTimeBasedInstanceFile.cbrTempTableName;
        conn.insert(sql);
        sql = "drop table " + CreateTimeBasedInstanceFile.cbrTempUserTrackTableName;
        conn.insert(sql);
    }

    public void createCbrData(int i) throws SQLException {
        String sql = "select distinct(user_id) from " + CreateTimeBasedInstanceFile.cbrTempUserTrackTableName; // + " limit 2";
        ResultSet resultSet = conn.query(sql);
        while (resultSet.next()) {
            int userId = resultSet.getInt("user_id");
            System.out.println(userId);
            createIndividualofUser(userId);
        }
    }

    public void createIndividualofUser(int userId) throws SQLException {
        String sql = "select track_id, listen_count from " + CreateTimeBasedInstanceFile.cbrTempUserTrackTableName + " where user_id = " + userId;
        ResultSet trackResult = conn.query(sql);
       
        
        OntClass obj = m.createClass(camNS + "RECOMMEND_CASE");
        Individual instance = obj.createIndividual(camNS + "I" + userId + "_" + partition);
        
        sql = "select * from " + usersTable + " where id = " + userId;
        ResultSet userResult = conn.query(sql);
        
        while (userResult.next()) {
            instance.addProperty(country, m.createTypedLiteral(userResult.getString("country")));
            instance.addProperty(gender, m.createTypedLiteral(userResult.getString("gender")));
            instance.addProperty(age, m.createTypedLiteral(userResult.getString("age_col")));
            instance.addProperty(listenWay, m.createTypedLiteral("internet"));
            instance.addProperty(register, m.createTypedLiteral(userResult.getString("register_col")));
        }
        
        Set<Integer> trackSet = new HashSet<Integer>();
        Set<String> selfViewSet = new HashSet<String>();
        Set<String> durationSet = new HashSet<String>();
        Set<String> listenerSet = new HashSet<String>();
        Set<String> playCountSet = new HashSet<String>();
        Set<String> artistSet = new HashSet<String>();
        Set<String> tagSet = new HashSet<String>();
        
        while (trackResult.next()) {
            trackSet.add(trackResult.getInt("track_id"));
            int listenCount = trackResult.getInt("listen_count");
            if (listenCount <= 23) {
                selfViewSet.add("few");
            } else if (listenCount > 23 && listenCount <= 100) {
                selfViewSet.add("normal");
            } else {
                selfViewSet.add("many");
            }
            
            ResultSet trackInfoRs = conn.query("select * from track where id = " + trackResult.getInt("track_id"));
            while (trackInfoRs.next()) {
                durationSet.add(trackInfoRs.getString("duration"));
                listenerSet.add(trackInfoRs.getString("listener"));
                playCountSet.add(trackInfoRs.getString("play_count"));
                artistSet.add(trackInfoRs.getString("artist_mbid"));
                
                String tags = trackInfoRs.getString("tags");
                String[] tagArray = tags.split(",");
                for (int i = 0; i < tagArray.length; i++) {
                    //ObjectProperty tagProp = m.getObjectProperty(camNS + tagArray[i].replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                    //if (tagProp != null) {
                        tagSet.add(tagArray[i].replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", ""));
                    //}
                }
            }
        }
        
        String s = "";
        for (int trackVal: trackSet) {
            s += trackVal + ",";
        }
        instance.addProperty(track, m.createTypedLiteral(s));
        trackSet = null;
        
        s = "";
        for (String selfViewVal: selfViewSet)  {
            s += selfViewVal + ",";
        }
        instance.addProperty(selfView, m.createTypedLiteral(s));
        selfViewSet = null;
        
        s = "";
        for (String durationVal: durationSet)  {
            s += durationVal + ",";
        }
        instance.addProperty(duration, m.createTypedLiteral(s));
        durationSet = null;
        
        s = "";
        for (String listenerVal: listenerSet)  {
            s += listenerVal + ",";
        }
        instance.addProperty(listener, m.createTypedLiteral(s));
        listenerSet = null;
        
        s = "";
        for (String playCountVal: playCountSet)  {
            s += playCountVal + ",";
        }
        instance.addProperty(playcount, m.createTypedLiteral(s));
        playCountSet = null;
        
        s = "";
        for (String artistVal: artistSet)  {
            s += artistVal + ",";
        }
        instance.addProperty(artist, m.createTypedLiteral(s));
        artistSet = null;
        
        s = "";
        for (String tagVal: tagSet) {
            s += tagVal + ",";
        }
        instance.addProperty(tag, s);
        tagSet = null;
    }

    public static OntModel loadOntologyModel() {
        OntDocumentManager mgr = new OntDocumentManager();
        OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);
        s.setDocumentManager(mgr);
        OntModel model = ModelFactory.createOntologyModel(s, null);
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + inputFileName + " not found");
        }
        model.read(in, "");
        return model;
    }
}
