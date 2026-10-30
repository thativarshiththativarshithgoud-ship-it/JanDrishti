package in.jandrishti.ui;

import in.jandrishti.data.IssueRepository;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AnalyticsView extends ScrollPane {
    private final IssueRepository repository;

    public AnalyticsView(IssueRepository repository) {
        this.repository = repository;
        setFitToWidth(true);
        getStyleClass().add("submit-scroll-pane");
        setContent(buildContent());
    }

    private Node buildContent() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(30, 45, 45, 45));
        container.getStyleClass().add("form-page-container");

        Label eyebrow = new Label("DATA & RESOLUTION INSIGHTS");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("Regional Analytics & SLA Dashboard");
        title.getStyleClass().add("page-title");
        Label desc = new Label("Comprehensive overview of issue distributions, state breakdown, and department dispatch statistics.");
        desc.getStyleClass().add("page-description");
        VBox header = new VBox(6, eyebrow, title, desc);

        List<Issue> issues = repository.findAll();
        long total = issues.size();

        // Row 1: Priority Cards
        HBox priorityCards = new HBox(14,
                statCard("CRITICAL PRIORITY", countPriority(issues, IssuePriority.CRITICAL), "Immediate response required", "metric-coral"),
                statCard("HIGH PRIORITY", countPriority(issues, IssuePriority.HIGH), "Urgent road / safety risks", "metric-amber"),
                statCard("MEDIUM PRIORITY", countPriority(issues, IssuePriority.MEDIUM), "Standard municipal workflow", "metric-teal"),
                statCard("LOW PRIORITY", countPriority(issues, IssuePriority.LOW), "Routine inspection queue", "metric-green")
        );

        // Row 2: State Breakdown & Dept Workload Grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.add(buildStateDistributionPanel(issues, total), 0, 0);
        grid.add(buildDepartmentWorkloadPanel(issues), 1, 0);

        container.getChildren().addAll(header, priorityCards, grid);
        return container;
    }

    private long countPriority(List<Issue> issues, IssuePriority priority) {
        return issues.stream().filter(i -> i.priority() == priority).count();
    }

    private Node statCard(String label, long count, String subtitle, String colorClass) {
        Label heading = new Label(label);
        heading.getStyleClass().add("metric-label");
        Label val = new Label(String.valueOf(count));
        val.getStyleClass().addAll("metric-value", colorClass);
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("metric-detail");

        VBox card = new VBox(10, heading, val, sub);
        card.getStyleClass().add("metric-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Node buildStateDistributionPanel(List<Issue> issues, long total) {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("metric-card");

        Label title = new Label("State-wise Issue Share");
        title.getStyleClass().add("section-title");

        Map<String, Long> stateCounts = issues.stream()
                .collect(Collectors.groupingBy(Issue::state, Collectors.counting()));

        VBox list = new VBox(12);
        stateCounts.forEach((state, count) -> {
            double ratio = total > 0 ? (double) count / total : 0;
            Label name = new Label(state);
            name.getStyleClass().add("hotspot-name");
            Label cnt = new Label(count + " reports (" + (int)(ratio * 100) + "%)");
            cnt.getStyleClass().add("section-detail");

            HBox labelRow = new HBox(name, cnt);
            HBox.setHgrow(name, Priority.ALWAYS);

            ProgressBar pb = new ProgressBar(ratio);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.getStyleClass().add("analytics-progress-bar");

            VBox stateRow = new VBox(4, labelRow, pb);
            list.getChildren().add(stateRow);
        });

        panel.getChildren().addAll(title, list);
        return panel;
    }

    private Node buildDepartmentWorkloadPanel(List<Issue> issues) {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("metric-card");

        Label title = new Label("Department Workload Dispatch");
        title.getStyleClass().add("section-title");

        Map<String, Long> deptCounts = issues.stream()
                .collect(Collectors.groupingBy(Issue::department, Collectors.counting()));

        VBox list = new VBox(10);
        deptCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Label dName = new Label(e.getKey());
                    dName.getStyleClass().add("workflow-name");
                    Label dVal = new Label(e.getValue() + " active task(s)");
                    dVal.getStyleClass().add("workflow-value");

                    HBox row = new HBox(dName, dVal);
                    row.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(dName, Priority.ALWAYS);
                    list.getChildren().add(row);
                });

        panel.getChildren().addAll(title, list);
        return panel;
    }
}
