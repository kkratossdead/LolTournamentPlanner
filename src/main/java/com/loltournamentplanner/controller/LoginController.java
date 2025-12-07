package com.loltournamentplanner.controller;

import com.loltournamentplanner.App;
import com.loltournamentplanner.model.User;
import com.loltournamentplanner.service.AuthService;
import com.loltournamentplanner.service.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    protected void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username and password cannot be empty.");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                UserSession.getInstance().setCurrentUser(user);
                App.setRoot("view/dashboard-view");
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username and password cannot be empty.");
            return;
        }

        try {
            authService.register(username, password);
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully! Please login.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Could not register user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
