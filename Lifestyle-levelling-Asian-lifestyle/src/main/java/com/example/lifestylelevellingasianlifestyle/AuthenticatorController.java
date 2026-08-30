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

    //auth
    private final AuthenticationService authenticationService = new AuthenticationService();
    //check email regex
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
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

        //check email valid
        if (!isValidEmail(user)){
            //UI notification invalid email
            return;
        }

        if (authenticationService.login(user, pass)){
            //success
            //go to homepage
        }else{
            //fail
            //UI notification login failed
        }

    }

    @FXML
    private void onSignUp() {

        String user = signUpUsernameField.getText();
        String pass = signUpPasswordField.getText();
        String confirm = signUpConfirmField.getText();

        //check email valid
        if (!isValidEmail(user)){
            //UI notification not a valid email
            return;
        }

        //check empty field
        if (user.isEmpty() || pass.isEmpty()){
            //UI notification fill in all fields
            return;
        }

        //check passwords match
        if (!pass.equals(confirm)){
            //UI notification passwords do not match
            return;
        }

        //create account
        if (authenticationService.signUp(user,pass)){
            //success
            showLogin();
        }else{
            //fail due to email in use
            //UI notification email in use
        }
    }

}