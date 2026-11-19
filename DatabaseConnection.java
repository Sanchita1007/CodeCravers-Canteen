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

        // Explicitly load the MySQL JDBC driver.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found.");
            throw new SQLException("MySQL JDBC driver not found.", e);
        }

        // Establish database connection.
        try {
            return DriverManager.getConnection(
                    databaseUrl, databaseUser, databasePassword);
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());

            if (e.getCause() != null) {
                System.err.println("Cause: " 
                        + e.getCause().getClass().getName());
            }

            throw e;
        }
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
