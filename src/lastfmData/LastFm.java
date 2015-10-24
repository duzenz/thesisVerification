package lastfmData;

import java.io.InputStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.json.*;

public class LastFm {

	public static Connection baglanti = null;
	public static Statement statement = null;

	public static void main(String[] args) {
		Boolean tryAgain = true;
		while (tryAgain) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				baglanti = DriverManager.getConnection(
						"jdbc:mysql://localhost/training", "root", "");

				statement = baglanti.createStatement();

				ResultSet res = statement
						.executeQuery("SELECT * FROM track where is_requested_blob != 1");
				while (res.next()) {
					System.out.println(res.getInt("id"));
					URL url = new URL(
							"http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=465a9ad86bad93eff26b316a993ea6ca&mbid="
									+ res.getString("track_id")
									+ "&format=json");
					InputStream is = url.openStream();
					JsonObject responseObj = Json.createReader(is).readObject();
					System.out.println(responseObj.toString());

					String query = "update track set blob_content = ?, is_requested_blob = 1 where id = ?";
					PreparedStatement preparedStmt = baglanti
							.prepareStatement(query);
					Blob blob = baglanti.createBlob();
					blob.setBytes(1, responseObj.toString().getBytes());
					preparedStmt.setBlob(1, blob);
					preparedStmt.setLong(2, res.getLong("id"));

					preparedStmt.executeUpdate();

				}
				tryAgain = false;

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("NOK");
				tryAgain = true;
			}
		}
	}

}
