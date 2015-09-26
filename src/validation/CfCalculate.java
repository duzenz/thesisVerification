package validation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import connection.MysqlConnect;

public class CfCalculate {

	//recall precision graphic data 
	public static int foundLineThreshold = 10000;
	public static MysqlConnect conn = null;
	
	public static void main(String[] args) {
		conn = MysqlConnect.getDbCon();
		CfCalculate cf = new CfCalculate();
		
		for (int i = 1; i <= 3 ; i++) {
			cf.readCsvFile(i);
		}
	}

	public void readCsvFile(int i) {

		String csvFile = "data\\cf_test_results_" + i + ".csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			Set<Integer> trackIds = new TreeSet<Integer>();
			while ((line = br.readLine()) != null) {
				String[] row = line.split(cvsSplitBy);
				int trackId = Integer.parseInt(row[1]);
				int found = Integer.parseInt(row[2]);
				int foundedLine = Integer.parseInt(row[4]);
				if (found == 1 && foundedLine <= foundLineThreshold) {
					trackIds.add(trackId);
				}
			}
			
			int trainingDistinctTrack = getDistinctTrackCountOfTrainingTable(i);
			int testDistinctTrack = getDistinctTrackCountOfTestTable(i);
			
			System.out.println("Training Distinct Track Count : " + trainingDistinctTrack);
			System.out.println("Test Distinct Track Count : " + testDistinctTrack);
			System.out.println("Test found distinct track : " + trackIds.size());
			
			System.out.println((float) trackIds.size() / trainingDistinctTrack);
			System.out.println((float) trackIds.size() / testDistinctTrack);
			
			trackIds = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Done");
	}
	
	
	public int getDistinctTrackCountOfTrainingTable(int i) throws SQLException {
		String sql = "select count(distinct(track_id)) as cnt from training_user_track_" + i;
		System.out.println(sql);
		int trainingDistinct = 0;
		ResultSet count = conn.query(sql);
		while (count.next()) {
			trainingDistinct = count.getInt("cnt");
		}
		return trainingDistinct;
	}
	
	public int getDistinctTrackCountOfTestTable(int i) throws SQLException {
		String sql = "select count(distinct(track_id)) as cnt from test_user_track_" + i;
		System.out.println(sql);
		int trainingDistinct = 0;
		ResultSet count = conn.query(sql);
		while (count.next()) {
			trainingDistinct = count.getInt("cnt");
		}
		return trainingDistinct;
	}
}
