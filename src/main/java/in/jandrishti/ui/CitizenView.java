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

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CitizenView extends BorderPane {
    private final IssueRepository repository;
    private final User citizenUser;
    private final Runnable onLogoutCallback;
    private final AiChatService aiService;

    // Sidebar navigation buttons
    private final Button btnNavDashboard = new Button("🏠   Dashboard");
    private final Button btnNavReport = new Button("✏️   Report Issue");
    private final Button btnNavMyIssues = new Button("📋   My Issues");
    private final Button btnNavTrack = new Button("🔍   Track Issue");
    private final Button btnNavNotifs = new Button("🔔   Notifications");
    private final Button btnNavProfile = new Button("👤   Profile");

    // Center views
    private final StackPane mainStack = new StackPane();
    private final ScrollPane dashboardScroll = new ScrollPane();
    private final IssueSubmitView submitView;
    private final MyIssuesView myIssuesView;
    private final ScrollPane trackScroll = new ScrollPane();
    private final ScrollPane notifsScroll = new ScrollPane();
    private final ProfileView profileView;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CitizenView(IssueRepository repository, User citizenUser, Runnable onLogoutCallback) {
        this.repository = repository;
        this.citizenUser = citizenUser;
        this.onLogoutCallback = onLogoutCallback;
        this.aiService = new AiChatService(repository);

        this.submitView = new IssueSubmitView(repository, aiService,
                newIssue -> switchNavView(2),
                () -> switchNavView(0)
        );

        this.myIssuesView = new MyIssuesView(repository);
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

        Label logoSub = new Label("Citizen Civic Portal");
        logoSub.getStyleClass().add("brand-subtitle");

        VBox titleBox = new VBox(1, logoText, logoSub);
        HBox brandBox = new HBox(10, logoIcon, titleBox);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label roleBadge = new Label("👤 Active Citizen");
        roleBadge.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: 800; -fx-padding: 4 10; -fx-background-radius: 12px; -fx-font-size: 11px;");

        String name = citizenUser != null ? citizenUser.fullName() : "Asha Verma";
        Button userMenu = new Button("👤  Welcome, " + name + " ▾");
        userMenu.getStyleClass().add("user-profile-menu");
        userMenu.setOnAction(e -> switchNavView(5));

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
        btnNavReport.getStyleClass().add("sidebar-button");
        btnNavMyIssues.getStyleClass().add("sidebar-button");
        btnNavTrack.getStyleClass().add("sidebar-button");
        btnNavNotifs.getStyleClass().add("sidebar-button");
        btnNavProfile.getStyleClass().add("sidebar-button");

        btnNavDashboard.setOnAction(e -> switchNavView(0));
        btnNavReport.setOnAction(e -> switchNavView(1));
        btnNavMyIssues.setOnAction(e -> switchNavView(2));
        btnNavTrack.setOnAction(e -> switchNavView(3));
        btnNavNotifs.setOnAction(e -> switchNavView(4));
        btnNavProfile.setOnAction(e -> switchNavView(5));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("sidebar-button");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(
                btnNavDashboard,
                btnNavReport,
                btnNavMyIssues,
                btnNavTrack,
                new Separator(),
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
        btnNavReport.getStyleClass().remove("sidebar-button-active");
        btnNavMyIssues.getStyleClass().remove("sidebar-button-active");
        btnNavTrack.getStyleClass().remove("sidebar-button-active");
        btnNavNotifs.getStyleClass().remove("sidebar-button-active");
        btnNavProfile.getStyleClass().remove("sidebar-button-active");

        dashboardScroll.setVisible(false);
        submitView.setVisible(false);
        myIssuesView.setVisible(false);
        trackScroll.setVisible(false);
        notifsScroll.setVisible(false);
        profileView.setVisible(false);

        switch (index) {
            case 0 -> {
                btnNavDashboard.getStyleClass().add("sidebar-button-active");
                dashboardScroll.setVisible(true);
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
                btnNavTrack.getStyleClass().add("sidebar-button-active");
                trackScroll.setContent(buildTrackContent());
                trackScroll.setVisible(true);
            }
            case 4 -> {
                btnNavNotifs.getStyleClass().add("sidebar-button-active");
                notifsScroll.setContent(buildNotifsContent());
                notifsScroll.setVisible(true);
            }
            case 5 -> {
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
                buildRecentSubmissionsPanel()
        );

        setupScroll(dashboardScroll, container);
        setupScroll(trackScroll, buildTrackContent());
        setupScroll(notifsScroll, buildNotifsContent());

        mainStack.getChildren().addAll(
                dashboardScroll,
                submitView,
                myIssuesView,
                trackScroll,
                notifsScroll,
                profileView
        );

        submitView.setVisible(false);
        myIssuesView.setVisible(false);
        trackScroll.setVisible(false);
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
        Label title = new Label("Welcome to Your Citizen Action Center");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Report civic issues, track real-time field repairs, and verify resolved issues in your neighborhood.");
        subtitle.getStyleClass().add("hero-subtitle");

        VBox textGroup = new VBox(3, title, subtitle);

        Button reportBtn = new Button("🚀 Report New Issue");
        reportBtn.getStyleClass().add("btn-report-new");
        reportBtn.setOnAction(e -> switchNavView(1));

        HBox btnGroup = new HBox(10, reportBtn);
        btnGroup.setAlignment(Pos.CENTER_RIGHT);

        HBox hero = new HBox(textGroup, btnGroup);
        hero.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        return hero;
    }

    private Node buildMetricsRow() {
        List<Issue> myIssues = repository.findAll();
        long total = myIssues.size();
        long open = myIssues.stream().filter(i -> i.status() != IssueStatus.RESOLVED).count();
        long resolved = myIssues.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count();

        GridPane grid = new GridPane();
        grid.setHgap(14);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(33.3);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(33.3);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(33.4);

        grid.getColumnConstraints().addAll(c1, c2, c3);

        grid.add(cardMetric("card-total", "📌 TOTAL MY REPORTS", new Label(String.valueOf(total)), "Defects logged by you"), 0, 0);
        grid.add(cardMetric("card-open", "⏱️ IN PROGRESS / OPEN", new Label(String.valueOf(open)), "Under field officer repair"), 1, 0);
        grid.add(cardMetric("card-resolved", "✅ RESOLVED & VERIFIED", new Label(String.valueOf(resolved)), "Successfully repaired"), 2, 0);

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

    private Node buildRecentSubmissionsPanel() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("white-panel");

        Label title = new Label("📋 My Reported Issues");
        title.getStyleClass().add("panel-header-title");
        Label sub = new Label("Track defect repair progress and give citizen feedback");
        sub.getStyleClass().add("panel-header-sub");

        ObservableList<Issue> myIssuesList = FXCollections.observableArrayList(repository.findAll());
        TableView<Issue> table = new TableView<>(myIssuesList);

        TableColumn<Issue, String> idCol = textColumn("#", i -> String.valueOf(i.id()));
        TableColumn<Issue, String> titleCol = textColumn("ISSUE TITLE", Issue::title);
        TableColumn<Issue, String> catCol = textColumn("CATEGORY", Issue::category);
        TableColumn<Issue, String> locCol = textColumn("LOCATION", Issue::locationLabel);
        TableColumn<Issue, String> statusCol = textColumn("STATUS", i -> i.status().label());
        TableColumn<Issue, String> dateCol = textColumn("REPORTED AT", i -> i.reportedAt().format(DATE_FORMAT));

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

        table.getColumns().setAll(idCol, titleCol, catCol, locCol, statusCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(320);

        panel.getChildren().addAll(new VBox(2, title, sub), table);
        return panel;
    }

    private Node buildTrackContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🔍 Track Issue Status & Repair Progress");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Select your reported issue to view real-time department audit history.");
        sub.getStyleClass().add("hero-subtitle");

        List<Issue> issues = repository.findAll();
        ComboBox<Issue> issueCombo = new ComboBox<>(FXCollections.observableArrayList(issues));
        if (!issues.isEmpty()) issueCombo.getSelectionModel().selectFirst();
        issueCombo.getStyleClass().add("filter-field");
        issueCombo.setStyle("-fx-pref-width: 400px;");

        VBox timelineBox = new VBox(12);
        timelineBox.getStyleClass().add("white-panel");

        issueCombo.valueProperty().addListener((o, oldV, selected) -> updateTimeline(timelineBox, selected));
        updateTimeline(timelineBox, issueCombo.getValue());

        VBox panel = new VBox(14, new VBox(2, title, sub), new HBox(10, new Label("Select Issue:"), issueCombo), timelineBox);
        panel.getStyleClass().add("white-panel");
        box.getChildren().add(panel);
        return box;
    }

    private void updateTimeline(VBox box, Issue issue) {
        box.getChildren().clear();
        if (issue == null) {
            box.getChildren().add(new Label("No issue selected."));
            return;
        }

        Label heading = new Label("Issue #" + issue.id() + " - " + issue.title());
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label desc = new Label(issue.description());
        desc.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");

        Label currentStatus = new Label("Current Status: " + issue.status().label());
        currentStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");

        VBox steps = new VBox(10);
        steps.setStyle("-fx-padding: 10 0 0 10;");

        addStep(steps, "1. Issue Reported", "Logged by citizen with location coordinates", true);
        addStep(steps, "2. AI Auto-Triage Assessment", "Assigned priority level: " + issue.priority().label(), true);
        addStep(steps, "3. Officer Assignment & Repair", "Department: " + issue.department(), issue.status() != IssueStatus.REPORTED);
        addStep(steps, "4. Verification & Resolution", "Resolution confirmation & citizen audit", issue.status() == IssueStatus.RESOLVED);

        Button btnConfirmVerify = new Button("✅ Confirm Resolution & Give 5-Star Citizen Rating");
        btnConfirmVerify.getStyleClass().add("btn-report-new");
        btnConfirmVerify.setDisable(issue.status() != IssueStatus.RESOLVED);
        btnConfirmVerify.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Thank you! Your resolution confirmation and rating have been recorded.", ButtonType.OK);
            alert.setHeaderText("Resolution Verified");
            alert.showAndWait();
        });

        box.getChildren().addAll(heading, desc, currentStatus, new Separator(), steps, new Separator(), btnConfirmVerify);
    }

    private void addStep(VBox container, String stepTitle, String desc, boolean active) {
        Label t = new Label(stepTitle);
        t.setStyle("-fx-font-weight: 800; -fx-font-size: 13px; -fx-text-fill: " + (active ? "#16a34a;" : "#94a3b8;"));
        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (active ? "#334155;" : "#cbd5e1;"));
        VBox step = new VBox(2, t, d);
        step.setStyle("-fx-border-color: " + (active ? "#bbf7d0;" : "#f1f5f9;") + "; -fx-border-width: 0 0 0 3px; -fx-padding: 0 0 0 10px;");
        container.getChildren().add(step);
    }

    private Node buildNotifsContent() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(20));

        Label title = new Label("🔔 Notifications & Alerts");
        title.getStyleClass().add("hero-title");
        Label sub = new Label("Live updates on your reported defects.");
        sub.getStyleClass().add("hero-subtitle");

        VBox list = new VBox(10);
        list.getStyleClass().add("white-panel");

        addNotif(list, "✅ Issue #1042 Verified", "Public Works Dept inspects Karol Bagh pothole.", "2 hours ago");
        addNotif(list, "⚡ Status Updated to Assigned", "Traffic Signal defect assigned to Traffic Police Cell.", "5 hours ago");
        addNotif(list, "🎉 Issue #1039 Resolved", "Community bin overflow cleared by GHMC Sanitation Dept.", "1 day ago");

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
        myIssuesView.refresh();
    }
}
