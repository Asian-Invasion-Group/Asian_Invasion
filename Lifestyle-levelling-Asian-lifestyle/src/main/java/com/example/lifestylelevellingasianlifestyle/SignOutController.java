package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SignOutController {

    /**
     * Signs out the current user and navigates to the
     * Login / Sign Up page.
     *
     * @param button the button pressed by the user
     */
    public static void onSignout(Button button) {
        Session.setUserId(-1);
        SignOutController.navigate(
                button,
                "authenticator-view.fxml",
                "Login / Sign Up"
        );
    }


    /**
     * Navigates to another page in the application.
     *
     * @param button the button pressed by the user
     * @param fxml the FXML file of the page to navigate to
     * @param title the title of the page
     */
    private static void navigate(
            Button button,
            String fxml,
            String title
    ){
        try {
            Parent root = FXMLLoader.load(
                    SignOutController.class.getResource(fxml)
            );
            
            Stage stage = (Stage) button.getScene().getWindow();
            stage.setScene(new Scene(root, 450,375));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
