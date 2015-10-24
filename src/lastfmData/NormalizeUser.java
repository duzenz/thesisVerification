package lastfmData;

import java.sql.ResultSet;

import connection.MysqlConnect;

public class NormalizeUser {

    private static MysqlConnect conn = null;

    public static void main(String[] args) {
        conn = MysqlConnect.getDbCon();
        try {
            ResultSet resultSet = conn.query("select * from lastfm_users");
            while (resultSet.next()) {

                System.out.println("id: " + resultSet.getInt("id"));
                int age = resultSet.getInt("age");
                String register = resultSet.getString("registered");

                String ageCol = "";
                if (age > 0) {
                    if (age <= 17) {
                        ageCol = "0-17";
                    } else if (age > 17 && age <= 24) {
                        ageCol = "18-24";
                    } else if (age > 24 && age <= 30) {
                        ageCol = "25-30";
                    } else if (age > 30 && age <= 40) {
                        ageCol = "31-40";
                    } else if (age > 40 && age <= 50) {
                        ageCol = "41-50";
                    } else {
                        ageCol = "51-100";
                    }
                }

                String registerColumn = "";
                if (register != null && register.length() > 0) {
                    registerColumn = register.substring(register.length() - 4);
                }

                String sql = "update lastfm_users set age_col = '" + ageCol + "', register_col = '" + registerColumn + "' where id = " + resultSet.getInt("id");
                conn.insert(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
