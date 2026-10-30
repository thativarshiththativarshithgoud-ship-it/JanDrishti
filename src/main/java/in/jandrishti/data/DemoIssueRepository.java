package in.jandrishti.data;

import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DemoIssueRepository implements IssueRepository {
    private final List<Issue> issues = new ArrayList<>(List.of(
            new Issue(1042L, "Pothole", "Deep pothole near school gate", 
                    "Deep severe pothole near school gate causing traffic bottleneck & accidents during morning hours", 
                    IssuePriority.CRITICAL, "Delhi", "Central Delhi", "New Delhi", "Karol Bagh", "Ajmal Khan Road", 
                    IssueStatus.VERIFIED, "Asha Verma", "+91 98765 43210", "Public Works Dept (PWD)", false, LocalDateTime.now().minusHours(2)),
            
            new Issue(1041L, "Streetlight", "Three lights not working", 
                    "Three main junction streetlights non-functional creating hazardous dark zone at night", 
                    IssuePriority.HIGH, "Maharashtra", "Pune", "Pune", "Kothrud", "Paud Road", 
                    IssueStatus.ASSIGNED, "Rohan Mehta", "+91 98111 22233", "Electricity Board (MSEDCL)", false, LocalDateTime.now().minusHours(5)),
            
            new Issue(1040L, "Drainage", "Waterlogging after light rain", 
                    "Severe waterlogging after light rain blocking 12th Main intersection and causing mosquito breeding", 
                    IssuePriority.HIGH, "Karnataka", "Bengaluru Urban", "Bengaluru", "Indiranagar", "12th Main Road", 
                    IssueStatus.REPORTED, "Nisha Rao", "+91 99000 88776", "BBMP Stormwater Drain Dept", false, LocalDateTime.now().minusHours(8)),
            
            new Issue(1039L, "Garbage", "Overflowing community bins", 
                    "Overflowing community waste bins attracting strays & blocking pedestrian pathway for 3 days", 
                    IssuePriority.MEDIUM, "Telangana", "Hyderabad", "Hyderabad", "Banjara Hills", "Road No. 12", 
                    IssueStatus.RESOLVED, "Vikram Singh", "+91 97654 32109", "GHMC Sanitation Dept", true, LocalDateTime.now().minusDays(1)),
            
            new Issue(1038L, "Damaged road", "Unsafe road edge at bus stop", 
                    "Unsafe eroded asphalt edge near busy suburban bus stop risking passenger ankle injuries", 
                    IssuePriority.MEDIUM, "Delhi", "South Delhi", "New Delhi", "Saket", "Press Enclave Road", 
                    IssueStatus.ASSIGNED, "Meera Joshi", "+91 98989 12345", "Municipal Corporation (MCD)", false, LocalDateTime.now().minusDays(1)),
            
            new Issue(1037L, "Pothole", "Potholes across service lane", 
                    "Multiple severe potholes across service lane near metro construction site damaging vehicle suspensions", 
                    IssuePriority.HIGH, "Maharashtra", "Mumbai Suburban", "Mumbai", "Andheri East", "Marol Road", 
                    IssueStatus.REPORTED, "Kabir Khan", "+91 98200 11223", "BMC Ward Officer", false, LocalDateTime.now().minusDays(2)),
            
            new Issue(1036L, "Streetlight", "Dark stretch beside park", 
                    "Dark 300m stretch beside public park causing safety concerns for evening senior citizen walkers", 
                    IssuePriority.LOW, "Delhi", "East Delhi", "New Delhi", "Mayur Vihar", "Patparganj Road", 
                    IssueStatus.RESOLVED, "Ishita Das", "+91 98333 44556", "BSES Yamuna Power", true, LocalDateTime.now().minusDays(3)),
            
            new Issue(1035L, "Water Leakage", "Main drinking water pipeline breach", 
                    "Main drinking water pipeline breach leaking thousands of liters of clean water onto main road", 
                    IssuePriority.CRITICAL, "Tamil Nadu", "Chennai", "Chennai", "Anna Nagar", "2nd Avenue", 
                    IssueStatus.VERIFIED, "Senthil Kumar", "+91 94440 99887", "Metrowater Board", false, LocalDateTime.now().minusHours(12)),
            
            new Issue(1034L, "Traffic Signal", "Blinking amber traffic light defect", 
                    "Traffic light stuck on blinking amber causing near collisions at busy four-way intersection", 
                    IssuePriority.CRITICAL, "Karnataka", "Bengaluru Urban", "Bengaluru", "HSR Layout", "27th Main", 
                    IssueStatus.ASSIGNED, "Ananya Hegde", "+91 99887 76655", "Traffic Police Cell", false, LocalDateTime.now().minusHours(14))
    ));

    @Override
    public List<Issue> findAll() {
        return List.copyOf(issues);
    }

    @Override
    public Optional<Issue> findById(long id) {
        return issues.stream().filter(i -> i.id() == id).findFirst();
    }

    @Override
    public void save(Issue issue) {
        issues.add(0, issue);
    }

    @Override
    public void updateStatus(long issueId, IssueStatus status) {
        for (int index = 0; index < issues.size(); index++) {
            Issue issue = issues.get(index);
            if (issue.id() == issueId) {
                issues.set(index, new Issue(
                        issue.id(), issue.category(), issue.title(), issue.description(), issue.priority(),
                        issue.state(), issue.district(), issue.city(), issue.area(), issue.road(),
                        status, issue.reportedBy(), issue.contactInfo(), issue.department(),
                        status == IssueStatus.RESOLVED ? true : issue.citizenVerified(), issue.reportedAt()));
                return;
            }
        }
    }

    @Override
    public void updatePriority(long issueId, IssuePriority priority) {
        for (int index = 0; index < issues.size(); index++) {
            Issue issue = issues.get(index);
            if (issue.id() == issueId) {
                issues.set(index, new Issue(
                        issue.id(), issue.category(), issue.title(), issue.description(), priority,
                        issue.state(), issue.district(), issue.city(), issue.area(), issue.road(),
                        issue.status(), issue.reportedBy(), issue.contactInfo(), issue.department(),
                        issue.citizenVerified(), issue.reportedAt()));
                return;
            }
        }
    }

    @Override
    public List<Issue> search(String query, String state, IssueStatus status, IssuePriority priority) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return issues.stream().filter(issue -> {
            boolean matchState = state == null || state.equals("All states") || issue.state().equalsIgnoreCase(state);
            boolean matchStatus = status == null || issue.status() == status;
            boolean matchPriority = priority == null || issue.priority() == priority;
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
}
