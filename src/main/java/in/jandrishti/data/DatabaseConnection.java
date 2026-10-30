package in.jandrishti.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/jandrishti?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "var123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[JanDrishti Database] MySQL JDBC Driver not found in classpath.");
        }
    }

    private DatabaseConnection() {
        // Utility class
    }

    private static String resolvePassword() {
        String sysProp = System.getProperty("db.password");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String envVar = System.getenv("DB_PASSWORD");
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }
        return DEFAULT_PASSWORD;
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", DEFAULT_URL);
        String user = System.getProperty("db.user", DEFAULT_USER);
        String password = resolvePassword();

        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getRootConnection() throws SQLException {
        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = System.getProperty("db.user", DEFAULT_USER);
        String password = resolvePassword();

        return DriverManager.getConnection(baseUrl, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn != null && !conn.isClosed();
            if (valid) {
                System.out.println("[JanDrishti Database] SUCCESS: Connected to MySQL database server at localhost:3306.");
            }
            return valid;
        } catch (SQLException e) {
            System.out.println("[JanDrishti Database] JDBC connection test note: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
