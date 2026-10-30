package in.jandrishti.ui;

import in.jandrishti.data.AiChatService;
import in.jandrishti.data.IssueRepository;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Consumer;

public final class IssueSubmitView extends ScrollPane {
    private final IssueRepository repository;
    private final AiChatService aiService;
    private final Consumer<Issue> onSubmittedCallback;
    private final Runnable onCancelCallback;

    private final TextField titleField = new TextField();
    private final ComboBox<String> categoryCombo = new ComboBox<>();
    private final ComboBox<String> priorityCombo = new ComboBox<>();
    private final ComboBox<String> stateCombo = new ComboBox<>();
    private final TextField districtField = new TextField();
    private final TextField cityField = new TextField();
    private final TextField areaField = new TextField();
    private final TextField roadField = new TextField();
    private final TextField reporterNameField = new TextField();
    private final TextField contactField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final TextField departmentField = new TextField();

    // Image Upload Controls
    private File attachedImageFile = null;
    private final Label photoLabel = new Label("No photo attached (Optional)");
    private final Button btnAttachPhoto = new Button("📁  Attach Photo / Proof");
    private final Button btnRemovePhoto = new Button("✖ Remove");

    private final Label triageBadge = new Label();
    private final Label triageExplanation = new Label();
    private final VBox triageBanner = new VBox(6);
    private final Label notificationBanner = new Label();

    public IssueSubmitView(IssueRepository repository, AiChatService aiService, Consumer<Issue> onSubmittedCallback, Runnable onCancelCallback) {
        this.repository = repository;
        this.aiService = aiService;
        this.onSubmittedCallback = onSubmittedCallback;
        this.onCancelCallback = onCancelCallback;

        setFitToWidth(true);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("submit-scroll-pane");
        setContent(buildFormContainer());
    }

    private Node buildFormContainer() {
        VBox container = new VBox(16);
        container.setStyle("-fx-padding: 16px 24px 180px 24px; -fx-background-color: #f1f5f9;");
        container.getStyleClass().add("form-page-container");
        container.setMinHeight(Region.USE_PREF_SIZE);
        container.setPrefHeight(Region.USE_COMPUTED_SIZE);
        container.setMaxHeight(Double.MAX_VALUE);

        // Top Navigation / Header
        Button btnBack = new Button("← Back to Dashboard");
        btnBack.getStyleClass().add("btn-ask-ai");
        btnBack.setOnAction(e -> {
            if (onCancelCallback != null) onCancelCallback.run();
        });

        Button btnTopSubmit = new Button("🚀 Submit Report");
        btnTopSubmit.getStyleClass().add("btn-report-new");
        btnTopSubmit.setStyle("-fx-font-size: 12px; -fx-padding: 7 16; -fx-font-weight: bold;");
        btnTopSubmit.setOnAction(e -> submitForm());

        HBox topButtons = new HBox(8, btnTopSubmit, btnBack);
        topButtons.setAlignment(Pos.CENTER_RIGHT);

        Label title = new Label("Log a New Infrastructure Issue");
        title.getStyleClass().add("hero-title");
        Label desc = new Label("Report civic defects with location details. Click 'AI Auto-Triage' to automatically evaluate priority and department.");
        desc.getStyleClass().add("hero-subtitle");

        VBox titleGroup = new VBox(3, title, desc);
        HBox headerRow = new HBox(titleGroup, topButtons);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleGroup, Priority.ALWAYS);

        // Success notification banner (hidden by default)
        notificationBanner.getStyleClass().add("success-banner");
        notificationBanner.setVisible(false);
        notificationBanner.setManaged(false);

        // Form Layout Card
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("white-panel");
        formCard.setStyle("-fx-padding: 18px 20px 30px 20px;");
        formCard.setMinHeight(Region.USE_PREF_SIZE);
        formCard.setPrefHeight(Region.USE_COMPUTED_SIZE);
        formCard.setMaxHeight(Double.MAX_VALUE);

        // Section 1: Overview
        Label sec1Title = new Label("1. Issue Overview & Description");
        sec1Title.getStyleClass().add("panel-header-title");

        titleField.setPromptText("E.g. Deep pothole near school gate / Streetlights not working");
        titleField.getStyleClass().add("filter-field");

        categoryCombo.setItems(FXCollections.observableArrayList(
                "Pothole", "Road Damage", "Drainage", "Streetlight", "Garbage", "Water Leakage", "Traffic Signal", "Other"
        ));
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.getStyleClass().add("filter-field");

        priorityCombo.setItems(FXCollections.observableArrayList("Low", "Medium", "High", "Critical"));
        priorityCombo.getSelectionModel().select("Medium");
        priorityCombo.getStyleClass().add("filter-field");

        departmentField.setPromptText("E.g. Public Works Dept (PWD)");
        departmentField.setText("Public Works Dept (PWD)");
        departmentField.getStyleClass().add("filter-field");

        HBox categoryPriorityRow = new HBox(12,
                createFormField("Category *", categoryCombo),
                createFormField("Priority Level *", priorityCombo),
                createFormField("Target Department", departmentField)
        );

        descriptionArea.setPromptText("Describe the defect, size, traffic impact, or specific landmark context...");
        descriptionArea.setPrefRowCount(2);
        descriptionArea.getStyleClass().add("filter-field");

        // AI Triage Button & Banner
        Button triageButton = new Button("✨ AI Auto-Triage & Assessment");
        triageButton.getStyleClass().add("btn-ask-ai");
        triageButton.setOnAction(e -> runAiTriage());

        triageBanner.getStyleClass().add("triage-banner");
        triageBanner.setVisible(false);
        triageBanner.setManaged(false);
        triageBadge.getStyleClass().add("triage-badge");
        triageExplanation.getStyleClass().add("triage-text");
        triageBanner.getChildren().addAll(triageBadge, triageExplanation);

        HBox triageActionRow = new HBox(12, triageButton, triageBanner);
        triageActionRow.setAlignment(Pos.CENTER_LEFT);

        VBox sec1 = new VBox(8, sec1Title, createFormField("Issue Title / Headline *", titleField), categoryPriorityRow, createFormField("Detailed Description *", descriptionArea), triageActionRow);

        // Section 2: Location
        Label sec2Title = new Label("2. Location Details");
        sec2Title.getStyleClass().add("panel-header-title");

        stateCombo.setItems(FXCollections.observableArrayList("Delhi", "Maharashtra", "Karnataka", "Telangana", "Tamil Nadu", "Uttar Pradesh", "West Bengal", "Gujarat"));
        stateCombo.getSelectionModel().selectFirst();
        stateCombo.getStyleClass().add("filter-field");

        districtField.setPromptText("District (e.g. Central Delhi)");
        districtField.setText("Central Delhi");
        districtField.getStyleClass().add("filter-field");

        cityField.setPromptText("City (e.g. New Delhi)");
        cityField.setText("New Delhi");
        cityField.getStyleClass().add("filter-field");

        areaField.setPromptText("Area / Locality (e.g. Karol Bagh)");
        areaField.getStyleClass().add("filter-field");

        roadField.setPromptText("Road / Landmark (e.g. Ajmal Khan Road)");
        roadField.getStyleClass().add("filter-field");

        HBox locRow1 = new HBox(12, createFormField("State *", stateCombo), createFormField("District *", districtField), createFormField("City *", cityField));
        HBox locRow2 = new HBox(12, createFormField("Area / Locality *", areaField), createFormField("Road / Street *", roadField));

        VBox sec2 = new VBox(8, sec2Title, locRow1, locRow2);

        // Section 3: Optional Image Upload & Reporter Info
        Label sec3Title = new Label("3. Reporter & Photo Proof (Optional)");
        sec3Title.getStyleClass().add("panel-header-title");

        reporterNameField.setPromptText("Full Name");
        reporterNameField.setText("Civic Citizen");
        reporterNameField.getStyleClass().add("filter-field");

        contactField.setPromptText("Phone Number");
        contactField.setText("+91 98765 00000");
        contactField.getStyleClass().add("filter-field");

        // Photo Upload Controls
        btnAttachPhoto.getStyleClass().add("secondary-button");
        btnAttachPhoto.setOnAction(e -> choosePhotoFile());

        btnRemovePhoto.getStyleClass().add("secondary-button");
        btnRemovePhoto.setVisible(false);
        btnRemovePhoto.setOnAction(e -> clearPhotoFile());

        photoLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 600;");

        HBox photoRow = new HBox(12, btnAttachPhoto, photoLabel, btnRemovePhoto);
        photoRow.setAlignment(Pos.CENTER_LEFT);

        HBox reporterRow = new HBox(12, createFormField("Reporter Name", reporterNameField), createFormField("Contact Phone", contactField));
        VBox sec3 = new VBox(8, sec3Title, reporterRow, createFormField("Attach Photo Proof (Optional)", photoRow));

        // Submit & Cancel Buttons
        Button submitButton = new Button("🚀 Submit Civic Issue Report");
        submitButton.getStyleClass().add("btn-report-new");
        submitButton.setStyle("-fx-font-size: 13px; -fx-padding: 10 22; -fx-font-weight: bold;");
        submitButton.setOnAction(e -> submitForm());

        Button cancelButton = new Button("Cancel / Back");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-padding: 10 22; -fx-font-weight: bold;");
        cancelButton.setOnAction(e -> {
            if (onCancelCallback != null) onCancelCallback.run();
        });

        HBox actionRow = new HBox(12, submitButton, cancelButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(6, 0, 8, 0));

        formCard.getChildren().addAll(sec1, new Separator(), sec2, new Separator(), sec3, new Separator(), actionRow);
        container.getChildren().addAll(headerRow, notificationBanner, formCard);

        return container;
    }

    private Node createFormField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 700;");
        VBox box = new VBox(4, label, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void choosePhotoFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Issue Photo Attachment");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) getScene().getWindow();
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            this.attachedImageFile = selected;
            photoLabel.setText("📷 Attached: " + selected.getName() + " (" + (selected.length() / 1024) + " KB)");
            photoLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: 700; -fx-font-size: 11px;");
            btnRemovePhoto.setVisible(true);
        }
    }

    private void clearPhotoFile() {
        this.attachedImageFile = null;
        photoLabel.setText("No photo attached (Optional)");
        photoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        btnRemovePhoto.setVisible(false);
    }

    private void runAiTriage() {
        String desc = descriptionArea.getText();
        String cat = categoryCombo.getValue();
        String road = roadField.getText();

        AiChatService.TriageResult result = aiService.autoTriage(desc, cat, road);

        priorityCombo.getSelectionModel().select(result.priority().label());
        categoryCombo.getSelectionModel().select(result.suggestedCategory());
        departmentField.setText(result.suggestedDepartment());

        triageBadge.setText("AI Triage: " + result.priority().label().toUpperCase() + " PRIORITY");
        triageBadge.getStyleClass().removeAll("badge-priority-critical", "badge-priority-high", "badge-priority-medium", "badge-priority-low");
        triageBadge.getStyleClass().add("badge-priority-" + result.priority().label().toLowerCase());

        triageExplanation.setText(result.explanation() + " Assigned to " + result.suggestedDepartment() + ".");
        triageBanner.setVisible(true);
        triageBanner.setManaged(true);
    }

    private void submitForm() {
        if (titleField.getText().isBlank() || descriptionArea.getText().isBlank() || areaField.getText().isBlank() || roadField.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in the required fields: Title, Description, Area, and Road/Street.", ButtonType.OK);
            alert.setHeaderText("Required Fields Missing");
            alert.showAndWait();
            return;
        }

        long newId = 2000L + (new Random().nextInt(8999) + 1000);
        IssuePriority priority = IssuePriority.fromString(priorityCombo.getValue());

        Issue newIssue = new Issue(
                newId,
                categoryCombo.getValue(),
                titleField.getText().trim(),
                descriptionArea.getText().trim(),
                priority,
                stateCombo.getValue(),
                districtField.getText().trim().isEmpty() ? "Central District" : districtField.getText().trim(),
                cityField.getText().trim().isEmpty() ? "Metro Area" : cityField.getText().trim(),
                areaField.getText().trim(),
                roadField.getText().trim(),
                IssueStatus.REPORTED,
                reporterNameField.getText().trim().isEmpty() ? "Citizen" : reporterNameField.getText().trim(),
                contactField.getText().trim(),
                departmentField.getText().trim().isEmpty() ? "Public Works Dept (PWD)" : departmentField.getText().trim(),
                false,
                LocalDateTime.now()
        );

        repository.save(newIssue);

        notificationBanner.setText("✅ Success! Issue #" + newId + " has been reported and saved into the MySQL database.");
        notificationBanner.setVisible(true);
        notificationBanner.setManaged(true);

        if (onSubmittedCallback != null) {
            onSubmittedCallback.accept(newIssue);
        }

        resetForm();
    }

    private void resetForm() {
        titleField.clear();
        descriptionArea.clear();
        areaField.clear();
        roadField.clear();
        clearPhotoFile();
        triageBanner.setVisible(false);
        triageBanner.setManaged(false);
    }
}
