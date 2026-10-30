package in.jandrishti.data;

import in.jandrishti.model.User;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class UserSession {
    private static User currentUser = null;

    private UserSession() {}

    public static synchronized void login(User user) {
        currentUser = user;
    }

    public static synchronized void logout() {
        currentUser = null;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean hasRole(String role) {
        if (currentUser == null || currentUser.role() == null) return false;
        return currentUser.role().equalsIgnoreCase(role);
    }

    public static boolean confirmAndLogout(Runnable onLogoutConfirmed) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");

        ButtonType logoutBtn = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(logoutBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == logoutBtn) {
            logout();
            if (onLogoutConfirmed != null) {
                onLogoutConfirmed.run();
            }
            return true;
        }
        return false;
    }
}

