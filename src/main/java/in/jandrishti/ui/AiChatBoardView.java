package in.jandrishti.ui;

import in.jandrishti.data.AiChatService;
import in.jandrishti.model.ChatMessage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public final class AiChatBoardView extends BorderPane {
    private final AiChatService aiService;
    private final Consumer<Long> onInspectIssueCallback;

    private final VBox chatFeed = new VBox(14);
    private final ScrollPane scrollPane = new ScrollPane(chatFeed);
    private final TextField inputField = new TextField();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AiChatBoardView(AiChatService aiService, Consumer<Long> onInspectIssueCallback) {
        this.aiService = aiService;
        this.onInspectIssueCallback = onInspectIssueCallback;

        getStyleClass().add("ai-chat-board");
        setTop(buildHeader());
        setCenter(buildChatFeed());
        setBottom(buildInputArea());

        // Welcome message
        addMessage(ChatMessage.ai(
                "👋 **Welcome to JanDrishti Copilot AI!**\n\n" +
                "I monitor all civic reports, field updates, and regional bottlenecks across India in real-time.\n" +
                "Click any prompt chip below or type your question!"
        ));
    }

    private Node buildHeader() {
        Label avatar = new Label("AI");
        avatar.getStyleClass().add("ai-avatar");

        Label name = new Label("JanDrishti Copilot AI");
        name.getStyleClass().add("ai-title");

        Label status = new Label("• ONLINE & MONITORED");
        status.getStyleClass().add("ai-status-online");

        VBox identity = new VBox(2, name, status);
        HBox brand = new HBox(12, avatar, identity);
        brand.setAlignment(Pos.CENTER_LEFT);

        Label sub = new Label("Real-time Infrastructure Intelligence");
        sub.getStyleClass().add("section-detail");

        HBox top = new HBox(brand, sub);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brand, Priority.ALWAYS);
        top.getStyleClass().add("ai-header");

        // Quick Suggestion Chips
        HBox chips = new HBox(8,
                chip("📊 Executive Summary", "Show executive summary"),
                chip("🔥 Top Hotspots", "Show top hotspots"),
                chip("🚨 Critical Emergency List", "Show critical emergency list"),
                chip("🛣️ Road & Potholes", "Show pothole report"),
                chip("⏱️ SLA Risk Report", "Check SLA delays")
        );
        chips.getStyleClass().add("chip-row");
        chips.setPadding(new Insets(8, 24, 12, 24));

        VBox headerContainer = new VBox(0, top, chips);
        return headerContainer;
    }

    private Button chip(String labelText, String query) {
        Button chipBtn = new Button(labelText);
        chipBtn.getStyleClass().add("prompt-chip");
        chipBtn.setOnAction(e -> handleUserQuery(query));
        return chipBtn;
    }

    private Node buildChatFeed() {
        chatFeed.setPadding(new Insets(20, 24, 20, 24));
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("chat-scroll-pane");
        scrollPane.vvalueProperty().bind(chatFeed.heightProperty());
        return scrollPane;
    }

    private Node buildInputArea() {
        inputField.setPromptText("Ask AI Copilot anything... (e.g., 'Summary', 'Show critical potholes in Delhi')");
        inputField.getStyleClass().add("chat-input");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> handleUserQuery(inputField.getText()));

        Button sendBtn = new Button("Send ➔");
        sendBtn.getStyleClass().add("primary-button");
        sendBtn.setOnAction(e -> handleUserQuery(inputField.getText()));

        HBox inputRow = new HBox(10, inputField, sendBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        inputRow.setPadding(new Insets(14, 24, 18, 24));
        inputRow.getStyleClass().add("chat-input-bar");

        return inputRow;
    }

    private void handleUserQuery(String text) {
        if (text == null || text.isBlank()) return;

        String query = text.trim();
        inputField.clear();

        // Add user message to feed
        addMessage(ChatMessage.user(query));

        // Generate response from AiChatService
        Platform.runLater(() -> {
            ChatMessage aiResp = aiService.processQuery(query);
            addMessage(aiResp);
        });
    }

    private void addMessage(ChatMessage msg) {
        VBox card = new VBox(6);
        card.setMaxWidth(680);

        Label senderLabel = new Label(msg.sender() + " • " + msg.timestamp().format(TIME_FORMAT));
        senderLabel.getStyleClass().add("chat-sender");

        Label contentLabel = new Label(msg.text());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("chat-text");

        card.getChildren().addAll(senderLabel, contentLabel);

        if (msg.relatedIssueId() != null && msg.actionLabel() != null) {
            Button actionBtn = new Button("🔍 " + msg.actionLabel());
            actionBtn.getStyleClass().add("chat-action-btn");
            actionBtn.setOnAction(e -> {
                if (onInspectIssueCallback != null) {
                    onInspectIssueCallback.accept(msg.relatedIssueId());
                }
            });
            card.getChildren().add(actionBtn);
        }

        HBox row = new HBox(card);
        row.setPadding(new Insets(4, 0, 4, 0));

        if (msg.isAi()) {
            card.getStyleClass().addAll("chat-bubble", "chat-bubble-ai");
            row.setAlignment(Pos.CENTER_LEFT);
        } else {
            card.getStyleClass().addAll("chat-bubble", "chat-bubble-user");
            row.setAlignment(Pos.CENTER_RIGHT);
        }

        chatFeed.getChildren().add(row);
    }
}
