package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AuthenticatorController {

    @FXML private VBox loginPane;
    @FXML private VBox signUpPane;

    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;

    @FXML private TextField signUpUsernameField;
    @FXML private PasswordField signUpPasswordField;
    @FXML private PasswordField signUpConfirmField;

    @FXML
    private void showSignUp() {
        loginPane.setVisible(false);
        signUpPane.setVisible(true);
    }

    @FXML
    private void showLogin() {
        signUpPane.setVisible(false);
        loginPane.setVisible(true);
    }

    @FXML
    private void onLogin() {
        String user = loginUsernameField.getText();
        String pass = loginPasswordField.getText();

    }

    @FXML
    private void onSignUp() {
        String user = signUpUsernameField.getText();
        String pass = signUpPasswordField.getText();
        String confirm = signUpConfirmField.getText();

        showLogin();
    }

}