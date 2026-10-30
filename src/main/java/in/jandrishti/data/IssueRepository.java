package in.jandrishti.data;

import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    List<Issue> findAll();

    Optional<Issue> findById(long id);

    void save(Issue issue);

    void updateStatus(long issueId, IssueStatus status);

    void updatePriority(long issueId, IssuePriority priority);

    List<Issue> search(String query, String state, IssueStatus status, IssuePriority priority);
}
