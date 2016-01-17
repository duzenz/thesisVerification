package connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.*;

public class DBOperation {

    private static MysqlConnect conn = null;

    public DBOperation() {
        conn = MysqlConnect.getDbCon();
    }
    
    public MysqlConnect getConn() {
        return conn;
    }

    // TODO this function is only works for int typed columns
    public List<Integer> getDistinctColumnValuesOfTable(String tablename, String columnName) {
        List<Integer> distinctList = new ArrayList<>();
        String sql = "select distinct(" + columnName + ") from " + tablename + " order by " + columnName;
        try {
            ResultSet resultSet = conn.query(sql);
            while (resultSet.next()) {
                distinctList.add(resultSet.getInt(columnName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distinctList;
    }

    public void truncateTable(String tablename) {
        try {
            String sql = "drop table IF EXISTS " + tablename;
            conn.insert(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserInfo(int userId) {
        String sql = "select * from lastfm_users where id = " + userId;
        User user = new User();
        try {
            ResultSet resultSet = conn.query(sql);
            resultSet.first();
            user.setAge(resultSet.getInt("age"));
            user.setAgeCol(resultSet.getString("age_col"));
            user.setCountry(resultSet.getString("country"));
            user.setGender(resultSet.getString("gender"));
            user.setRegisterCol(resultSet.getString("register_col"));
            user.setRegistered(resultSet.getString("registered"));
            user.setUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public List<UserTrack> getUserTracks(String tableName, int userId) {
        String sql = "select * from " + tableName + " where user_id = " + userId;
        List<UserTrack> userTracks = new ArrayList<>();
        try {
            ResultSet rs = conn.query(sql);
            while (rs.next()) {
                UserTrack ut = new UserTrack();
                ut.setListenCount(rs.getInt("listen_count"));
                ut.setTrackId(rs.getInt("track_id"));
                ut.setUserId(userId);
                ut.setTime(rs.getString("time"));
                //ut.setRating(rs.getFloat("rating"));
                userTracks.add(ut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userTracks;
    }

    public List<UserTrack> getUserTracks(String tableName, int userId, int ratingThreshold) {
        String sql = "select * from " + tableName + " where user_id = " + userId + " order by listen_count desc, time asc limit 10";
        List<UserTrack> userTracks = new ArrayList<>();
        try {
            ResultSet rs = conn.query(sql);
            while (rs.next()) {
                UserTrack ut = new UserTrack();
                ut.setListenCount(rs.getInt("listen_count"));
                ut.setTrackId(rs.getInt("track_id"));
                ut.setUserId(userId);
                ut.setTime(rs.getString("time"));
                //ut.setRating(rs.getFloat("rating"));
                userTracks.add(ut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userTracks;
    }

    public Track getTrackInfo(String tablename, int trackId) {
        String sql = "select * from " + tablename + " where id = " + trackId;
        Track t = new Track();
        try {
            ResultSet rs = conn.query(sql);
            rs.next();
            t.setArtistMbid(rs.getString("artist_mbid"));
            t.setArtistName(rs.getString("artist_name"));
            t.setDuration(rs.getString("duration"));
            t.setListener(rs.getString("listener"));
            t.setPlayCount(rs.getString("play_count"));
            t.setTags(rs.getString("tags"));
            t.setTrackId(trackId);
            t.setTrackMbid(rs.getString("track_id"));
            t.setTrackName(rs.getString("track_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public void createCfTrainingFile(String columns, String filename, String trainingTable, int userId) throws SQLException {
        conn.query("SELECT " + columns + " into OUTFILE  '" + filename + "" + "' FIELDS TERMINATED BY ',' FROM " + trainingTable + " where user_id != " + userId + " order by " + columns);
    }
}
