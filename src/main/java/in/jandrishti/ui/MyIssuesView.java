package in.jandrishti.ui;

import in.jandrishti.data.IssueRepository;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public final class MyIssuesView extends ScrollPane {
    private final IssueRepository repository;
    private final ObservableList<Issue> myIssuesList = FXCollections.observableArrayList();
    private final TableView<Issue> table = new TableView<>(myIssuesList);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyIssuesView(IssueRepository repository) {
        this.repository = repository;
        setFitToWidth(true);
        getStyleClass().add("main-scroll-pane");
        setContent(buildContent());
        refresh();
    }

    private Node buildContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("📋 My Reported Issues");
        title.getStyleClass().add("hero-title");

        Label sub = new Label("Track the status, officer assignment, and citizen verification for issues submitted by your account.");
        sub.getStyleClass().add("hero-subtitle");

        VBox header = new VBox(4, title, sub);

        // Table
        TableColumn<Issue, String> idCol = textColumn("#", i -> String.valueOf(i.id()));
        idCol.setPrefWidth(55);

        TableColumn<Issue, String> titleCol = textColumn("ISSUE TITLE", Issue::title);
        titleCol.setPrefWidth(220);

        TableColumn<Issue, String> priorityCol = textColumn("PRIORITY", i -> i.priority().label());
        priorityCol.setPrefWidth(95);
        priorityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(val);
                    badge.getStyleClass().add("badge-priority-" + val.toLowerCase());
                    setGraphic(badge); setText(null);
                }
            }
        });

        TableColumn<Issue, String> locationCol = textColumn("LOCATION", Issue::locationLabel);
        locationCol.setPrefWidth(160);

        TableColumn<Issue, String> deptCol = textColumn("ASSIGNED DEPT", Issue::department);
        deptCol.setPrefWidth(160);

        TableColumn<Issue, String> statusCol = textColumn("STATUS", i -> i.status().label());
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(val);
                    badge.getStyleClass().add("badge-status-" + val.toLowerCase());
                    setGraphic(badge); setText(null);
                }
            }
        });

        TableColumn<Issue, String> dateCol = textColumn("SUBMITTED AT", i -> i.reportedAt().format(DATE_FORMAT));
        dateCol.setPrefWidth(130);

        table.getColumns().setAll(idCol, titleCol, priorityCol, locationCol, deptCol, statusCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getStyleClass().add("issue-table");
        table.setFixedCellSize(44);

        VBox card = new VBox(14, header, table);
        card.getStyleClass().add("white-panel");

        return card;
    }

    private TableColumn<Issue, String> textColumn(String heading, java.util.function.Function<Issue, String> value) {
        TableColumn<Issue, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(value.apply(cell.getValue())));
        return column;
    }

    public void refresh() {
        myIssuesList.setAll(repository.findAll());
    }
}
