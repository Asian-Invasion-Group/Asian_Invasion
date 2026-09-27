package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls Grocery List UI
 * Manages temporary grocery list
 * Allows user to add or remove items or move to existing database
 */
public class GroceryListController {

    @FXML
    private TableView<GroceryItem> groceryTable;

    @FXML
    private TableColumn<GroceryItem, String> nameColumn;

    @FXML
    private TableColumn<GroceryItem, Integer> quantityColumn;

    private final ObservableList<GroceryItem> groceryItems =
            FXCollections.observableArrayList();

    private final ItemService itemService = new ItemService();

    /**
     * Initialize the grocery table and columns
     * Allows for single-item selection
     */
    @FXML
    public void initialize() {

        // Connect the table columns to GroceryItem
        nameColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getItemName()
                )
        );

        quantityColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(
                        cellData.getValue().getQuantity()
                ).asObject()
        );

        // Give the table its data
        groceryTable.setItems(groceryItems);

        // Only allow one item to be selected at a time
        groceryTable.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * Popup allowing user to enter name and quantity and add to the grocery list
     */
    @FXML
    private void onAddItem() {
        TextField nameField = new TextField();
        nameField.setPromptText("Item name");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        Label errorLabel = new Label();

        Button addButton = new Button("Add");
        Button backButton = new Button("Back");

        VBox layout = new VBox(
                10,
                new Label("Item Name"),
                nameField,
                new Label("Quantity"),
                quantityField,
                errorLabel,
                addButton,
                backButton
        );

        layout.setPadding(new Insets(15));

        // Create popup window
        Stage popup = new Stage();

        popup.setTitle("Add Grocery Item");

        // Put into scene
        Scene scene = new Scene(layout, 300, 300);

        popup.setScene(scene);

        // Add item
        addButton.setOnAction(event -> {

            String name = nameField.getText().trim();
            String quantityText = quantityField.getText().trim();

            // Check empty fields
            if (name.isEmpty() || quantityText.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }

            int quantity;

            // Check quantity is an integer
            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Quantity must be a number.");
                return;
            }

            // Check quantity is positive
            if (quantity <= 0) {
                errorLabel.setText("Quantity must be greater than 0.");
                return;
            }

            // Add item
            groceryItems.add(
                    new GroceryItem(name, quantity)
            );

            // Clear fields
            nameField.clear();
            quantityField.clear();
            errorLabel.setText("");

            // Put cursor back in item name
            nameField.requestFocus();
        });

        // Back closes popup
        backButton.setOnAction(event -> {
            popup.close();
        });

        // Show popup
        popup.show();
    }

    /**
     * Removes the currently selected item
     */
    @FXML
    private void onRemoveItem() {

        GroceryItem selectedItem =
                groceryTable.getSelectionModel().getSelectedItem();

        // Nothing selected
        if (selectedItem == null) {
            return;
        }

        // Remove selected item
        groceryItems.remove(selectedItem);
    }

    /**
     * Moves the currently selected item to the database (signifies item bought)
     */
    @FXML
    private void onMoveToExisting() {
        GroceryItem selectedItem =
                groceryTable.getSelectionModel().getSelectedItem();

        // Nothing selected
        if (selectedItem == null) {
            return;
        }

        String itemName = selectedItem.getItemName();
        int quantity = selectedItem.getQuantity();

        // Create popup
        Stage popup = new Stage();
        popup.setTitle("Move to Existing");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label titleLabel = new Label(
                itemName + " - Enter expiry date(s)"
        );

        Label errorLabel = new Label();

        List<DatePicker> datePickers = new ArrayList<>();

        layout.getChildren().add(titleLabel);

        // Create one DatePicker for each individual item
        for (int i = 0; i < quantity; i++) {

            Label label = new Label(
                    itemName + " #" + (i + 1) + " expiry date"
            );

            DatePicker datePicker = new DatePicker();

            datePickers.add(datePicker);

            layout.getChildren().add(label);
            layout.getChildren().add(datePicker);
        }

        Button moveButton = new Button("Move");
        Button backButton = new Button("Back");

        layout.getChildren().add(errorLabel);
        layout.getChildren().add(moveButton);
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 350, 100 + (quantity * 70));

        popup.setScene(scene);

        // Move items to database
        moveButton.setOnAction(event -> {

            // Make sure every expiry date has been entered
            for (DatePicker datePicker : datePickers) {

                if (datePicker.getValue() == null) {
                    errorLabel.setText(
                            "Please enter an expiry date for every item."
                    );
                    return;
                }
            }

            // Add each individual item to SQLite
            for (DatePicker datePicker : datePickers) {

                LocalDate expiryDate = datePicker.getValue();

                String expiryDateString = expiryDate.toString();
                String dateAddedString = LocalDate.now().toString();

                boolean success = itemService.addItem(
                        itemName,
                        expiryDateString,
                        dateAddedString
                );

                // Stop if database insertion failed
                if (!success) {

                    errorLabel.setText(
                            "Failed to add item to database."
                    );

                    return;
                }
            }

            // Database insertion successful
            groceryItems.remove(selectedItem);

            // Close popup
            popup.close();
        });

        // Back closes popup
        backButton.setOnAction(event -> {
            popup.close();
        });

        popup.show();
    }

    /**
     * Returns the user to homepage
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
