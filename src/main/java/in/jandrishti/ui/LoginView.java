package in.jandrishti.ui;

import in.jandrishti.data.DatabaseConnection;
import in.jandrishti.data.DatabaseInitializer;
import in.jandrishti.data.PasswordUtil;
import in.jandrishti.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Consumer;

public final class LoginView extends ScrollPane {
    private final Consumer<User> onLoginSuccess;

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField passwordTextField = new TextField();
    private final ToggleButton togglePasswordBtn = new ToggleButton("👁");

    private final Label errorLabel = new Label();

    public LoginView(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        // Ensure database schema & seed users (including admin/admin123) exist
        DatabaseInitializer.initializeDatabase();

        setFitToWidth(true);
        setFitToHeight(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("submit-scroll-pane");

        StackPane rootLayout = new StackPane(buildLoginCard());
        rootLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b, #0f172a); -fx-padding: 30px;");
        rootLayout.setAlignment(Pos.CENTER);

        setContent(rootLayout);
    }

    private Node buildLoginCard() {
        VBox card = new VBox(16);
        card.setMaxWidth(460);
        card.setPadding(new Insets(36, 32, 36, 32));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 24, 0, 0, 8);");

        // Header Branding
        Label logoIcon = new Label("👁");
        logoIcon.setStyle("-fx-font-size: 32px;");

        Label brandTitle = new Label("JanDrishti");
        brandTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");

        Label brandSubtitle = new Label("National Civic Issue Monitoring System");
        brandSubtitle.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748b;");

        VBox brandBox = new VBox(2, brandTitle, brandSubtitle);
        brandBox.setAlignment(Pos.CENTER);

        HBox headerRow = new HBox(10, logoIcon, brandBox);
        headerRow.setAlignment(Pos.CENTER);

        Label welcomeText = new Label("Sign in to your account");
        welcomeText.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #334155; -fx-padding: 4 0 4 0;");
        welcomeText.setAlignment(Pos.CENTER);

        // Username Field
        usernameField.setPromptText("Enter username (e.g. admin / citizen1 / officer1)");
        usernameField.getStyleClass().add("filter-field");
        usernameField.setStyle("-fx-pref-height: 38px; -fx-font-size: 13px;");
        usernameField.setOnAction(e -> handleLogin());

        // Password Fields & Show/Hide Toggle
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("filter-field");
        passwordField.setStyle("-fx-pref-height: 38px; -fx-font-size: 13px;");
        passwordField.setOnAction(e -> handleLogin());

        passwordTextField.setPromptText("Enter password");
        passwordTextField.getStyleClass().add("filter-field");
        passwordTextField.setStyle("-fx-pref-height: 38px; -fx-font-size: 13px;");
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        passwordTextField.setOnAction(e -> handleLogin());

        togglePasswordBtn.setStyle("-fx-pref-height: 38px; -fx-pref-width: 44px; -fx-cursor: hand; -fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold;");
        togglePasswordBtn.setOnAction(e -> {
            if (togglePasswordBtn.isSelected()) {
                passwordTextField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                passwordTextField.setVisible(true);
                passwordTextField.setManaged(true);
                togglePasswordBtn.setText("🙈");
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                togglePasswordBtn.setText("👁");
            }
        });

        StackPane passStack = new StackPane(passwordField, passwordTextField);
        HBox passwordRow = new HBox(6, passStack, togglePasswordBtn);
        HBox.setHgrow(passStack, Priority.ALWAYS);

        // Error Label
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 700;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Action Buttons: Login & Clear
        Button btnLogin = new Button("🔑  Sign In");
        btnLogin.getStyleClass().add("btn-report-new");
        btnLogin.setStyle("-fx-pref-height: 40px; -fx-font-size: 13px; -fx-font-weight: 800;");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnLogin, Priority.ALWAYS);
        btnLogin.setOnAction(e -> handleLogin());

        Button btnClear = new Button("🧹  Clear");
        btnClear.getStyleClass().add("secondary-button");
        btnClear.setStyle("-fx-pref-height: 40px; -fx-font-size: 13px;");
        btnClear.setOnAction(e -> handleClear());

        HBox buttonRow = new HBox(10, btnLogin, btnClear);
        buttonRow.setAlignment(Pos.CENTER);

        // Quick Demo Shortcuts
        Button btnDemoAdmin = new Button("👑 Admin (admin / admin123)");
        btnDemoAdmin.getStyleClass().add("secondary-button");
        btnDemoAdmin.setOnAction(e -> fillDemoCredentials("admin", "admin123"));

        Button btnDemoCitizen = new Button("👤 Citizen (citizen1)");
        btnDemoCitizen.getStyleClass().add("secondary-button");
        btnDemoCitizen.setOnAction(e -> fillDemoCredentials("citizen1", "password123"));

        Button btnDemoOfficer = new Button("👮 Officer (officer1)");
        btnDemoOfficer.getStyleClass().add("secondary-button");
        btnDemoOfficer.setOnAction(e -> fillDemoCredentials("officer1", "password123"));

        HBox demoRow = new HBox(6, btnDemoAdmin, btnDemoCitizen, btnDemoOfficer);
        demoRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(btnDemoAdmin, Priority.ALWAYS);
        HBox.setHgrow(btnDemoCitizen, Priority.ALWAYS);
        HBox.setHgrow(btnDemoOfficer, Priority.ALWAYS);

        Label demoNote = new Label("MySQL Database Authentication • SHA-256 Secured");
        demoNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-alignment: center;");

        card.getChildren().addAll(
                headerRow,
                welcomeText,
                createFormField("Username or Email *", usernameField),
                createFormField("Password *", passwordRow),
                errorLabel,
                buttonRow,
                new Separator(),
                demoRow,
                demoNote
        );

        return card;
    }

    private Node createFormField(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 700;");
        VBox box = new VBox(4, label, field);
        return box;
    }

    private void fillDemoCredentials(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
        passwordTextField.setText(password);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        passwordTextField.clear();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private enum AuthStatus { SUCCESS, USER_NOT_FOUND, INVALID_PASSWORD }
    private record AuthResult(AuthStatus status, User user) {}

    private void handleLogin() {
        String inputUser = usernameField.getText().trim();
        String currentPassword = togglePasswordBtn.isSelected() ? passwordTextField.getText() : passwordField.getText();

        if (inputUser.isEmpty() || currentPassword.isEmpty()) {
            showError("Invalid username/email or password.");
            return;
        }

        AuthResult result = authenticateInMySql(inputUser, currentPassword);

        if (result.status() == AuthStatus.USER_NOT_FOUND) {
            showError("User account not found.");
            return;
        } else if (result.status() == AuthStatus.INVALID_PASSWORD) {
            showError("Invalid username/email or password.");
            return;
        }

        if (result.user() != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            if (onLoginSuccess != null) {
                onLoginSuccess.accept(result.user());
            }
        }
    }

    private AuthResult authenticateInMySql(String usernameOrEmail, String password) {
        String sql = "SELECT id, username, email, password_hash, full_name, phone_number, role FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(password, storedHash)) {
                        User u = new User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("full_name"),
                                rs.getString("phone_number"),
                                rs.getString("role")
                        );
                        return new AuthResult(AuthStatus.SUCCESS, u);
                    } else {
                        return new AuthResult(AuthStatus.INVALID_PASSWORD, null);
                    }
                } else {
                    return new AuthResult(AuthStatus.USER_NOT_FOUND, null);
                }
            }
        } catch (Exception e) {
            System.err.println("[JanDrishti Auth] MySQL query exception: " + e.getMessage());
        }
        return new AuthResult(AuthStatus.USER_NOT_FOUND, null);
    }

    private void showError(String msg) {
        errorLabel.setText("❌ " + msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
