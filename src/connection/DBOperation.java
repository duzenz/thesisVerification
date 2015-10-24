package connection;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBOperation {

    private static MysqlConnect conn = null;

    public List<Integer> getDistinctTrackIdsOfTable(String tablename) {
        conn = MysqlConnect.getDbCon();
        List<Integer> trackList = new ArrayList<Integer>();
        String sql = "select distinct(track_id) from " + tablename;
        try {
            ResultSet resultSet = conn.query(sql);
            while (resultSet.next()) {
                trackList.add(resultSet.getInt("track_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trackList;
    }
}
