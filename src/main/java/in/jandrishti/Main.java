package in.jandrishti;

import in.jandrishti.data.MySqlIssueRepository;
import in.jandrishti.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {
    private MySqlIssueRepository repository;

    @Override
    public void start(Stage stage) {
        this.repository = new MySqlIssueRepository();

        stage.setTitle("JanDrishti | National Civic Issue Monitoring System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        showLoginScene(stage);
        stage.setMaximized(true);
        stage.show();
    }

    private void showLoginScene(Stage stage) {
        in.jandrishti.data.UserSession.logout();
        in.jandrishti.ui.LoginView loginView = new in.jandrishti.ui.LoginView(user -> showDashboardScene(stage, user));
        Scene scene = new Scene(loginView, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    private void showDashboardScene(Stage stage, in.jandrishti.model.User user) {
        if (user == null) {
            showLoginScene(stage);
            return;
        }

        in.jandrishti.data.UserSession.login(user);

        javafx.scene.Parent root;
        String role = user.role() != null ? user.role().toUpperCase() : "CITIZEN";

        if ("ADMIN".contains(role)) {
            root = new in.jandrishti.ui.AdminView(repository, user, () -> showLoginScene(stage));
        } else if ("OFFICER".contains(role)) {
            root = new in.jandrishti.ui.OfficerView(repository, user, () -> showLoginScene(stage));
        } else {
            root = new in.jandrishti.ui.CitizenView(repository, user, () -> showLoginScene(stage));
        }

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
