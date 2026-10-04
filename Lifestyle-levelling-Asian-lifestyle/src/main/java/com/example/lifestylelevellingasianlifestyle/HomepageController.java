package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controls the homepage UI
 * Provides navigation to Grocery List and Existing Groceries
 */
public class HomepageController {

    @FXML
    public Button groceryButton;
    @FXML
    public Button homeButton;
    @FXML
    public Button utilityButton;
    @FXML
    public Button signoutButton;
    @FXML
    public Button weatherButton;

    /**
     * Navigates to the Grocery Tab
     */
    @FXML
    private void onGroceries() {
        NavigationController.onGroceries(groceryButton);
    }

    /**
     * Navigates to the Electricity Tracker.
     */
    @FXML
    private void onUtilities() {
        NavigationController.onUtilities(utilityButton);
    }

    /**
     * Navigates back to the Home page
     */
    @FXML
    private void onHome() {
        NavigationController.onHome(homeButton);
    }

    /**
     * Navigates to the Weather page
     */
    @FXML
    private void onWeather(){
        NavigationController.onWeather(weatherButton);
    }

    /**
     * Navigates back to login page and clears the session ID
     */
    @FXML
    private void onSignout() {
        Session.setUserId(-1);
        SignOutController.onSignout(signoutButton);
    }

}
