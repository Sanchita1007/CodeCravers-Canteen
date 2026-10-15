import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL_SETTING = "CANTEEN_DB_URL";
    private static final String USER_SETTING = "CANTEEN_DB_USER";
    private static final String PASSWORD_SETTING = "CANTEEN_DB_PASSWORD";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String databaseUrl = getRequiredSetting(URL_SETTING);
        String databaseUser = getRequiredSetting(USER_SETTING);
        String databasePassword = getRequiredSetting(PASSWORD_SETTING);

        if (!databaseUrl.startsWith("jdbc:mysql://")) {
            throw new IllegalStateException(
                    URL_SETTING + " must start with jdbc:mysql://.");
        }

        // Connector/J registers its driver automatically when Maven loads it.
        return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
    }

    private static String getRequiredSetting(String settingName) {
        String settingValue = System.getenv(settingName);
        if (settingValue == null || settingValue.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Set the required database environment variable: " + settingName);
        }
        return settingValue;
    }
}
