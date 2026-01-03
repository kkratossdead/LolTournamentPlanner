package com.loltournamentplanner.controller;

import com.loltournamentplanner.App;
import com.loltournamentplanner.model.User;
import com.loltournamentplanner.service.AuthService;
import com.loltournamentplanner.service.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class LoginController {

    // Login form fields
    @FXML private VBox loginForm;
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginUsernameError;
    @FXML private Label loginPasswordError;
    @FXML private Label loginGeneralError;

    // Register form fields
    @FXML private VBox registerForm;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private Label registerUsernameError;
    @FXML private Label registerEmailError;
    @FXML private Label registerPasswordError;
    @FXML private Label registerConfirmPasswordError;
    @FXML private Label registerGeneralError;

    // Password strength
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;

    // Mode label
    @FXML private Label formModeLabel;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        // Add listener for password strength indicator
        registerPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });

        // Clear errors on input
        loginUsernameField.textProperty().addListener((obs, o, n) -> hideError(loginUsernameError, loginUsernameField));
        loginPasswordField.textProperty().addListener((obs, o, n) -> hideError(loginPasswordError, loginPasswordField));
        registerUsernameField.textProperty().addListener((obs, o, n) -> hideError(registerUsernameError, registerUsernameField));
        registerEmailField.textProperty().addListener((obs, o, n) -> hideError(registerEmailError, registerEmailField));
        registerPasswordField.textProperty().addListener((obs, o, n) -> hideError(registerPasswordError, registerPasswordField));
        registerConfirmPasswordField.textProperty().addListener((obs, o, n) -> hideError(registerConfirmPasswordError, registerConfirmPasswordField));
    }

    private void updatePasswordStrength(String password) {
        int strength = authService.getPasswordStrength(password);
        String label = authService.getPasswordStrengthLabel(strength);
        
        double progress = strength / 4.0;
        passwordStrengthBar.setProgress(progress);
        passwordStrengthLabel.setText(label);

        // Update style class based on strength
        passwordStrengthBar.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-strong", "strength-very-strong");
        passwordStrengthLabel.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-strong", "strength-very-strong");
        
        String styleClass = switch (strength) {
            case 0, 1 -> "strength-weak";
            case 2 -> "strength-fair";
            case 3 -> "strength-strong";
            case 4 -> "strength-very-strong";
            default -> "";
        };
        
        if (!styleClass.isEmpty()) {
            passwordStrengthBar.getStyleClass().add(styleClass);
            passwordStrengthLabel.getStyleClass().add(styleClass);
        }
    }

    @FXML
    protected void onLogin() {
        clearLoginErrors();
        
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        boolean hasError = false;

        if (username.isEmpty()) {
            showError(loginUsernameError, loginUsernameField, "Username is required");
            hasError = true;
        }

        if (password.isEmpty()) {
            showError(loginPasswordError, loginPasswordField, "Password is required");
            hasError = true;
        }

        if (hasError) return;

        try {
            User user = authService.login(username, password);
            if (user != null) {
                UserSession.getInstance().setCurrentUser(user);
                App.setRoot("view/dashboard-view");
            } else {
                showError(loginGeneralError, null, "Invalid username or password");
            }
        } catch (IOException e) {
            showError(loginGeneralError, null, "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onRegister() {
        clearRegisterErrors();

        String username = registerUsernameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();

        boolean hasError = false;

        // Validate username
        List<String> usernameErrors = authService.validateUsername(username);
        if (!usernameErrors.isEmpty()) {
            showError(registerUsernameError, registerUsernameField, usernameErrors.get(0));
            hasError = true;
        }

        // Validate email
        List<String> emailErrors = authService.validateEmail(email);
        if (!emailErrors.isEmpty()) {
            showError(registerEmailError, registerEmailField, emailErrors.get(0));
            hasError = true;
        }

        // Validate password
        List<String> passwordErrors = authService.validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            showError(registerPasswordError, registerPasswordField, passwordErrors.get(0));
            hasError = true;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            showError(registerConfirmPasswordError, registerConfirmPasswordField, "Please confirm your password");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            showError(registerConfirmPasswordError, registerConfirmPasswordField, "Passwords do not match");
            hasError = true;
        }

        if (hasError) return;

        try {
            // Check if username is taken
            if (authService.isUsernameTaken(username)) {
                showError(registerUsernameError, registerUsernameField, "Username is already taken");
                return;
            }

            // Check if email is taken
            if (authService.isEmailTaken(email)) {
                showError(registerEmailError, registerEmailField, "Email is already registered");
                return;
            }

            authService.register(username, email, password);
            
            // Switch to login form with success message
            onSwitchToLogin();
            showSuccess(loginGeneralError, "Account created successfully! Please sign in.");
            
        } catch (IOException e) {
            showError(registerGeneralError, null, e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSwitchToRegister() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        formModeLabel.setText("Create Account");
        clearLoginErrors();
        clearRegisterErrors();
    }

    @FXML
    protected void onSwitchToLogin() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        formModeLabel.setText("Sign In");
        clearLoginErrors();
        clearRegisterErrors();
    }

    private void showError(Label errorLabel, TextField field, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (field != null) {
            field.getStyleClass().add("field-error");
        }
    }

    private void showSuccess(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
        label.getStyleClass().remove("error-label");
        label.getStyleClass().add("success-label");
    }

    private void hideError(Label errorLabel, TextField field) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        if (field != null) {
            field.getStyleClass().remove("field-error");
        }
    }

    private void clearLoginErrors() {
        hideError(loginUsernameError, loginUsernameField);
        hideError(loginPasswordError, loginPasswordField);
        loginGeneralError.setVisible(false);
        loginGeneralError.setManaged(false);
        loginGeneralError.getStyleClass().remove("success-label");
        loginGeneralError.getStyleClass().add("error-label");
    }

    private void clearRegisterErrors() {
        hideError(registerUsernameError, registerUsernameField);
        hideError(registerEmailError, registerEmailField);
        hideError(registerPasswordError, registerPasswordField);
        hideError(registerConfirmPasswordError, registerConfirmPasswordField);
        registerGeneralError.setVisible(false);
        registerGeneralError.setManaged(false);
    }
}
