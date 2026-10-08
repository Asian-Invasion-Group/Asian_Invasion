package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;

/**
 * Controls the Login and Signup UI
 * Handles user authentication, email validation and navigation between pages
 */
public class AuthenticatorController {

    @FXML private VBox loginPane;
    @FXML private VBox signUpPane;

    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;

    @FXML private TextField signUpUsernameField;
    @FXML private PasswordField signUpPasswordField;
    @FXML private PasswordField signUpConfirmField;

    private final AuthenticationService authenticationService = new AuthenticationService();

    /**
     * Checks whether the email that User provided is a valid email address
     * Compares with expected email formate
     * @param email the given email by the user
     * @return true or false based on whether the email is valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Displays an error message to the user.
     *
     * @param title the title of the popup
     * @param message the message displayed to the user
     */
    private void errorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays signup page
     */
    @FXML
    private void showSignUp() {
        loginPane.setVisible(false);
        signUpPane.setVisible(true);
    }

    /**
     * Displays login page
     */
    @FXML
    private void showLogin() {
        signUpPane.setVisible(false);
        loginPane.setVisible(true);
    }

    /**
     * Displays home page
     */
    @FXML
    private void showHomepage(){
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("home-dashboard.fxml")
            );

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Authenticate user with provided email and password
     * If successful, userID is stored in current session
     * Homepage is displayed
     */
    @FXML
    private void onLogin() {

        String user = loginUsernameField.getText();
        String pass = loginPasswordField.getText();

        if (!isValidEmail(user)){
            errorPopup("Invalid Email", "Please enter a valid email address.");
            return;
        }

        int userId = authenticationService.login(user, pass);


        if (userId != -1){
            Session.setUserId(userId);
            showHomepage();
        }else{
            //FAILED LOGIN
            errorPopup("Login Failed", "Login Failed.");
        }

    }

    /**
     * Validates signing up details
     * Creates a new user account
     * If successful, transfer to login
     */
    @FXML
    private void onSignUp() {

        String user = signUpUsernameField.getText();
        String pass = signUpPasswordField.getText();
        String confirm = signUpConfirmField.getText();

        if (user.isEmpty() || pass.isEmpty()){
            errorPopup("Fill In All Fields", "Please fill in all fields.");
            return;
        }

        if (!isValidEmail(user)){
            errorPopup("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!pass.equals(confirm)){
            errorPopup("Passwords Do Not Match", "Please ensure that both passwords matches.");
            return;
        }

        //Create Account
        if (authenticationService.signUp(user,pass)){
            showLogin();
        }else{
            //FAILED SIGNUP
            errorPopup("Invalid Signup", "This email is in use. Please enter another email.");
        }
    }

}