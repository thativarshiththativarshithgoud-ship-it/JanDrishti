package in.jandrishti.ui;

import in.jandrishti.data.IssueRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public final class ProfileView extends ScrollPane {
    private final IssueRepository repository;

    public ProfileView(IssueRepository repository) {
        this.repository = repository;
        setFitToWidth(true);
        getStyleClass().add("main-scroll-pane");
        setContent(buildContent());
    }

    private Node buildContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24, 30, 30, 30));

        Label title = new Label("👤 User Profile & Credentials");
        title.getStyleClass().add("hero-title");

        Label sub = new Label("Manage your account details, operational district, and citizen verification badge.");
        sub.getStyleClass().add("hero-subtitle");

        VBox header = new VBox(4, title, sub);

        // Profile Card
        VBox card = new VBox(16);
        card.getStyleClass().add("white-panel");

        Label avatar = new Label("JD");
        avatar.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: 900; -fx-min-width: 60px; -fx-min-height: 60px; -fx-background-radius: 30px; -fx-alignment: center;");

        Label name = new Label("Officer / Active Citizen");
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label role = new Label("Verified District Reporter • Central Delhi");
        role.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-font-weight: 700;");

        VBox nameBox = new VBox(2, name, role);
        HBox topBox = new HBox(16, avatar, nameBox);
        topBox.setAlignment(Pos.CENTER_LEFT);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(12);

        addGridRow(infoGrid, 0, "Email:", "officer.jandrishti@gov.in");
        addGridRow(infoGrid, 1, "Phone:", "+91 98765 43210");
        addGridRow(infoGrid, 2, "Assigned State:", "Delhi / National Capital Territory");
        addGridRow(infoGrid, 3, "Primary District:", "Central Delhi (Karol Bagh, New Delhi)");
        addGridRow(infoGrid, 4, "Account Status:", "Active & Verified (Level 3 Officer)");

        long count = repository.findAll().size();
        Label statLabel = new Label("📊 System Impact: " + count + " Total Issues Tracked Across India");
        statLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #047857;");

        card.getChildren().addAll(topBox, new Separator(), infoGrid, new Separator(), statLabel);
        container.getChildren().addAll(header, card);

        return container;
    }

    private void addGridRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #475569; -fx-font-size: 12px;");

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 12px;");

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }
}
