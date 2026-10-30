package in.jandrishti.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public final class DatabaseVerifier {

    public static void verifyAll() {
        System.out.println("=== JanDrishti MySQL Verification Suite ===");
        DatabaseInitializer.initializeDatabase();

        // 1. Connection Test
        boolean connSuccess = DatabaseConnection.testConnection();

        // 2. Database Test
        boolean dbSuccess = false;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
            if (rs.next()) {
                String dbName = rs.getString(1);
                dbSuccess = "jandrishti".equalsIgnoreCase(dbName);
                System.out.println("[JanDrishti Verifier] Active Database: " + dbName);
            }
        } catch (Exception e) {
            System.err.println("[JanDrishti Verifier] Database selection check failed: " + e.getMessage());
        }

        // 3. Tables Test
        boolean tablesSuccess = false;
        Set<String> requiredTables = Set.of("users", "locations", "issues", "issue_status_history");
        Set<String> foundTables = new HashSet<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    foundTables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
            tablesSuccess = foundTables.containsAll(requiredTables);
            System.out.println("[JanDrishti Verifier] Found Tables: " + foundTables);
            System.out.println("[JanDrishti Verifier] Required Tables Present: " + tablesSuccess);
        } catch (Exception e) {
            System.err.println("[JanDrishti Verifier] Table metadata check failed: " + e.getMessage());
        }

        System.out.println("\n--- VERIFICATION RESULTS ---");
        System.out.println("MySQL connection: " + (connSuccess ? "SUCCESS" : "FAILED"));
        System.out.println("Database: " + (dbSuccess ? "SUCCESS" : "FAILED"));
        System.out.println("Tables: " + (tablesSuccess ? "SUCCESS" : "FAILED"));
    }

    public static void dumpAllData() {
        System.out.println("\n=== JanDrishti MySQL Data Dump ===");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("\n--- USERS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT id, username, email, full_name, phone_number, role, created_at FROM users")) {
                while (rs.next()) {
                    System.out.printf("User #%d | Username: %s | Name: %s | Email: %s | Role: %s\n",
                            rs.getLong("id"), rs.getString("username"), rs.getString("full_name"), rs.getString("email"), rs.getString("role"));
                }
            }

            System.out.println("\n--- LOCATIONS TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT id, state_name, district_name, city_name, area_name, road_name FROM locations")) {
                while (rs.next()) {
                    System.out.printf("Location #%d | %s, %s, %s, %s, %s\n",
                            rs.getLong("id"), rs.getString("road_name"), rs.getString("area_name"), rs.getString("city_name"), rs.getString("district_name"), rs.getString("state_name"));
                }
            }

            System.out.println("\n--- ISSUES TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT i.id, i.category, i.title, i.priority, i.status, i.department, i.reported_at, l.area_name, l.road_name FROM issues i JOIN locations l ON i.location_id = l.id")) {
                while (rs.next()) {
                    System.out.printf("Issue #%d | [%s] %s | Priority: %s | Status: %s | Area: %s | Dept: %s | Date: %s\n",
                            rs.getLong("id"), rs.getString("category"), rs.getString("title"), rs.getString("priority"), rs.getString("status"), rs.getString("area_name"), rs.getString("department"), rs.getTimestamp("reported_at"));
                }
            }

            System.out.println("\n--- ISSUE STATUS HISTORY TABLE ---");
            try (ResultSet rs = stmt.executeQuery("SELECT id, issue_id, old_status, new_status, changed_by, remarks, changed_at FROM issue_status_history")) {
                while (rs.next()) {
                    System.out.printf("History #%d | Issue #%d | %s -> %s | By: %s | %s\n",
                            rs.getLong("id"), rs.getLong("issue_id"), rs.getString("old_status"), rs.getString("new_status"), rs.getString("changed_by"), rs.getString("remarks"));
                }
            }
        } catch (Exception e) {
            System.err.println("Data dump error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        verifyAll();
        dumpAllData();
    }
}
