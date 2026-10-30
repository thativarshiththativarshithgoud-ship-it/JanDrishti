package in.jandrishti.data;

import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MySqlIssueRepository implements IssueRepository {
    private final DemoIssueRepository fallbackRepo = new DemoIssueRepository();

    public MySqlIssueRepository() {
        DatabaseInitializer.initializeDatabase();
    }

    @Override
    public List<Issue> findAll() {
        String sql = """
            SELECT i.id, i.category, i.title, i.description, i.priority, i.status,
                   COALESCE(u.full_name, 'Citizen') AS reported_by,
                   i.department, i.citizen_verified, i.reported_at,
                   l.state_name, l.district_name, l.city_name, l.area_name, l.road_name
            FROM issues i
            JOIN locations l ON i.location_id = l.id
            LEFT JOIN users u ON i.reporter_id = u.id
            ORDER BY i.reported_at DESC
        """;

        List<Issue> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToIssue(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JanDrishti MySqlRepo] Note: Using demo repository fallback (" + e.getMessage() + ")");
            return fallbackRepo.findAll();
        }

        return list.isEmpty() ? fallbackRepo.findAll() : list;
    }

    @Override
    public Optional<Issue> findById(long id) {
        String sql = """
            SELECT i.id, i.category, i.title, i.description, i.priority, i.status,
                   COALESCE(u.full_name, 'Citizen') AS reported_by,
                   i.department, i.citizen_verified, i.reported_at,
                   l.state_name, l.district_name, l.city_name, l.area_name, l.road_name
            FROM issues i
            JOIN locations l ON i.location_id = l.id
            LEFT JOIN users u ON i.reporter_id = u.id
            WHERE i.id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIssue(rs));
                }
            }
        } catch (SQLException e) {
            return fallbackRepo.findById(id);
        }

        return fallbackRepo.findById(id);
    }

    @Override
    public void save(Issue issue) {
        fallbackRepo.save(issue);

        String findLocSql = "SELECT id FROM locations WHERE state_name=? AND district_name=? AND city_name=? AND area_name=? AND road_name=?";
        String insertLocSql = "INSERT INTO locations (state_name, district_name, city_name, area_name, road_name) VALUES (?, ?, ?, ?, ?)";
        String insertIssueSql = """
            INSERT INTO issues (location_id, category, title, description, priority, status, department, citizen_verified, reported_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            long locationId = -1;
            try (PreparedStatement ps = conn.prepareStatement(findLocSql)) {
                ps.setString(1, issue.state());
                ps.setString(2, issue.district());
                ps.setString(3, issue.city());
                ps.setString(4, issue.area());
                ps.setString(5, issue.road());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        locationId = rs.getLong(1);
                    }
                }
            }

            if (locationId == -1) {
                try (PreparedStatement ps = conn.prepareStatement(insertLocSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, issue.state());
                    ps.setString(2, issue.district());
                    ps.setString(3, issue.city());
                    ps.setString(4, issue.area());
                    ps.setString(5, issue.road());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) locationId = rs.getLong(1);
                    }
                }
            }

            if (locationId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(insertIssueSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, locationId);
                    ps.setString(2, issue.category());
                    ps.setString(3, issue.title());
                    ps.setString(4, issue.description());
                    ps.setString(5, issue.priority().name());
                    ps.setString(6, issue.status().label());
                    ps.setString(7, issue.department());
                    ps.setBoolean(8, issue.citizenVerified());
                    ps.setTimestamp(9, Timestamp.valueOf(issue.reportedAt()));
                    ps.executeUpdate();

                    long generatedIssueId = -1;
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) generatedIssueId = rs.getLong(1);
                    }

                    if (generatedIssueId != -1) {
                        String histSql = "INSERT INTO issue_status_history (issue_id, old_status, new_status, changed_by, remarks) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement hps = conn.prepareStatement(histSql)) {
                            hps.setLong(1, generatedIssueId);
                            hps.setNull(2, Types.VARCHAR);
                            hps.setString(3, issue.status().label());
                            hps.setString(4, issue.reportedBy());
                            hps.setString(5, "Initial issue report submitted.");
                            hps.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("[JanDrishti MySqlRepo] Saved to demo memory fallback (" + e.getMessage() + ")");
        }
    }

    @Override
    public void updateStatus(long issueId, IssueStatus status) {
        fallbackRepo.updateStatus(issueId, status);

        String oldStatus = "Reported";
        Optional<Issue> existing = findById(issueId);
        if (existing.isPresent()) {
            oldStatus = existing.get().status().label();
        }

        String sql = "UPDATE issues SET status = ?, resolved_at = ? WHERE id = ?";
        String historySql = "INSERT INTO issue_status_history (issue_id, old_status, new_status, changed_by, remarks) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.label());
                ps.setTimestamp(2, status == IssueStatus.RESOLVED ? Timestamp.valueOf(LocalDateTime.now()) : null);
                ps.setLong(3, issueId);
                ps.executeUpdate();
            }

            try (PreparedStatement hps = conn.prepareStatement(historySql)) {
                hps.setLong(1, issueId);
                hps.setString(2, oldStatus);
                hps.setString(3, status.label());
                hps.setString(4, "District Officer");
                hps.setString(5, "Status updated to " + status.label());
                hps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            // Handled by in-memory fallback
        }
    }

    @Override
    public void updatePriority(long issueId, IssuePriority priority) {
        fallbackRepo.updatePriority(issueId, priority);

        String sql = "UPDATE issues SET priority = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, priority.name());
            ps.setLong(2, issueId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Handled by in-memory fallback
        }
    }

    @Override
    public List<Issue> search(String query, String state, IssueStatus status, IssuePriority priority) {
        return findAll().stream().filter(issue -> {
            boolean matchState = state == null || state.equals("All states") || issue.state().equalsIgnoreCase(state);
            boolean matchStatus = status == null || issue.status() == status;
            boolean matchPriority = priority == null || issue.priority() == priority;
            String q = query == null ? "" : query.trim().toLowerCase();
            boolean matchQuery = q.isEmpty() ||
                    issue.title().toLowerCase().contains(q) ||
                    issue.description().toLowerCase().contains(q) ||
                    issue.locationLabel().toLowerCase().contains(q) ||
                    issue.road().toLowerCase().contains(q) ||
                    issue.category().toLowerCase().contains(q) ||
                    String.valueOf(issue.id()).contains(q);
            return matchState && matchStatus && matchPriority && matchQuery;
        }).toList();
    }

    private Issue mapResultSetToIssue(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String category = rs.getString("category");
        String title = rs.getString("title");
        String description = rs.getString("description");
        IssuePriority priority = IssuePriority.fromString(rs.getString("priority"));
        IssueStatus status = IssueStatus.REPORTED;
        String statusText = rs.getString("status");
        for (IssueStatus s : IssueStatus.values()) {
            if (s.label().equalsIgnoreCase(statusText)) {
                status = s;
                break;
            }
        }
        String reportedBy = rs.getString("reported_by");
        String department = rs.getString("department");
        boolean citizenVerified = rs.getBoolean("citizen_verified");
        Timestamp ts = rs.getTimestamp("reported_at");
        LocalDateTime reportedAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        String state = rs.getString("state_name");
        String district = rs.getString("district_name");
        String city = rs.getString("city_name");
        String area = rs.getString("area_name");
        String road = rs.getString("road_name");

        return new Issue(id, category, title, description, priority, state, district, city, area, road, status, reportedBy, "+91 98765 00000", department, citizenVerified, reportedAt);
    }
}
