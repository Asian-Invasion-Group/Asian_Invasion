package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class ProfilesettingsController {

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
    @FXML
    public Button profileButton;
    @FXML
    public Button backButton;

    /**
     * Navigates to the Profile Tab
     */
    @FXML
    private void onProfile() {
        NavigationController.onProfile(profileButton);
    }

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
        NavigationController.onHome(backButton);
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


