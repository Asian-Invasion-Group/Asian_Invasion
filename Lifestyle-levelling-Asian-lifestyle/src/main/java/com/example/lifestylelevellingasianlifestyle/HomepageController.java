package com.example.lifestylelevellingasianlifestyle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls the homepage UI
 * Provides navigation to Grocery List and Existing Groceries
 */
public class HomepageController {

    @FXML
    private Button groceryListButton;

    @FXML
    private Button existingGroceriesButton;

    /**
     * Navigates to the temporary Grocery List
     */
    @FXML
    private void onGroceryList() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("grocery-list.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) groceryListButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the list of existing groceries
     */
    @FXML
    private void onExistingGroceries() {

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("existing-grocery.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) existingGroceriesButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
