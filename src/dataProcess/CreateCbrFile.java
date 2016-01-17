package dataProcess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import model.CbrUtil;
import util.Constants;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import connection.DBOperation;

public class CreateCbrFile {

    private CbrUtil cbrUtil;
    private DBOperation dbUtil;

    public CreateCbrFile() {
        cbrUtil = new CbrUtil();
        dbUtil = new DBOperation();
        createCbrDataTable(Constants.cbrDataStartDate, Constants.cbrDataFinishDate);
        runOperations();
    }

    public void runOperations() {
        OntModel model = cbrUtil.loadCbrModel(Constants.noInstanceOwlFileName);
        List<Integer> trackIds = dbUtil.getDistinctColumnValuesOfTable(Constants.cbrUserTrackTableName, "track_id");
        System.out.println("model loaded");
        System.out.println(trackIds.size());

        int counter = 0;
        for (int trackId : trackIds) {
            counter++;
            String userIdText = getListenersOfTrack(trackId);
            addIndividualToModel(model, userIdText, trackId);
            //System.out.println("added : " + trackId);
            System.out.println(counter);
            if (counter % 10000 == 0) {
                cbrUtil.saveIntances(model, Constants.cbrInstanceFile);
                model = null;
                model = cbrUtil.loadCbrModel(Constants.cbrInstanceFile);
                System.out.println("model loaded again");
            }
        }
    }

    public void addIndividualToModel(OntModel model, String userIdText, int trackId) {
        OntClass obj = model.createClass(Constants.namespace + "RECOMMEND_CASE");
        Individual instance = obj.createIndividual(Constants.namespace + "I" + trackId);

        try {
            String sql = "select distinct(gender) from  lastfm_users where id in ( " + userIdText + ")";
            ResultSet genderResult = dbUtil.getConn().query(sql);
            while (genderResult.next()) {
                instance.addProperty(cbrUtil.gender, model.createTypedLiteral(genderResult.getString("gender")));
            }
            
            sql = "select distinct(country) from  lastfm_users where id in ( " + userIdText + ")";
            ResultSet countryResult = dbUtil.getConn().query(sql);
            while (countryResult.next()) {
                instance.addProperty(cbrUtil.country, model.createTypedLiteral(countryResult.getString("country")));
            }
            
            sql = "select distinct(age_col) from  lastfm_users where id in ( " + userIdText + ")";
            ResultSet ageResult = dbUtil.getConn().query(sql);
            while (ageResult.next()) {
                instance.addProperty(cbrUtil.age, model.createTypedLiteral(ageResult.getString("age_col")));
            }
            
            sql = "select distinct(register_col) from  lastfm_users where id in ( " + userIdText + ")";
            ResultSet registerResult = dbUtil.getConn().query(sql);
            while (registerResult.next()) {
                instance.addProperty(cbrUtil.register, model.createTypedLiteral(registerResult.getString("register_col")));
            }
            
            sql = "select * from track where id = " + trackId;
            ResultSet trackInfo = dbUtil.getConn().query(sql);
            while (trackInfo.next())  {
                String[] tagArray = trackInfo.getString("tags").split(",");
                for (int k = 0; k < tagArray.length; k++) {
                    ObjectProperty tagProp = model.getObjectProperty(Constants.namespace + tagArray[k].replace("\"", "").replaceAll("-", "_").replaceAll("\\s+", "_").replaceAll("#", ""));
                    if (tagProp != null) {
                        instance.addProperty(tagProp, "true");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getListenersOfTrack(int trackId) {
        String userIdsText = "";
        try {
            String sql = "select user_id from " + Constants.cbrUserTrackTableName + " where track_id = " + trackId;
            ResultSet userIds = dbUtil.getConn().query(sql);
            while (userIds.next()) {
                if (!userIds.isLast()) {
                    userIdsText += userIds.getInt("user_id") + ",";
                } else {
                    userIdsText += userIds.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIdsText;
    }

    public static void main(String args[]) {
        new CreateCbrFile();
    }

    public void createCbrDataTable(String startdate, String finishdate) {
        dbUtil.truncateTable(Constants.cbrDataTableName);
        dbUtil.truncateTable(Constants.cbrUserTrackTableName);
        System.out.println(Constants.cbrDataTableName + " table deleted");
        System.out.println(Constants.cbrUserTrackTableName + " table deleted");
        try {
            dbUtil.getConn().insert("create table " + Constants.cbrDataTableName + " (SELECT * FROM " + Constants.rawTable + " where time between '" + startdate + "' and '" + finishdate + "' )");
            System.out.println(Constants.cbrDataTableName + " table created");
            dbUtil.getConn().insert("create table " + Constants.cbrUserTrackTableName + " SELECT user_id, track_id, count(track_id) as listen_count FROM " + Constants.cbrDataTableName
                    + " group by user_id, track_id " + " having listen_count >= " + Constants.listenThreshold);
            System.out.println(Constants.cbrUserTrackTableName + " table created");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
