package lastfmData;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Statement;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class Tag {

    public static Connection baglanti = null;
    public static Statement statement = null;

    public static void main(String[] args) {
        try {
            URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=tag.getTopTags&api_key=465a9ad86bad93eff26b316a993ea6ca&format=json");
            InputStream is = url.openStream();
            JsonObject responseObj = Json.createReader(is).readObject();
            JsonObject alltags = (JsonObject) responseObj.get("toptags");

            String hede = "";
            if (alltags != null) {
                System.out.println(alltags);
                JsonArray tags = (JsonArray) alltags.get("tag");
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        JsonObject tag = (JsonObject) tags.get(i);
                        System.out.println(tag.get("name"));
                        hede += tag.get("name") + ",";
                        // getSimilarTags(tag.get("name").toString());
                    }
                }
            }
            System.out.println(hede);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public static void getSimilarTags(String tagName) {
        URL newUrl;
        try {
            System.out.println("http://ws.audioscrobbler.com/2.0/?method=tag.getsimilar&tag=" + URLEncoder.encode(tagName) + "&api_key=465a9ad86bad93eff26b316a993ea6ca&format=json");
            newUrl = new URL("http://ws.audioscrobbler.com/2.0/?method=tag.getsimilar&tag=" + URLEncoder.encode(tagName) + "&api_key=465a9ad86bad93eff26b316a993ea6ca&format=json");
            InputStream newIn = newUrl.openStream();
            JsonObject newRes = Json.createReader(newIn).readObject();
            System.out.println(newRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
