package lastfmData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import connection.MysqlConnect;

/**
 * Getting distinct track tags of system.
 */
public class DistinctTag {

    private static MysqlConnect conn = null;

    public static void main(String args[]) {
        
        conn = MysqlConnect.getDbCon();

        try {

            String sql = "select * from track";
            ResultSet track = conn.query(sql);

            Set<String> setA = new HashSet<String>();
            while (track.next()) {

                JsonObject responseObj = JSON.parse(track.getString("blob_content"));
                JsonObject trackObj = (JsonObject) responseObj.get("track");

                if (trackObj != null) {
                    if (trackObj.get("toptags").isObject()) {
                        JsonObject toptags = (JsonObject) trackObj.get("toptags");
                        if (toptags.get("tag").isArray()) {
                            JsonArray tagArray = (JsonArray) toptags.get("tag");
                            for (int i = 0; i < tagArray.size(); i++) {
                                JsonObject tag = (JsonObject) tagArray.get(i);
                                String tagString = tag.get("name").toString().replace("\"", "");
                                setA.add(tagString);
                            }
                        } else {
                            JsonObject tag = (JsonObject) toptags.get("tag");
                            String tagString = tag.get("name").toString().replace("\"", "");
                            setA.add(tagString);
                        }
                    }
                }
            }
            System.out.println(setA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        conn = null;
    }
}
