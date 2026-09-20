package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class AuthenticatorController {

    @FXML
    private VBox loginPane;

    @FXML
    private VBox signUpPane;

    @FXML
    private TextField loginUsernameField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextField signUpUsernameField;

    @FXML
    private PasswordField signUpPasswordField;

    @FXML
    private PasswordField signUpConfirmField;

    @FXML
    private Label loginStatusLabel;

    @FXML
    private Label signUpStatusLabel;

    // auth
    private final AuthenticationService authenticationService =
            new AuthenticationService();

    // check email regex
    private boolean isValidEmail(String email) {
        return email != null
                && email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
    }

    @FXML
    private void showSignUp() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);

        signUpPane.setVisible(true);
        signUpPane.setManaged(true);

        loginStatusLabel.setText("");
    }

    @FXML
    private void showLogin() {
        signUpPane.setVisible(false);
        signUpPane.setManaged(false);

        loginPane.setVisible(true);
        loginPane.setManaged(true);

        signUpStatusLabel.setText("");
    }

    @FXML
    private void onLogin() {

        String user = loginUsernameField
                .getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String pass = loginPasswordField.getText();

        loginStatusLabel.getStyleClass().removeAll("status-success");

        if (user.isEmpty() || pass.isEmpty()) {
            showLoginError("Enter both your email and password.");
            return;
        }

        if (!isValidEmail(user)) {
            showLoginError("Enter a valid email address.");
            return;
        }

        /*
         * 保留 main 分支登录逻辑
         */
        int userId = authenticationService.login(user, pass);

        if (userId != -1) {

            // 保存当前用户 ID
            Session.setUserId(userId);

            System.out.println("Login userId: " + Session.getUserId());

            try {

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("home-dashboard.fxml")
                );

                Parent root = loader.load();

                Stage stage = (Stage)
                        loginUsernameField
                                .getScene()
                                .getWindow();

                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();

                showLoginError(
                        "The dashboard could not be opened. Please try again."
                );
            }

        } else {

            showLoginError(
                    "Email or password is incorrect."
            );
        }
    }

    @FXML
    private void onSignUp() {

        String user = signUpUsernameField
                .getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        String pass = signUpPasswordField.getText();
        String confirm = signUpConfirmField.getText();

        signUpStatusLabel
                .getStyleClass()
                .removeAll("status-success");

        if (user.isEmpty()
                || pass.isEmpty()
                || confirm.isEmpty()) {

            showSignUpError("Complete all fields.");
            return;
        }

        if (!isValidEmail(user)) {
            showSignUpError(
                    "Enter a valid email address."
            );
            return;
        }

        if (pass.length() < 8) {
            showSignUpError(
                    "Use at least 8 characters for your password."
            );
            return;
        }

        if (!pass.equals(confirm)) {
            showSignUpError(
                    "Passwords do not match."
            );
            return;
        }

        switch (authenticationService.signUpWithResult(user, pass)) {

            case SUCCESS -> {

                loginUsernameField.setText(user);

                showLogin();

                loginStatusLabel.setText(
                        "Account created. Sign in to continue."
                );

                loginStatusLabel
                        .getStyleClass()
                        .add("status-success");
            }

            case DUPLICATE_EMAIL ->
                    showSignUpError(
                            "An account with that email already exists."
                    );

            case INVALID_INPUT ->
                    showSignUpError(
                            "Check the email and use a password with at least 8 characters."
                    );

            case STORAGE_ERROR ->
                    showSignUpError(
                            "The account could not be saved. Please try again."
                    );
        }
    }

    private void showLoginError(String message) {
        loginStatusLabel.setText(message);

        loginStatusLabel
                .getStyleClass()
                .removeAll("status-success");
    }

    private void showSignUpError(String message) {
        signUpStatusLabel.setText(message);

        signUpStatusLabel
                .getStyleClass()
                .removeAll("status-success");
    }
}