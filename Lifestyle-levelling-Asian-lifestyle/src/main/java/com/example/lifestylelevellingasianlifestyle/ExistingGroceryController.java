package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controls the Existing Groceries UI
 * Displays current user's stored grocery items
 * Allows for interactions such as removal
 */
public class ExistingGroceryController {

    @FXML
    private TableView<ExistingGrocery> groceryTable;

    @FXML
    private TableColumn<ExistingGrocery, String> nameColumn;

    @FXML
    private TableColumn<ExistingGrocery, String> expiryColumn;

    @FXML
    private TableColumn<ExistingGrocery, String> dateAddedColumn;

    private final ObservableList<ExistingGrocery> groceryItems =
            FXCollections.observableArrayList();

    private final ItemService itemService = new ItemService();

    /**
     * Initialize the grocery table and loads grocery items from the database
     */
    @FXML
    public void initialize() {

        nameColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getItemName()
                )
        );

        expiryColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getExpiryDate()
                )
        );

        dateAddedColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getDateAdded()
                )
        );

        groceryTable.setItems(groceryItems);

        groceryItems.addAll(
                itemService.getItemsForCurrentUser()
        );
    }

    /**
     * Removes currently selected item from the database
     */
    @FXML
    private void onRemoveItem() {

        ExistingGrocery selectedItem =
                groceryTable.getSelectionModel().getSelectedItem();

        // Nothing selected
        if (selectedItem == null) {
            return;
        }

        // Delete from SQLite
        boolean success =
                itemService.deleteItem(selectedItem.getItemID());

        if (success) {

            // Remove from the TableView
            groceryItems.remove(selectedItem);

        } else {

            System.out.println("Failed to delete grocery.");
        }
    }

    /**
     * Returns to homepage
     */
    @FXML
    private void onBackToHomepage() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("home-dashboard.fxml")
            );

            Parent root = loader.load();

            Stage stage =
                    (Stage) groceryTable.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
