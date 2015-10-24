package lastfmData;

import java.sql.ResultSet;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import connection.MysqlConnect;


public class TrackInfo {

    private static MysqlConnect conn = null;
    
    public static void main(String[] args) {
        conn = MysqlConnect.getDbCon();
        try {
            ResultSet resultSet = conn.query("select * from track where id > 660000");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                String trackInfo = resultSet.getString("blob_content");
                JsonObject responseObj = JSON.parse(trackInfo);
                JsonObject trackObj = (JsonObject) responseObj.get("track");
                
                String durationCol = "";
                String listenersCol = "";
                String playCountCol = "";
                String toptagsCol = "";
                
                String trackUrlCol = "";
                String artistUrlCol = "";
                String artistNameCol = "";
                String artistMbidCol = "";
                String albumUrlCol = "";
                String albumMbidCol = "";
                String albumTitleCol = "";
                
                if (trackObj != null) {
                    JsonValue trackDuration = trackObj.get("duration").getAsString();
                    if (trackDuration.toString().length() > 0) {
                        int milliseconds = (int) Double.parseDouble(trackDuration.toString().replace("\"", ""));
                        if (milliseconds < 120000) {
                            durationCol = "short";
                        } else if (milliseconds >= 120000 && milliseconds <= 300000) {
                            durationCol = "normal";
                        } else {
                            durationCol = "long";
                        }
                    }
                    JsonValue listenersCount = trackObj.get("listeners").getAsString();
                    if (listenersCount.toString().length() > 0) {
                        int listenersCountVal = Integer.parseInt(listenersCount.toString().replace("\"", ""));
                        if (listenersCountVal < 500) {
                            listenersCol = "few";
                        } else if (listenersCountVal >= 500 && listenersCountVal < 3000) {
                            listenersCol = "normal";
                        } else {
                            listenersCol = "many";
                        }
                    }
                    
                    JsonValue playCount = trackObj.get("playcount").getAsString();
                    if (playCount.toString().length() > 0) {
                        String number = playCount.toString().replace("\"", "");
                        int playCountVal = Integer.parseInt(number.length() <= 0 ? "0" : number);
                        if (playCountVal < 5000) {
                            playCountCol = "few";
                        } else if (playCountVal >= 5000 && playCountVal < 15000) {
                            playCountCol = "normal";
                        } else {
                            playCountCol = "many";
                        }
                    }
                    if (trackObj.get("toptags") != null && trackObj.get("toptags").isObject()) {
                        JsonObject toptags = (JsonObject) trackObj.get("toptags");
                        if (toptags.get("tag").isArray()) {
                            JsonArray tagArray = (JsonArray) toptags.get("tag");
                            for (int i = 0; i < tagArray.size(); i++) {
                                JsonObject tag = (JsonObject) tagArray.get(i);
                                toptagsCol += tag.get("name").toString().replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", "") + ",";
                            }
                        } else {
                            JsonObject tag = (JsonObject) toptags.get("tag");
                            toptagsCol += tag.get("name").toString().replace("\"", "").replaceAll("\\s+", "_").replaceAll("#", "");
                        }
                    }
                    
                    if (trackObj.get("artist") != null && trackObj.get("artist").isObject()) {
                        JsonObject artistObj = (JsonObject) trackObj.get("artist");
                        JsonValue artistName = artistObj.get("name").getAsString();
                        JsonValue artistMbid = artistObj.get("mbid").getAsString();
                        JsonValue artistUrl = artistObj.get("url").getAsString();
                        artistNameCol = artistName.toString().replace("\"", "");
                        artistMbidCol = artistMbid.toString().replace("\"", "");
                        artistUrlCol = artistUrl.toString().replace("\"", "");
                    }
                    if (trackObj.get("album") != null && trackObj.get("album").isObject()) {
                        JsonObject albumObj = (JsonObject) trackObj.get("album");
                        JsonValue albumTitle = albumObj.get("title").getAsString();
                        JsonValue albumMbid = albumObj.get("mbid").getAsString();
                        JsonValue albumUrl = albumObj.get("url").getAsString();
                        albumTitleCol = albumTitle.toString().replace("\"", "");
                        albumMbidCol = albumMbid.toString().replace("\"", "");
                        albumUrlCol = albumUrl.toString().replace("\"", "");
                    }
                    
                    JsonValue trackUrl = trackObj.get("url").getAsString();
                    trackUrlCol = trackUrl.toString().replace("\"", "");
                    
                    String sql = "update track set duration = '" + durationCol + "', "
                            + "listener = '" + listenersCol + "', "
                            + "play_count = '" + playCountCol + "', "
                            + "tags = '" + toptagsCol.replaceAll("'", "") + "', " 
                            //+ "track_url = '" + trackUrlCol.replaceAll("'", "") + "', "
                            //+ "artist_name = '" + artistNameCol.replaceAll("'", "") + "', "
                            + "artist_mbid = '" + artistMbidCol + "', "
                            //+ "artist_url = '" + artistUrlCol.replaceAll("'", "") + "', "
                            //+ "album_title = '" + albumTitleCol.replaceAll("'", "") + "', "
                            + "album_mbid = '" + albumMbidCol + "' "
                            + "where id = " + resultSet.getInt("id") + ";";
                            //+ "album_url = '" + albumUrlCol.replaceAll("'", "") + "' where id = " + resultSet.getInt("id") + ";";
                    System.out.println(sql);
                    conn.insert(sql);
                } else {
                    System.out.println("==================");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
