package in.jandrishti.ui;

import in.jandrishti.data.AiChatService;
import in.jandrishti.data.DatabaseConnection;
import in.jandrishti.data.IssueRepository;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;
import in.jandrishti.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AdminView extends BorderPane {
    private final IssueRepository repository;
    private final User adminUser;
    private final Runnable onLogoutCallback;

    private final ObservableList<Issue> visibleIssues = FXCollections.observableArrayList();
    private final TableView<Issue> issueTable = new TableView<>(visibleIssues);

    private final Label totalValue = new Label("0");
    private final Label openValue = new Label("0");
    private final Label criticalValue = new Label("0");
    private final Label resolvedValue = new Label("0");

    private final ComboBox<String> stateFilter = new ComboBox<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final TextField searchField = new TextField();

    // Sidebar navigation buttons
    private final Button btnNavDashboard = new Button("🏠   Dashboard");
    private final Button btnNavAllIssues = new Button("📋   All Issues");
    private final Button btnNavUsers = new Button("👥   Manage Users");
    private final Button btnNavOfficers = new Button("👮   Manage Officers");
    private final Button btnNavAssign = new Button("📌   Assign Issues");
    private final Button btnNavAnalytics = new Button("📊   Analytics");
    private final Button btnNavHotspots = new Button("📍   Problem Hotspots");
    private final Button btnNavReports = new Button("📑   Reports");
    private final Button btnNavProfile = new Button("👤   Profile");

    // Center views
    private final StackPane mainStack = new StackPane();
    private final ScrollPane dashboardScroll = new ScrollPane();
    private final ScrollPane allIssuesScroll = new ScrollPane();
    private final ScrollPane usersScroll = new ScrollPane();
    private final ScrollPane officersScroll = new ScrollPane();
    private final ScrollPane assignScroll = new ScrollPane();
    private final AnalyticsView analyticsView;
    private final ProfileView profileView;

    private final VBox inspectorPanel = new VBox(10);
    private final VBox hotspotsList = new VBox(8);
    private final VBox workflowList = new VBox(12);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AdminView(IssueRepository repository, User adminUser, Runnable onLogoutCallback) {
        this.repository = repository;
        this.adminUser = adminUser;
        this.onLogoutCallback = onLogoutCallback;
        this.analyticsView = new AnalyticsView(repository);
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

        Label logoSub = new Label("Admin Management Portal");
        logoSub.getStyleClass().add("brand-subtitle");

        VBox titleBox = new VBox(1, logoText, logoSub);
        HBox brandBox = new HBox(10, logoIcon, titleBox);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label("👑 Administrator");
        roleBadge.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 11px;");

        String name = adminUser != null ? adminUser.fullName() : "System Admin";
        Button userMenu = new Button("👤  Welcome, " + name + " ▾");
        userMenu.getStyleClass().add("user-profile-menu");
        userMenu.setOnAction(e -> switchNavView(8));

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
        btnNavAllIssues.getStyleClass().add("sidebar-button");
        btnNavUsers.getStyleClass().add("sidebar-button");
        btnNavOfficers.getStyleClass().add("sidebar-button");
        btnNavAssign.getStyleClass().add("sidebar-button");
        btnNavAnalytics.getStyleClass().add("sidebar-button");
        btnNavHotspots.getStyleClass().add("sidebar-button");
        btnNavReports.getStyleClass().add("sidebar-button");
        btnNavProfile.getStyleClass().add("sidebar-button");

        btnNavDashboard.setOnAction(e -> switchNavView(0));
        btnNavAllIssues.setOnAction(e -> switchNavView(1));
        btnNavUsers.setOnAction(e -> switchNavView(2));
        btnNavOfficers.setOnAction(e -> switchNavView(3));
        btnNavAssign.setOnAction(e -> switchNavView(4));
        btnNavAnalytics.setOnAction(e -> switchNavView(5));
        btnNavHotspots.setOnAction(e -> switchNavView(6));
        btnNavReports.setOnAction(e -> switchNavView(7));
        btnNavProfile.setOnAction(e -> switchNavView(8));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("sidebar-button");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(
                btnNavDashboard,
                btnNavAllIssues,
                new Separator(),
                btnNavUsers,
                btnNavOfficers,
                btnNavAssign,
                new Separator(),
                btnNavAnalytics,
                btnNavHotspots,
                btnNavReports,
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
        btnNavAllIssues.getStyleClass().remove("sidebar-button-active");
        btnNavUsers.getStyleClass().remove("sidebar-button-active");
        btnNavOfficers.getStyleClass().remove("sidebar-button-active");
        btnNavAssign.getStyleClass().remove("sidebar-button");
        btnNavAnalytics.getStyleClass().remove("sidebar-button-active");
        btnNavHotspots.getStyleClass().remove("sidebar-button-active");
        btnNavReports.getStyleClass().remove("sidebar-button-active");
        btnNavProfile.getStyleClass().remove("sidebar-button-active");

        dashboardScroll.setVisible(false);
        allIssuesScroll.setVisible(false);
        usersScroll.setVisible(false);
        officersScroll.setVisible(false);
        assignScroll.setVisible(false);
        analyticsView.setVisible(false);
        profileView.setVisible(false);

        switch (index) {
            case 0 -> {
                btnNavDashboard.getStyleClass().add("sidebar-button-active");
                dashboardScroll.setVisible(true);
                refreshAll();
            }
            case 1 -> {
                btnNavAllIssues.getStyleClass().add("sidebar-button-active");
                allIssuesScroll.setVisible(true);
                refreshAll();
            }
            case 2 -> {
                btnNavUsers.getStyleClass().add("sidebar-button-active");
                usersScroll.setContent(buildUsersContent());
                usersScroll.setVisible(true);
            }
            case 3 -> {
                btnNavOfficers.getStyleClass().add("sidebar-button-active");
                officersScroll.setContent(buildOfficersContent());
                officersScroll.setVisible(true);
            }
            case 4 -> {
                btnNavAssign.getStyleClass().add("sidebar-button-active");
                assignScroll.setContent(buildAssignContent());
                assignScroll.setVisible(true);
            }
            case 5, 6 -> {
                btnNavAnalytics.getStyleClass().add("sidebar-button-active");
                analyticsView.setVisible(true);
            }
            case 7 -> {
                btnNavReports.getStyleClass().add("sidebar-button-active");
                dashboardScroll.setVisible(true);
            }
            case 8 -> {
                btnNavProfile.getStyleClass().add("sidebar-button-active");
                profileView.setVisible(true);
            }
        }
    }

    private Node buildCenterContent() {
        VBox container = new VBox(18);
        container.getStyleClass().add("content-container");
        container.setPadding(new Insets(20, 24, 120, 24));
        container.setMinHeight(Region.USE_PREF_SIZE);
        container.setPrefHeight(Region.USE_COMPUTED_SIZE);
        container.setMaxHeight(Double.MAX_VALUE);

        Label bottomFooter = new Label("JanDrishti National Civic Operations Portal • All System Data Synced");
        bottomFooter.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 16 0 20 0; -fx-alignment: center;");
        bottomFooter.setMaxWidth(Double.MAX_VALUE);

        container.getChildren().addAll(
                buildHeroHeader(),
                buildMetricsRow(),
                buildMainGrid(),
                new Separator(),
                bottomFooter
        );

        setupScroll(dashboardScroll, container);
        setupScroll(allIssuesScroll, buildAllIssuesContent());
        setupScroll(usersScroll, buildUsersContent());
        setupScroll(officersScroll, buildOfficersContent());
        setupScroll(assignScroll, buildAssignContent());

        mainStack.getChildren().addAll(
                dashboardScroll,
                allIssuesScroll,
                usersScroll,
                officersScroll,
                assignScroll,
                analyticsView,
                profileView
        );

        allIssuesScroll.setVisible(false);
        usersScroll.setVisible(false);
        officersScroll.setVisible(false);
        assignScroll.setVisible(false);
        analyticsView.setVisible(false);
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
        Label title = new Label("👑 National Civic Operations Control Center");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Monitor all reported civic defects across Indian states, assign officers, and audit system resolutions.");
        subtitle.getStyleClass().add("hero-subtitle");

        VBox textGroup = new VBox(3, title, subtitle);

        Button btnAssignQuick = new Button("📌 Assign Pending Issues");
        btnAssignQuick.getStyleClass().add("btn-report-new");
        btnAssignQuick.setOnAction(e -> switchNavView(4));

        HBox btnGroup = new HBox(10, btnAssignQuick);
        btnGroup.setAlignment(Pos.CENTER_RIGHT);

        HBox hero = new HBox(textGroup, btnGroup);
        hero.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        return hero;
    }

    private Node buildMetricsRow() {
        GridPane grid = new GridPane();
        grid.setHgap(14);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(25);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(25);

        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        grid.add(cardMetric("card-total", "📌 TOTAL SYSTEM REPORTS", totalValue, "All issues across India"), 0, 0);
        grid.add(cardMetric("card-open", "⏱️ OPEN & UNASSIGNED", openValue, "Awaiting field officer dispatch"), 1, 0);
        grid.add(cardMetric("card-critical", "⚠️ CRITICAL PRIORITY", criticalValue, "Emergency defect hazards"), 2, 0);
        grid.add(cardMetric("card-resolved", "✅ RESOLVED & VERIFIED", resolvedValue, "Audited and verified resolutions"), 3, 0);

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

    private Node buildMainGrid() {
        VBox leftRegister = buildRegisterPanel();
        VBox rightSidebar = buildRightSidebarPanels();

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMinHeight(Region.USE_PREF_SIZE);
        grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
        grid.setMaxHeight(Double.MAX_VALUE);

        ColumnConstraints cLeft = new ColumnConstraints();
        cLeft.setPercentWidth(62);
        cLeft.setHgrow(Priority.ALWAYS);

        ColumnConstraints cRight = new ColumnConstraints();
        cRight.setPercentWidth(38);
        cRight.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(cLeft, cRight);

        RowConstraints r0 = new RowConstraints();
        r0.setVgrow(Priority.ALWAYS);
        r0.setValignment(VPos.TOP);
        grid.getRowConstraints().add(r0);

        grid.add(leftRegister, 0, 0);
        grid.add(rightSidebar, 1, 0);

        return grid;
    }

    private VBox buildRegisterPanel() {
        Label title = new Label("📋 Issue Register (Admin View)");
        title.getStyleClass().add("panel-header-title");
        Label subtitle = new Label("List of all reported civic issues across monitored districts.");
        subtitle.getStyleClass().add("panel-header-sub");

        VBox heading = new VBox(2, title, subtitle);

        searchField.setPromptText("🔍 Search by issue, location...");
        searchField.getStyleClass().add("filter-field");
        searchField.textProperty().addListener((o, oldV, newV) -> refreshTable());

        stateFilter.setPromptText("All states");
        stateFilter.getItems().setAll("All states", "Delhi", "Maharashtra", "Karnataka", "Telangana", "Tamil Nadu");
        stateFilter.getSelectionModel().selectFirst();
        stateFilter.getStyleClass().add("filter-field");
        stateFilter.setPrefWidth(100);
        stateFilter.valueProperty().addListener((o, oldV, newV) -> refreshTable());

        statusFilter.setPromptText("All statuses");
        statusFilter.getItems().setAll("All statuses", "Reported", "Verified", "Assigned", "In Progress", "Resolved");
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.getStyleClass().add("filter-field");
        statusFilter.setPrefWidth(100);
        statusFilter.valueProperty().addListener((o, oldV, newV) -> refreshTable());

        priorityFilter.setPromptText("All priorities");
        priorityFilter.getItems().setAll("All priorities", "Critical", "High", "Medium", "Low");
        priorityFilter.getSelectionModel().selectFirst();
        priorityFilter.getStyleClass().add("filter-field");
        priorityFilter.setPrefWidth(100);
        priorityFilter.valueProperty().addListener((o, oldV, newV) -> refreshTable());

        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox filterBar = new HBox(6, searchField, stateFilter, statusFilter, priorityFilter);

        // Columns
        TableColumn<Issue, String> idCol = textColumn("#", i -> String.valueOf(i.id()));
        idCol.setPrefWidth(40);

        TableColumn<Issue, String> titleCol = textColumn("ISSUE TITLE", Issue::title);
        titleCol.setPrefWidth(170);

        TableColumn<Issue, String> priorityCol = textColumn("PRIORITY", i -> i.priority().label());
        priorityCol.setPrefWidth(80);
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
        locationCol.setPrefWidth(110);

        TableColumn<Issue, String> categoryCol = textColumn("CATEGORY", Issue::category);
        categoryCol.setPrefWidth(85);

        TableColumn<Issue, String> statusCol = textColumn("STATUS", i -> i.status().label());
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(val);
                    badge.getStyleClass().add("badge-status-" + val.toLowerCase().replace(" ", ""));
                    setGraphic(badge); setText(null);
                }
            }
        });

        TableColumn<Issue, String> dateCol = textColumn("DATE", i -> i.reportedAt().format(DATE_FORMAT));
        dateCol.setPrefWidth(80);

        issueTable.getColumns().setAll(idCol, titleCol, priorityCol, locationCol, categoryCol, statusCol, dateCol);
        issueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        issueTable.getStyleClass().add("issue-table");
        issueTable.setFixedCellSize(40);
        issueTable.setPrefHeight(340);

        issueTable.getSelectionModel().selectedItemProperty().addListener((o, oldV, selected) -> updateInspector(selected));

        VBox panel = new VBox(12, heading, filterBar, issueTable);
        panel.getStyleClass().add("white-panel");
        return panel;
    }

    private VBox buildRightSidebarPanels() {
        VBox rightStack = new VBox(16);
        rightStack.setMinHeight(Region.USE_PREF_SIZE);
        rightStack.setPrefHeight(Region.USE_COMPUTED_SIZE);
        rightStack.setMaxHeight(Double.MAX_VALUE);

        inspectorPanel.getStyleClass().add("white-panel");
        inspectorPanel.setMinHeight(Region.USE_PREF_SIZE);
        updateInspector(null);

        // Hotspots Panel
        VBox hotspotsPanel = new VBox(10);
        hotspotsPanel.getStyleClass().add("white-panel");
        hotspotsPanel.setMinHeight(Region.USE_PREF_SIZE);

        Label hsTitle = new Label("📍 Problem Hotspots");
        hsTitle.getStyleClass().add("panel-header-title");
        Label hsSub = new Label("Top locations with active open reports");
        hsSub.getStyleClass().add("panel-header-sub");

        hotspotsList.setSpacing(8);
        hotspotsList.setMinHeight(Region.USE_PREF_SIZE);
        hotspotsPanel.getChildren().addAll(hsTitle, hsSub, hotspotsList);

        // Workflow Funnel Panel
        VBox workflowPanel = new VBox(10);
        workflowPanel.getStyleClass().add("white-panel");
        workflowPanel.setMinHeight(Region.USE_PREF_SIZE);
        workflowPanel.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Label wfTitle = new Label("📊 Workflow Funnel");
        wfTitle.getStyleClass().add("panel-header-title");
        Label wfSub = new Label("Current status distribution across issues");
        wfSub.getStyleClass().add("panel-header-sub");

        workflowList.setSpacing(10);
        workflowList.setMinHeight(Region.USE_PREF_SIZE);
        workflowList.setPrefHeight(Region.USE_COMPUTED_SIZE);

        workflowPanel.getChildren().addAll(wfTitle, wfSub, workflowList);

        rightStack.getChildren().addAll(inspectorPanel, hotspotsPanel, workflowPanel);
        return rightStack;
    }

    private Node buildAllIssuesContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("📋 System-Wide Issue Register");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Full administrative table of all civic defect submissions across India.");
        sub.getStyleClass().add("hero-subtitle");

        TableView<Issue> fullTable = new TableView<>(FXCollections.observableArrayList(repository.findAll()));
        fullTable.getColumns().setAll(
                textColumn("#", i -> String.valueOf(i.id())),
                textColumn("Title", Issue::title),
                textColumn("Category", Issue::category),
                textColumn("Priority", i -> i.priority().label()),
                textColumn("Status", i -> i.status().label()),
                textColumn("Department", Issue::department),
                textColumn("Location", Issue::locationLabel),
                textColumn("Reporter", Issue::reportedBy)
        );
        fullTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        fullTable.setPrefHeight(450);

        VBox panel = new VBox(12, new VBox(2, title, sub), fullTable);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildUsersContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("👥 User Directory");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Registered Citizens, District Officers and Administrators from MySQL database.");
        sub.getStyleClass().add("hero-subtitle");

        ObservableList<User> usersList = FXCollections.observableArrayList(fetchUsersFromMySql());
        TableView<User> userTable = new TableView<>(usersList);

        TableColumn<User, String> idCol = userTextColumn("#", u -> String.valueOf(u.id()));
        TableColumn<User, String> nameCol = userTextColumn("FULL NAME", User::fullName);
        TableColumn<User, String> userCol = userTextColumn("USERNAME", User::username);
        TableColumn<User, String> emailCol = userTextColumn("EMAIL", User::email);
        TableColumn<User, String> phoneCol = userTextColumn("PHONE", User::phoneNumber);
        TableColumn<User, String> roleCol = userTextColumn("ROLE", User::role);

        roleCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(role);
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        badge.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10px; -fx-font-weight: bold;");
                    } else if ("OFFICER".equalsIgnoreCase(role)) {
                        badge.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge); setText(null);
                }
            }
        });

        userTable.getColumns().setAll(idCol, nameCol, userCol, emailCol, phoneCol, roleCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        userTable.setPrefHeight(420);

        VBox panel = new VBox(12, new VBox(2, title, sub), userTable);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildOfficersContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("👮 District Field Officers");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Assigned officers across Municipal Corporations & Departments.");
        sub.getStyleClass().add("hero-subtitle");

        List<User> officers = fetchUsersFromMySql().stream().filter(u -> "OFFICER".equalsIgnoreCase(u.role())).toList();
        TableView<User> officerTable = new TableView<>(FXCollections.observableArrayList(officers));

        officerTable.getColumns().setAll(
                userTextColumn("#", u -> String.valueOf(u.id())),
                userTextColumn("OFFICER NAME", User::fullName),
                userTextColumn("USERNAME", User::username),
                userTextColumn("CONTACT EMAIL", User::email),
                userTextColumn("PHONE", User::phoneNumber)
        );
        officerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        officerTable.setPrefHeight(380);

        VBox panel = new VBox(12, new VBox(2, title, sub), officerTable);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private Node buildAssignContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("📌 Issue Assignment & Officer Dispatch");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Select open unassigned issues and assign to specific department officers.");
        sub.getStyleClass().add("hero-subtitle");

        List<Issue> unassigned = repository.findAll().stream().filter(i -> i.status() != IssueStatus.RESOLVED).toList();
        TableView<Issue> assignTable = new TableView<>(FXCollections.observableArrayList(unassigned));

        assignTable.getColumns().setAll(
                textColumn("#", i -> String.valueOf(i.id())),
                textColumn("TITLE", Issue::title),
                textColumn("PRIORITY", i -> i.priority().label()),
                textColumn("STATUS", i -> i.status().label()),
                textColumn("DEPARTMENT", Issue::department),
                textColumn("LOCATION", Issue::locationLabel)
        );
        assignTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        assignTable.setPrefHeight(360);

        Button btnAssignNow = new Button("⚡ Dispatch to Officer Rohan Mehta (PWD)");
        btnAssignNow.getStyleClass().add("btn-report-new");
        btnAssignNow.setOnAction(e -> {
            Issue selected = assignTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                repository.updateStatus(selected.id(), IssueStatus.ASSIGNED);
                refreshAll();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Issue #" + selected.id() + " has been assigned to Officer Rohan Mehta.", ButtonType.OK);
                alert.setHeaderText("Issue Assigned");
                alert.showAndWait();
            }
        });

        VBox panel = new VBox(12, new VBox(2, title, sub), assignTable, btnAssignNow);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private List<User> fetchUsersFromMySql() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, email, full_name, phone_number, role FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone_number"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) {
            // Fallback default users if query fails
            list.add(new User(1L, "citizen1", "citizen@jandrishti.gov.in", "Asha Verma", "+91 98765 43210", "CITIZEN"));
            list.add(new User(2L, "officer1", "pwd@jandrishti.gov.in", "Rohan Mehta", "+91 98111 22233", "OFFICER"));
            list.add(new User(3L, "admin", "admin@jandrishti.gov.in", "System Administrator", "+91 99000 11223", "ADMIN"));
        }
        return list;
    }

    private void updateInspector(Issue issue) {
        inspectorPanel.getChildren().clear();

        Label title = new Label("🔍 Issue Detail Inspector");
        title.getStyleClass().add("panel-header-title");

        if (issue == null) {
            Label placeholder = new Label("Select any issue from the register to inspect details, update status or dispatch to department.");
            placeholder.getStyleClass().add("panel-header-sub");
            placeholder.setWrapText(true);
            inspectorPanel.getChildren().addAll(title, placeholder);
            return;
        }

        Label heading = new Label("#" + issue.id() + " • " + issue.title());
        heading.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 800; -fx-font-size: 12px;");
        heading.setWrapText(true);

        Label desc = new Label("\"" + issue.description() + "\"");
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
        desc.setWrapText(true);

        Label loc = new Label("📍 " + issue.fullAddressLabel());
        loc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        loc.setWrapText(true);

        Label dept = new Label("🏢 " + issue.department());
        dept.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        Button btnSetVerified = new Button("Mark Verified");
        btnSetVerified.getStyleClass().add("btn-ask-ai");
        btnSetVerified.setOnAction(e -> {
            repository.updateStatus(issue.id(), IssueStatus.VERIFIED);
            refreshAll();
        });

        Button btnSetResolved = new Button("Mark Resolved");
        btnSetResolved.getStyleClass().add("btn-report-new");
        btnSetResolved.setOnAction(e -> {
            repository.updateStatus(issue.id(), IssueStatus.RESOLVED);
            refreshAll();
        });

        HBox actions = new HBox(6, btnSetVerified, btnSetResolved);

        inspectorPanel.getChildren().addAll(title, heading, desc, loc, dept, actions);
    }

    private TableColumn<Issue, String> textColumn(String title, java.util.function.Function<Issue, String> mapper) {
        TableColumn<Issue, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }

    private TableColumn<User, String> userTextColumn(String title, java.util.function.Function<User, String> mapper) {
        TableColumn<User, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(mapper.apply(data.getValue())));
        return col;
    }

    private void refreshAll() {
        List<Issue> all = repository.findAll();
        totalValue.setText(String.valueOf(all.size()));
        openValue.setText(String.valueOf(all.stream().filter(i -> i.status() != IssueStatus.RESOLVED).count()));
        criticalValue.setText(String.valueOf(all.stream().filter(i -> i.priority() == IssuePriority.CRITICAL).count()));
        resolvedValue.setText(String.valueOf(all.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count()));

        refreshTable();
        updateHotspots(all);
        updateWorkflowFunnel(all);
    }

    private void updateHotspots(List<Issue> all) {
        hotspotsList.getChildren().clear();
        Map<String, Long> hsCounts = all.stream()
                .filter(i -> i.status() != IssueStatus.RESOLVED)
                .collect(Collectors.groupingBy(Issue::locationLabel, Collectors.counting()));

        if (hsCounts.isEmpty()) {
            Label emptyLbl = new Label("No active open hotspots.");
            emptyLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
            hotspotsList.getChildren().add(emptyLbl);
            return;
        }

        hsCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .forEach(e -> {
                    Label name = new Label(e.getKey());
                    name.setStyle("-fx-text-fill: #334155; -fx-font-size: 11px; -fx-font-weight: 600;");
                    Label pill = new Label(e.getValue() + " open");
                    pill.getStyleClass().add("hotspot-pill");
                    HBox row = new HBox(name, pill);
                    row.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(name, Priority.ALWAYS);
                    hotspotsList.getChildren().add(row);
                });
    }

    private void updateWorkflowFunnel(List<Issue> all) {
        workflowList.getChildren().clear();
        long total = all.size();

        Label totalLbl = new Label("Total Issues: " + total);
        totalLbl.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 13px; -fx-font-weight: 800; -fx-padding: 0 0 4 0;");
        workflowList.getChildren().add(totalLbl);

        long reportedCount = all.stream().filter(i -> i.status() == IssueStatus.REPORTED).count();
        long verifiedCount = all.stream().filter(i -> i.status() == IssueStatus.VERIFIED).count();
        long assignedCount = all.stream().filter(i -> i.status() == IssueStatus.ASSIGNED).count();
        long inProgressCount = all.stream().filter(i -> i.status() == IssueStatus.IN_PROGRESS).count();
        long resolvedCount = all.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count();
        long citizenVerifiedCount = all.stream().filter(Issue::citizenVerified).count();

        // Render all 6 workflow stages with database-driven counts & visual progress bars
        addFunnelStage("Reported", "dot-reported", "#f43f5e", reportedCount, total);
        addFunnelStage("Verified", "dot-verified", "#10b981", verifiedCount, total);
        addFunnelStage("Assigned", "dot-assigned", "#3b82f6", assignedCount, total);
        addFunnelStage("In Progress", "dot-inprogress", "#f59e0b", inProgressCount, total);
        addFunnelStage("Resolved", "dot-resolved", "#059669", resolvedCount, total);
        addFunnelStage("Citizen Verified", "dot-verified", "#8b5cf6", citizenVerifiedCount, total);
    }

    private void addFunnelStage(String name, String dotClass, String hexColor, long count, long total) {
        Circle dot = new Circle(4);
        dot.getStyleClass().add(dotClass);
        dot.setStyle("-fx-fill: " + hexColor + ";");

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 11px; -fx-font-weight: 600;");

        HBox left = new HBox(6, dot, nameLbl);
        left.setAlignment(Pos.CENTER_LEFT);

        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 700; -fx-font-size: 11px;");

        HBox topRow = new HBox(left, countLbl);
        topRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        double progress = total > 0 ? (double) count / total : 0.0;
        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(4);
        bar.setStyle("-fx-accent: " + hexColor + "; -fx-control-inner-background: #f1f5f9;");

        VBox stageBox = new VBox(2, topRow, bar);
        stageBox.setMinHeight(Region.USE_PREF_SIZE);

        workflowList.getChildren().add(stageBox);
    }

    private void refreshTable() {
        String query = searchField.getText();
        String st = stateFilter.getValue();
        String statusVal = statusFilter.getValue();
        String priorityVal = priorityFilter.getValue();

        IssueStatus statusEnum = (statusVal == null || statusVal.startsWith("All")) ? null : parseStatus(statusVal);
        IssuePriority priorityEnum = (priorityVal == null || priorityVal.startsWith("All")) ? null : IssuePriority.fromString(priorityVal);

        visibleIssues.setAll(repository.search(query, st, statusEnum, priorityEnum));
    }

    private IssueStatus parseStatus(String text) {
        for (IssueStatus s : IssueStatus.values()) {
            if (s.label().equalsIgnoreCase(text)) return s;
        }
        return null;
    }
}
