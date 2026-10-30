package in.jandrishti.ui;

import in.jandrishti.data.IssueRepository;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;
import in.jandrishti.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class OfficerView extends BorderPane {
    private final IssueRepository repository;
    private final User officerUser;
    private final Runnable onLogoutCallback;

    // Sidebar navigation buttons
    private final Button btnNavDashboard = new Button("🏠   Dashboard");
    private final Button btnNavAssigned = new Button("🎯   Assigned Issues");
    private final Button btnNavDetails = new Button("🔍   Issue Details");
    private final Button btnNavUpdateStatus = new Button("🔄   Update Status");
    private final Button btnNavAreaIssues = new Button("📍   Area Issues");
    private final Button btnNavNotifs = new Button("🔔   Notifications");
    private final Button btnNavProfile = new Button("👤   Profile");

    // Center views
    private final StackPane mainStack = new StackPane();
    private final ScrollPane dashboardScroll = new ScrollPane();
    private final ScrollPane assignedScroll = new ScrollPane();
    private final ScrollPane detailsScroll = new ScrollPane();
    private final ScrollPane updateScroll = new ScrollPane();
    private final ScrollPane areaScroll = new ScrollPane();
    private final ScrollPane notifsScroll = new ScrollPane();
    private final ProfileView profileView;

    private final VBox inspectorPanel = new VBox(10);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OfficerView(IssueRepository repository, User officerUser, Runnable onLogoutCallback) {
        this.repository = repository;
        this.officerUser = officerUser;
        this.onLogoutCallback = onLogoutCallback;
        this.profileView = new ProfileView(repository);

        getStyleClass().add("app-shell");

        setTop(buildTopHeader());
        setLeft(buildSidebar());
        setCenter(buildCenterContent());

        refreshAll();
    }

    private Node buildTopHeader() {
        Label logoIcon = new Label("👁");
        logoIcon.getStyleClass().add("brand-logo-icon");

        Label logoText = new Label("JanDrishti");
        logoText.getStyleClass().add("brand-title");

        Label logoSub = new Label("Field Officer Operations Center");
        logoSub.getStyleClass().add("brand-subtitle");

        VBox titleBox = new VBox(1, logoText, logoSub);
        HBox brandBox = new HBox(10, logoIcon, titleBox);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label("👮 District Officer");
        roleBadge.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 11px;");

        String name = officerUser != null ? officerUser.fullName() : "Rohan Mehta";
        Button userMenu = new Button("👤  Welcome, " + name + " ▾");
        userMenu.getStyleClass().add("user-profile-menu");
        userMenu.setOnAction(e -> switchNavView(6));

        Button logoutHeaderBtn = new Button("🚪  Logout");
        logoutHeaderBtn.getStyleClass().add("secondary-button");
        logoutHeaderBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10; -fx-font-weight: 700;");
        logoutHeaderBtn.setOnAction(e -> handleLogout());

        HBox rightHeader = new HBox(10, roleBadge, userMenu, logoutHeaderBtn);
        rightHeader.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(brandBox, rightHeader);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);
        topBar.getStyleClass().add("top-header");

        return topBar;
    }

    private Node buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("left-sidebar");

        btnNavDashboard.getStyleClass().addAll("sidebar-button", "sidebar-button-active");
        btnNavAssigned.getStyleClass().add("sidebar-button");
        btnNavDetails.getStyleClass().add("sidebar-button");
        btnNavUpdateStatus.getStyleClass().add("sidebar-button");
        btnNavAreaIssues.getStyleClass().add("sidebar-button");
        btnNavNotifs.getStyleClass().add("sidebar-button");
        btnNavProfile.getStyleClass().add("sidebar-button");

        btnNavDashboard.setOnAction(e -> switchNavView(0));
        btnNavAssigned.setOnAction(e -> switchNavView(1));
        btnNavDetails.setOnAction(e -> switchNavView(2));
        btnNavUpdateStatus.setOnAction(e -> switchNavView(3));
        btnNavAreaIssues.setOnAction(e -> switchNavView(4));
        btnNavNotifs.setOnAction(e -> switchNavView(5));
        btnNavProfile.setOnAction(e -> switchNavView(6));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("sidebar-button");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(
                btnNavDashboard,
                btnNavAssigned,
                btnNavDetails,
                btnNavUpdateStatus,
                new Separator(),
                btnNavAreaIssues,
                btnNavNotifs,
                btnNavProfile,
                spacer,
                logoutBtn
        );

        return sidebar;
    }

    private void handleLogout() {
        in.jandrishti.data.UserSession.confirmAndLogout(onLogoutCallback);
    }

    private void switchNavView(int index) {
        btnNavDashboard.getStyleClass().remove("sidebar-button-active");
        btnNavAssigned.getStyleClass().remove("sidebar-button-active");
        btnNavDetails.getStyleClass().remove("sidebar-button-active");
        btnNavUpdateStatus.getStyleClass().remove("sidebar-button-active");
        btnNavAreaIssues.getStyleClass().remove("sidebar-button-active");
        btnNavNotifs.getStyleClass().remove("sidebar-button-active");
        btnNavProfile.getStyleClass().remove("sidebar-button-active");

        dashboardScroll.setVisible(false);
        assignedScroll.setVisible(false);
        detailsScroll.setVisible(false);
        updateScroll.setVisible(false);
        areaScroll.setVisible(false);
        notifsScroll.setVisible(false);
        profileView.setVisible(false);

        switch (index) {
            case 0 -> {
                btnNavDashboard.getStyleClass().add("sidebar-button-active");
                dashboardScroll.setVisible(true);
                refreshAll();
            }
            case 1 -> {
                btnNavAssigned.getStyleClass().add("sidebar-button-active");
                assignedScroll.setContent(buildAssignedContent());
                assignedScroll.setVisible(true);
            }
            case 2 -> {
                btnNavDetails.getStyleClass().add("sidebar-button-active");
                detailsScroll.setContent(buildDetailsContent());
                detailsScroll.setVisible(true);
            }
            case 3 -> {
                btnNavUpdateStatus.getStyleClass().add("sidebar-button-active");
                updateScroll.setContent(buildUpdateStatusContent());
                updateScroll.setVisible(true);
            }
            case 4 -> {
                btnNavAreaIssues.getStyleClass().add("sidebar-button-active");
                areaScroll.setContent(buildAreaContent());
                areaScroll.setVisible(true);
            }
            case 5 -> {
                btnNavNotifs.getStyleClass().add("sidebar-button-active");
                notifsScroll.setContent(buildNotifsContent());
                notifsScroll.setVisible(true);
            }
            case 6 -> {
                btnNavProfile.getStyleClass().add("sidebar-button-active");
                profileView.setVisible(true);
            }
        }
    }

    private Node buildCenterContent() {
        VBox container = new VBox(18);
        container.getStyleClass().add("content-container");
        container.setPadding(new Insets(20, 24, 60, 24));
        container.setMinHeight(Region.USE_PREF_SIZE);

        container.getChildren().addAll(
                buildHeroHeader(),
                buildMetricsRow(),
                buildAssignedTablePanel()
        );

        setupScroll(dashboardScroll, container);
        setupScroll(assignedScroll, buildAssignedContent());
        setupScroll(detailsScroll, buildDetailsContent());
        setupScroll(updateScroll, buildUpdateStatusContent());
        setupScroll(areaScroll, buildAreaContent());
        setupScroll(notifsScroll, buildNotifsContent());

        mainStack.getChildren().addAll(
                dashboardScroll,
                assignedScroll,
                detailsScroll,
                updateScroll,
                areaScroll,
                notifsScroll,
                profileView
        );

        assignedScroll.setVisible(false);
        detailsScroll.setVisible(false);
        updateScroll.setVisible(false);
        areaScroll.setVisible(false);
        notifsScroll.setVisible(false);
        profileView.setVisible(false);

        return mainStack;
    }

    private void setupScroll(ScrollPane pane, Node content) {
        pane.setContent(content);
        pane.setFitToWidth(true);
        pane.setFitToHeight(false);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.getStyleClass().add("main-scroll-pane");
    }

    private Node buildHeroHeader() {
        Label title = new Label("👮 Field Officer Workload Operations");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Review your assigned civic defect tasks, inspect field addresses, and post completion status updates.");
        subtitle.getStyleClass().add("hero-subtitle");

        VBox textGroup = new VBox(3, title, subtitle);

        Button btnUpdateQuick = new Button("🔄 Update Task Status");
        btnUpdateQuick.getStyleClass().add("btn-report-new");
        btnUpdateQuick.setOnAction(e -> switchNavView(3));

        HBox btnGroup = new HBox(10, btnUpdateQuick);
        btnGroup.setAlignment(Pos.CENTER_RIGHT);

        HBox hero = new HBox(textGroup, btnGroup);
        hero.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        return hero;
    }

    private Node buildMetricsRow() {
        List<Issue> assigned = repository.findAll();
        long total = assigned.size();
        long open = assigned.stream().filter(i -> i.status() != IssueStatus.RESOLVED).count();
        long critical = assigned.stream().filter(i -> i.priority() == IssuePriority.CRITICAL).count();
        long resolved = assigned.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count();

        GridPane grid = new GridPane();
        grid.setHgap(14);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(25);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(25);

        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        grid.add(cardMetric("card-total", "🎯 ASSIGNED TASKS", new Label(String.valueOf(total)), "Workload in your department"), 0, 0);
        grid.add(cardMetric("card-open", "⏱️ PENDING REPAIR", new Label(String.valueOf(open)), "Awaiting field repair completion"), 1, 0);
        grid.add(cardMetric("card-critical", "⚠️ CRITICAL URGENT", new Label(String.valueOf(critical)), "High hazard priority"), 2, 0);
        grid.add(cardMetric("card-resolved", "✅ COMPLETED REPAIRS", new Label(String.valueOf(resolved)), "Closed & resolved tasks"), 3, 0);

        return grid;
    }

    private Node cardMetric(String styleClass, String heading, Label valLabel, String sub) {
        Label lblHeading = new Label(heading);
        lblHeading.getStyleClass().add("metric-card-title");
        valLabel.getStyleClass().add("metric-card-num");
        Label lblSub = new Label(sub);
        lblSub.getStyleClass().add("metric-card-sub");

        VBox card = new VBox(4, lblHeading, valLabel, lblSub);
        card.getStyleClass().add(styleClass);
        return card;
    }

    private Node buildAssignedTablePanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("white-panel");

        Label title = new Label("📋 Assigned Field Workload Register");
        title.getStyleClass().add("panel-header-title");
        Label sub = new Label("Select any assigned defect to view address landmarks & update resolution status");
        sub.getStyleClass().add("panel-header-sub");

        ObservableList<Issue> assignedList = FXCollections.observableArrayList(repository.findAll());
        TableView<Issue> table = new TableView<>(assignedList);

        TableColumn<Issue, String> idCol = textColumn("#", i -> String.valueOf(i.id()));
        TableColumn<Issue, String> titleCol = textColumn("DEFECT TITLE", Issue::title);
        TableColumn<Issue, String> prioCol = textColumn("PRIORITY", i -> i.priority().label());
        TableColumn<Issue, String> locCol = textColumn("LOCATION", Issue::locationLabel);
        TableColumn<Issue, String> statusCol = textColumn("STATUS", i -> i.status().label());
        TableColumn<Issue, String> deptCol = textColumn("DEPARTMENT", Issue::department);

        prioCol.setCellFactory(col -> new TableCell<>() {
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

        table.getColumns().setAll(idCol, titleCol, prioCol, locCol, statusCol, deptCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(340);

        panel.getChildren().addAll(new VBox(2, title, sub), table);
        return panel;
    }

    private Node buildAssignedContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🎯 Officer Assigned Workload List");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Complete list of civic issues assigned to Public Works Dept (PWD) & Municipal Officers.");
        sub.getStyleClass().add("hero-subtitle");

        TableView<Issue> table = new TableView<>(FXCollections.observableArrayList(repository.findAll()));
        table.getColumns().setAll(
                textColumn("#", i -> String.valueOf(i.id())),
                textColumn("TITLE", Issue::title),
                textColumn("CATEGORY", Issue::category),
                textColumn("PRIORITY", i -> i.priority().label()),
                textColumn("STATUS", i -> i.status().label()),
                textColumn("LOCATION", Issue::locationLabel),
                textColumn("REPORTED BY", Issue::reportedBy)
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(420);

        VBox panel = new VBox(12, new VBox(2, title, sub), table);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildDetailsContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🔍 Issue Inspector & Landmark Context");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Inspect complete issue context, exact street address and reporter contact.");
        sub.getStyleClass().add("hero-subtitle");

        List<Issue> issues = repository.findAll();
        ComboBox<Issue> issueCombo = new ComboBox<>(FXCollections.observableArrayList(issues));
        if (!issues.isEmpty()) issueCombo.getSelectionModel().selectFirst();
        issueCombo.getStyleClass().add("filter-field");
        issueCombo.setStyle("-fx-pref-width: 400px;");

        VBox detailBox = new VBox(12);
        detailBox.getStyleClass().add("white-panel");

        issueCombo.valueProperty().addListener((o, oldV, selected) -> updateDetailsView(detailBox, selected));
        updateDetailsView(detailBox, issueCombo.getValue());

        VBox panel = new VBox(14, new VBox(2, title, sub), new HBox(10, new Label("Select Defect:"), issueCombo), detailBox);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private void updateDetailsView(VBox box, Issue issue) {
        box.getChildren().clear();
        if (issue == null) {
            box.getChildren().add(new Label("No issue selected."));
            return;
        }

        Label heading = new Label("#" + issue.id() + " • " + issue.title());
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label category = new Label("Category: " + issue.category() + " | Priority: " + issue.priority().label());
        category.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");

        Label desc = new Label("Description: \"" + issue.description() + "\"");
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        Label address = new Label("📍 Address: " + issue.fullAddressLabel());
        address.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold;");

        Label reporter = new Label("👤 Reporter: " + issue.reportedBy() + " (Contact: " + issue.contactInfo() + ")");
        reporter.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Label dept = new Label("🏢 Target Department: " + issue.department());
        dept.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        box.getChildren().addAll(heading, category, desc, new Separator(), address, reporter, dept);
    }

    private Node buildUpdateStatusContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🔄 Update Issue Resolution Status");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Post progress remarks and mark defect tasks as resolved.");
        sub.getStyleClass().add("hero-subtitle");

        List<Issue> issues = repository.findAll();
        ComboBox<Issue> issueCombo = new ComboBox<>(FXCollections.observableArrayList(issues));
        if (!issues.isEmpty()) issueCombo.getSelectionModel().selectFirst();
        issueCombo.getStyleClass().add("filter-field");
        issueCombo.setStyle("-fx-pref-width: 400px;");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Assigned", "Verified", "Resolved"));
        statusCombo.getSelectionModel().select("Resolved");
        statusCombo.getStyleClass().add("filter-field");

        TextArea remarksArea = new TextArea();
        remarksArea.setPromptText("Enter field work progress / resolution remarks...");
        remarksArea.setPrefRowCount(3);
        remarksArea.getStyleClass().add("filter-field");

        Button btnUpdate = new Button("🚀 Submit Status Update");
        btnUpdate.getStyleClass().add("btn-report-new");
        btnUpdate.setOnAction(e -> {
            Issue selected = issueCombo.getValue();
            if (selected != null) {
                IssueStatus newStatus = parseStatus(statusCombo.getValue());
                if (newStatus != null) {
                    repository.updateStatus(selected.id(), newStatus);
                    refreshAll();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Status for Issue #" + selected.id() + " updated to " + newStatus.label() + ".", ButtonType.OK);
                    alert.setHeaderText("Status Updated");
                    alert.showAndWait();
                }
            }
        });

        VBox form = new VBox(12,
                new HBox(10, new Label("Select Defect:"), issueCombo),
                new HBox(10, new Label("New Status:"), statusCombo),
                new VBox(4, new Label("Officer Resolution Remarks:"), remarksArea),
                btnUpdate
        );
        form.getStyleClass().add("white-panel");

        VBox panel = new VBox(14, new VBox(2, title, sub), form);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildAreaContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("📍 Assigned District / Area Defect Map");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Defects reported in Central Delhi & Metropolitan zones.");
        sub.getStyleClass().add("hero-subtitle");

        TableView<Issue> table = new TableView<>(FXCollections.observableArrayList(repository.findAll()));
        table.getColumns().setAll(
                textColumn("#", i -> String.valueOf(i.id())),
                textColumn("TITLE", Issue::title),
                textColumn("CATEGORY", Issue::category),
                textColumn("PRIORITY", i -> i.priority().label()),
                textColumn("LOCATION", Issue::locationLabel)
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(380);

        VBox panel = new VBox(12, new VBox(2, title, sub), table);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildNotifsContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🔔 Officer Task Notifications");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Newly assigned tasks and priority escalations.");
        sub.getStyleClass().add("hero-subtitle");

        VBox list = new VBox(10);
        list.getStyleClass().add("white-panel");

        addNotif(list, "📌 New Task Assigned: #1042", "Deep pothole near school gate assigned to PWD.", "10 mins ago");
        addNotif(list, "⚠️ Priority Escalated: #1035", "Main drinking water pipeline breach marked CRITICAL.", "1 hour ago");

        VBox panel = new VBox(12, new VBox(2, title, sub), list);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private void addNotif(VBox container, String title, String body, String time) {
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: 800; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
        Label b = new Label(body);
        b.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        Label tm = new Label(time);
        tm.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        HBox row = new HBox(new VBox(2, t, b), tm);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        row.setStyle("-fx-padding: 10px; -fx-background-color: #f8fafc; -fx-background-radius: 6px;");
        container.getChildren().add(row);
    }

    private TableColumn<Issue, String> textColumn(String title, java.util.function.Function<Issue, String> mapper) {
        TableColumn<Issue, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }

    private void refreshAll() {
        // Refresh dashboard tables
    }

    private IssueStatus parseStatus(String text) {
        for (IssueStatus s : IssueStatus.values()) {
            if (s.label().equalsIgnoreCase(text)) return s;
        }
        return IssueStatus.RESOLVED;
    }
}
