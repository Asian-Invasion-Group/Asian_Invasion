package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls navigation between pages in the application.
 *
 * Handles navigation to the Home, Grocery, Utilities and
 * Login / Sign Up pages.
 */
public class NavigationController {

    /**
     * Navigates to the Home page.
     *
     * @param button the button pressed by the user
     */
    public static void onHome(Button button) {
        navigate(button, "home-dashboard.fxml", "Lifestyle Levelling");
    }

    /**
     * Navigates to the Grocery page.
     *
     * @param button the button pressed by the user
     */
    public static void onGroceries(Button button) {
        navigate(button, "grocery.fxml", "Grocery");
    }

    /**
     * Navigates to the Utilities page.
     *
     * @param button the button pressed by the user
     */
    public static void onUtilities(Button button) {
        navigate(
                button,
                "water-electricityusage.fxml",
                "Electricity Tracker"
        );
    }

    /**
     * Navigates to the Weather page.
     *
     * @param button pressed by user
     */
    public static void onWeather(Button button) {
        navigate(button, "Weather page.fxml", "Weather");
    }


    /**
     * Navigates to the Profile page.
     *
     * @param button pressde by user
     */
    public static void onProfile(Button button) {
        navigate(button, "profile settings.fxml", "Profile Settings");
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
    ) {
        try {
            Parent root = FXMLLoader.load(
                    NavigationController.class.getResource(fxml)
            );

            Stage stage = (Stage) button.getScene().getWindow();
            stage.setScene(new Scene(root, 850, 540));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

