package lastfmApi;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

public class SimilarTag {

    public static void main(String args[]) {

        //List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
        //System.out.println(items.size());
        /*
        int i = 0;
        URL newUrl;
        for (String s : items) {
            try {
                newUrl = new URL("http://ws.audioscrobbler.com/2.0/?method=tag.getsimilar&tag=" + URLEncoder.encode(s) + "&api_key=465a9ad86bad93eff26b316a993ea6ca&format=json");
                //TODO create prop
                InputStream newIn = newUrl.openStream();
                JsonObject responseObj = JSON.parse(newIn);
                if (responseObj.get("similartags").isObject()) {
                    JsonObject similartags = (JsonObject) responseObj.get("similartags");
                    if (similartags.get("tag").isArray()) {
                        JsonArray tags = (JsonArray) similartags.get("tag");
                        for (int j = 0; j < tags.size(); j++) {
                            JsonObject tag = (JsonObject) tags.get(j);
                            String tagString = tag.get("name").toString().replace("\"", "");
                            System.out.println(tagString);
                            //TODO create sub prop
                        }
                    } else {
                        //kendi tipinden bir object property yarat
                        System.out.println("array yok kendisi");
                    }
                } else {
                    
                    System.out.println("Object de�il");
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i > 10) {
                break;
            }
        }
        */

    }
}