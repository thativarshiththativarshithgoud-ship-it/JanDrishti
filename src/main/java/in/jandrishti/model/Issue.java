package in.jandrishti.model;

import java.time.LocalDateTime;

public record Issue(
        long id,
        String category,
        String title,
        String description,
        IssuePriority priority,
        String state,
        String district,
        String city,
        String area,
        String road,
        IssueStatus status,
        String reportedBy,
        String contactInfo,
        String department,
        boolean citizenVerified,
        LocalDateTime reportedAt) {

    public String locationLabel() {
        return area + ", " + city;
    }

    public String fullAddressLabel() {
        return road + ", " + area + ", " + city + ", " + district + ", " + state;
    }
}
