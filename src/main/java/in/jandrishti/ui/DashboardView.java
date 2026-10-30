package in.jandrishti.ui;

import in.jandrishti.data.AiChatService;
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
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DashboardView extends BorderPane {
    private final IssueRepository repository;
    private final AiChatService aiService;

    private final ObservableList<Issue> visibleIssues = FXCollections.observableArrayList();
    private final TableView<Issue> issueTable = new TableView<>(visibleIssues);

    private final Label totalValue = new Label("12");
    private final Label openValue = new Label("8");
    private final Label criticalValue = new Label("3");
    private final Label resolvedValue = new Label("2");

    private final ComboBox<String> stateFilter = new ComboBox<>();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final TextField searchField = new TextField();

    // Sidebar navigation buttons
    private final Button btnNavDashboard = new Button("🏠   Dashboard");
    private final Button btnNavReport = new Button("✏️   Report Issue");
    private final Button btnNavMyIssues = new Button("📋   My Issues");
    private final Button btnNavProfile = new Button("👤   Profile");
    private final Button btnNavAiChat = new Button("🤖   Ask AI Copilot");
    private final Button btnNavAnalytics = new Button("📊   Analytics");

    // Center content views
    private final StackPane mainStack = new StackPane();
    private final ScrollPane dashboardMainScroll = new ScrollPane();
    private final IssueSubmitView submitView;
    private final MyIssuesView myIssuesView;
    private final ProfileView profileView;
    private final AiChatBoardView aiChatView;
    private final AnalyticsView analyticsView;

    // Issue Drawer Inspector
    private final VBox inspectorPanel = new VBox(10);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final User activeUser;
    private final Runnable onLogoutCallback;

    public DashboardView(IssueRepository repository) {
        this(repository, null, null);
    }

    public DashboardView(IssueRepository repository, User activeUser, Runnable onLogoutCallback) {
        this.repository = repository;
        this.activeUser = activeUser;
        this.onLogoutCallback = onLogoutCallback;
        this.aiService = new AiChatService(repository);

        this.submitView = new IssueSubmitView(repository, aiService,
                newIssue -> {
                    refreshAll();
                    switchNavView(0);
                },
                () -> switchNavView(0)
        );

        this.myIssuesView = new MyIssuesView(repository);
        this.profileView = new ProfileView(repository);

        this.aiChatView = new AiChatBoardView(aiService, issueId -> {
            switchNavView(0);
            selectIssueById(issueId);
        });

        this.analyticsView = new AnalyticsView(repository);

        getStyleClass().add("app-shell");

        setTop(buildTopHeader());
        setLeft(buildSidebar());
        setCenter(buildCenterContent());
        setBottom(buildFooter());

        refreshAll();
    }

    private Node buildTopHeader() {
        Label logoIcon = new Label("👁");
        logoIcon.getStyleClass().add("brand-logo-icon");

        Label logoText = new Label("JanDrishti");
        logoText.getStyleClass().add("brand-title");

        Label logoSub = new Label("National Civic Issue Monitoring System");
        logoSub.getStyleClass().add("brand-subtitle");

        VBox titleBox = new VBox(1, logoText, logoSub);
        HBox brandBox = new HBox(10, logoIcon, titleBox);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        String userLabelText = activeUser != null ? "👤  Welcome, " + activeUser.fullName() + " (" + activeUser.role() + ") ▾" : "👤  Welcome, Citizen ▾";
        Button userMenu = new Button(userLabelText);
        userMenu.getStyleClass().add("user-profile-menu");
        userMenu.setOnAction(e -> switchNavView(3));

        HBox topBar = new HBox(brandBox, userMenu);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);
        topBar.getStyleClass().add("top-header");

        return topBar;
    }

    private Node buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("left-sidebar");

        btnNavDashboard.getStyleClass().addAll("sidebar-button", "sidebar-button-active");
        btnNavReport.getStyleClass().add("sidebar-button");
        btnNavMyIssues.getStyleClass().add("sidebar-button");
        btnNavProfile.getStyleClass().add("sidebar-button");
        btnNavAiChat.getStyleClass().add("sidebar-button");
        btnNavAnalytics.getStyleClass().add("sidebar-button");

        btnNavDashboard.setOnAction(e -> switchNavView(0));
        btnNavReport.setOnAction(e -> switchNavView(1));
        btnNavMyIssues.setOnAction(e -> switchNavView(2));
        btnNavProfile.setOnAction(e -> switchNavView(3));
        btnNavAiChat.setOnAction(e -> switchNavView(4));
        btnNavAnalytics.setOnAction(e -> switchNavView(5));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("sidebar-button");
        logoutBtn.setOnAction(e -> {
            if (onLogoutCallback != null) {
                onLogoutCallback.run();
            } else {
                switchNavView(0);
            }
        });

        sidebar.getChildren().addAll(
                btnNavDashboard,
                btnNavReport,
                btnNavMyIssues,
                btnNavProfile,
                new Separator(),
                btnNavAiChat,
                btnNavAnalytics,
                spacer,
                logoutBtn
        );
        return sidebar;
    }

    private void switchNavView(int index) {
        btnNavDashboard.getStyleClass().remove("sidebar-button-active");
        btnNavReport.getStyleClass().remove("sidebar-button-active");
        btnNavMyIssues.getStyleClass().remove("sidebar-button-active");
        btnNavProfile.getStyleClass().remove("sidebar-button-active");
        btnNavAiChat.getStyleClass().remove("sidebar-button-active");
        btnNavAnalytics.getStyleClass().remove("sidebar-button-active");

        dashboardMainScroll.setVisible(false);
        submitView.setVisible(false);
        myIssuesView.setVisible(false);
        profileView.setVisible(false);
        aiChatView.setVisible(false);
        analyticsView.setVisible(false);

        switch (index) {
            case 0 -> {
                btnNavDashboard.getStyleClass().add("sidebar-button-active");
                dashboardMainScroll.setVisible(true);
                refreshAll();
            }
            case 1 -> {
                btnNavReport.getStyleClass().add("sidebar-button-active");
                submitView.setVisible(true);
            }
            case 2 -> {
                btnNavMyIssues.getStyleClass().add("sidebar-button-active");
                myIssuesView.refresh();
                myIssuesView.setVisible(true);
            }
            case 3 -> {
                btnNavProfile.getStyleClass().add("sidebar-button-active");
                profileView.setVisible(true);
            }
            case 4 -> {
                btnNavAiChat.getStyleClass().add("sidebar-button-active");
                aiChatView.setVisible(true);
            }
            case 5 -> {
                btnNavAnalytics.getStyleClass().add("sidebar-button-active");
                analyticsView.setVisible(true);
            }
        }
    }

    private Node buildCenterContent() {
        VBox container = new VBox(18);
        container.getStyleClass().add("content-container");

        container.getChildren().addAll(
                buildHeroHeader(),
                buildMetricsRow(),
                buildMainGrid()
        );

        dashboardMainScroll.setContent(container);
        dashboardMainScroll.setFitToWidth(true);
        dashboardMainScroll.setFitToHeight(false);
        dashboardMainScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dashboardMainScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dashboardMainScroll.getStyleClass().add("main-scroll-pane");

        mainStack.getChildren().addAll(
                dashboardMainScroll,
                submitView,
                myIssuesView,
                profileView,
                aiChatView,
                analyticsView
        );

        submitView.setVisible(false);
        myIssuesView.setVisible(false);
        profileView.setVisible(false);
        aiChatView.setVisible(false);
        analyticsView.setVisible(false);

        return mainStack;
    }

    private Node buildHeroHeader() {
        Label title = new Label("Where India Needs Attention");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Report civic issues, track progress and help build a cleaner, safer and better India.");
        subtitle.getStyleClass().add("hero-subtitle");

        VBox textGroup = new VBox(3, title, subtitle);

        Button reportBtn = new Button("+ Report New Issue");
        reportBtn.getStyleClass().add("btn-report-new");
        reportBtn.setOnAction(e -> switchNavView(1));

        Button askAiBtn = new Button("🤖 Ask AI Copilot");
        askAiBtn.getStyleClass().add("btn-ask-ai");
        askAiBtn.setOnAction(e -> switchNavView(4));

        HBox btnGroup = new HBox(10, reportBtn, askAiBtn);
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

        grid.add(cardMetric("card-total", "📌 TOTAL REPORTS", totalValue, "All reported issues"), 0, 0);
        grid.add(cardMetric("card-open", "⏱️ OPEN ISSUES", openValue, "Awaiting field team action"), 1, 0);
        grid.add(cardMetric("card-critical", "⚠️ CRITICAL PRIORITY", criticalValue, "Emergency hazard level"), 2, 0);
        grid.add(cardMetric("card-resolved", "✅ RESOLVED & VERIFIED", resolvedValue, "Closed with citizen confirmation"), 3, 0);

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

        ColumnConstraints cLeft = new ColumnConstraints();
        cLeft.setPercentWidth(62);
        cLeft.setHgrow(Priority.ALWAYS);

        ColumnConstraints cRight = new ColumnConstraints();
        cRight.setPercentWidth(38);
        cRight.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(cLeft, cRight);
        grid.add(leftRegister, 0, 0);
        grid.add(rightSidebar, 1, 0);

        return grid;
    }

    private VBox buildRegisterPanel() {
        Label title = new Label("📋 Issue Register");
        title.getStyleClass().add("panel-header-title");

        Label subtitle = new Label("List of all civic issues across monitored districts.");
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
        statusFilter.getItems().setAll("All statuses", "Reported", "Verified", "Assigned", "Resolved");
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

        // Table Columns
        TableColumn<Issue, String> idCol = textColumn("#", i -> String.valueOf(i.id()));
        idCol.setPrefWidth(35);

        TableColumn<Issue, String> titleCol = textColumn("ISSUE TITLE", Issue::title);
        titleCol.setPrefWidth(180);

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
        locationCol.setPrefWidth(120);

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
                    badge.getStyleClass().add("badge-status-" + val.toLowerCase());
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
        VBox.setVgrow(issueTable, Priority.ALWAYS);

        return panel;
    }

    private VBox buildRightSidebarPanels() {
        VBox rightStack = new VBox(14);

        // Panel 1: Inspector
        inspectorPanel.getStyleClass().add("white-panel");
        updateInspector(null);

        // Panel 2: Hotspots
        VBox hotspotsPanel = new VBox(10);
        hotspotsPanel.getStyleClass().add("white-panel");

        Label hsTitle = new Label("📍 Problem Hotspots");
        hsTitle.getStyleClass().add("panel-header-title");
        Label hsSub = new Label("Top locations with active open reports");
        hsSub.getStyleClass().add("panel-header-sub");

        VBox hsList = new VBox(8);
        Map<String, Long> hsCounts = repository.findAll().stream()
                .filter(i -> i.status() != IssueStatus.RESOLVED)
                .collect(Collectors.groupingBy(Issue::locationLabel, Collectors.counting()));

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
                    hsList.getChildren().add(row);
                });

        hotspotsPanel.getChildren().addAll(hsTitle, hsSub, hsList);

        // Panel 3: Workflow Funnel
        VBox workflowPanel = new VBox(10);
        workflowPanel.getStyleClass().add("white-panel");

        Label wfTitle = new Label("📊 Workflow Funnel");
        wfTitle.getStyleClass().add("panel-header-title");
        Label wfSub = new Label("Current status distribution across issues");
        wfSub.getStyleClass().add("panel-header-sub");

        VBox wfList = new VBox(7);
        Map<IssueStatus, Long> statusCounts = repository.findAll().stream()
                .collect(Collectors.groupingBy(Issue::status, Collectors.counting()));

        for (IssueStatus status : IssueStatus.values()) {
            long count = statusCounts.getOrDefault(status, 0L);

            Circle dot = new Circle(4);
            switch (status) {
                case REPORTED -> dot.getStyleClass().add("dot-reported");
                case VERIFIED -> dot.getStyleClass().add("dot-verified");
                case ASSIGNED -> dot.getStyleClass().add("dot-assigned");
                case RESOLVED -> dot.getStyleClass().add("dot-resolved");
            }

            Label name = new Label(status.label());
            name.setStyle("-fx-text-fill: #334155; -fx-font-size: 11px;");

            HBox left = new HBox(6, dot, name);
            left.setAlignment(Pos.CENTER_LEFT);

            Label val = new Label(String.valueOf(count));
            val.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: 700; -fx-font-size: 11px;");

            HBox row = new HBox(left, val);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(left, Priority.ALWAYS);
            wfList.getChildren().add(row);
        }

        workflowPanel.getChildren().addAll(wfTitle, wfSub, wfList);

        rightStack.getChildren().addAll(inspectorPanel, hotspotsPanel, workflowPanel);
        return rightStack;
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

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Reported", "Verified", "Assigned", "Resolved"));
        statusCombo.setValue(issue.status().label());
        statusCombo.getStyleClass().add("filter-field");

        Button updateBtn = new Button("Update Status");
        updateBtn.getStyleClass().add("btn-ask-ai");
        updateBtn.setOnAction(e -> {
            IssueStatus newStatus = IssueStatus.values()[statusCombo.getSelectionModel().getSelectedIndex()];
            repository.updateStatus(issue.id(), newStatus);
            refreshAll();
            selectIssueById(issue.id());
        });

        HBox statusRow = new HBox(8, statusCombo, updateBtn);

        inspectorPanel.getChildren().addAll(title, heading, desc, loc, dept, new Separator(), statusRow);
    }

    private Node buildFooter() {
        Label text = new Label("JanDrishti  |  National Civic Issue Monitoring System");
        text.getStyleClass().add("footer-text");

        Label ver = new Label("v1.0");
        ver.getStyleClass().add("footer-text");

        HBox footer = new HBox(text, ver);
        footer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
        footer.getStyleClass().add("app-footer");

        return footer;
    }

    private TableColumn<Issue, String> textColumn(String heading, Function<Issue, String> value) {
        TableColumn<Issue, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(value.apply(cell.getValue())));
        return column;
    }

    private void refreshAll() {
        List<Issue> issues = repository.findAll();
        long open = issues.stream().filter(i -> i.status() != IssueStatus.RESOLVED).count();
        long critical = issues.stream().filter(i -> i.priority() == IssuePriority.CRITICAL && i.status() != IssueStatus.RESOLVED).count();
        long resolved = issues.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count();

        totalValue.setText(String.valueOf(issues.size()));
        openValue.setText(String.valueOf(open));
        criticalValue.setText(String.valueOf(critical));
        resolvedValue.setText(String.valueOf(resolved));

        refreshTable();
    }

    private void refreshTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String state = stateFilter.getValue();
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        IssuePriority pEnum = (priority == null || priority.equals("All priorities")) ? null : IssuePriority.fromString(priority);

        List<Issue> filtered = repository.findAll().stream().filter(issue ->
                        (state == null || state.equals("All states") || issue.state().equalsIgnoreCase(state)) &&
                                (status == null || status.equals("All statuses") || issue.status().label().equalsIgnoreCase(status)) &&
                                (pEnum == null || issue.priority() == pEnum) &&
                                (search.isBlank() ||
                                        String.valueOf(issue.id()).contains(search) ||
                                        (issue.title() + issue.locationLabel() + issue.road() + issue.category()).toLowerCase().contains(search)))
                .sorted(Comparator.comparing(Issue::reportedAt).reversed()).toList();

        visibleIssues.setAll(filtered);
    }

    public void selectIssueById(long id) {
        repository.findById(id).ifPresent(issue -> {
            issueTable.getSelectionModel().select(issue);
            issueTable.scrollTo(issue);
        });
    }
}
