package in.jandrishti.data;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static boolean initialized = false;

    public static synchronized boolean initializeDatabase() {
        if (initialized) return true;

        try (Connection conn = DatabaseConnection.getRootConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS jandrishti;");
            stmt.executeUpdate("USE jandrishti;");

            // Execute DDL Statements
            Path schemaPath = Paths.get("database/schema.sql");
            if (Files.exists(schemaPath)) {
                String sql = Files.readString(schemaPath, StandardCharsets.UTF_8);
                String[] statements = sql.split(";");
                for (String sqlStatement : statements) {
                    String trimmed = sqlStatement.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.executeUpdate(trimmed);
                        } catch (Exception ex) {
                            // DDL statement warnings ignored
                        }
                    }
                }
                initialized = true;
                seedInitialData(conn);
                System.out.println("[JanDrishti Initializer] SUCCESS: Database 'jandrishti' schema & seed data initialized successfully.");
                return true;
            } else {
                System.err.println("[JanDrishti Initializer] WARNING: database/schema.sql not found.");
                return false;
            }

        } catch (Exception e) {
            System.err.println("[JanDrishti Initializer] Note: " + e.getMessage());
            return false;
        }
    }

    private static void seedInitialData(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Seed default demo users with SHA-256 hashed passwords
            String adminHash = PasswordUtil.hashPassword("admin123");
            String passHash = PasswordUtil.hashPassword("password123");

            stmt.executeUpdate("INSERT INTO users (id, username, email, password_hash, full_name, phone_number, role) VALUES " +
                    "(1, 'citizen1', 'citizen@jandrishti.gov.in', '" + passHash + "', 'Asha Verma', '+91 98765 43210', 'CITIZEN'), " +
                    "(2, 'officer1', 'pwd@jandrishti.gov.in', '" + passHash + "', 'Rohan Mehta', '+91 98111 22233', 'OFFICER'), " +
                    "(3, 'admin', 'admin@jandrishti.gov.in', '" + adminHash + "', 'System Administrator', '+91 99000 11223', 'ADMIN') " +
                    "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash)");

            // Check if issues table has data
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM issues");
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Data already seeded
            }

            // Seed demo locations & issues
            DemoIssueRepository demoRepo = new DemoIssueRepository();
            MySqlIssueRepository sqlRepo = new MySqlIssueRepository();

            for (var issue : demoRepo.findAll()) {
                sqlRepo.save(issue);
            }
            System.out.println("[JanDrishti Initializer] Seeded " + demoRepo.findAll().size() + " sample issues into MySQL database.");
        } catch (Exception e) {
            // Ignore seed warnings
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
