package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the homepage UI
 * Provides navigation to Grocery List and Existing Groceries
 */
public class HomepageController {

    @FXML
    public Button Grocery;

    @FXML
    public Button homeButton;

    @FXML
    private TableView<ExistingGrocery> groceryTable;

    @FXML
    private TableColumn<ExistingGrocery, String> existingNameColumn;

    @FXML
    private TableColumn<ExistingGrocery, String> expiryColumn;

    @FXML
    private TableColumn<ExistingGrocery, String> dateAddedColumn;

    private final ObservableList<ExistingGrocery> groceryItems = FXCollections.observableArrayList();

    private final ItemService itemService = new ItemService();

    @FXML
    private TableView<GroceryItem> groceryListTable;

    @FXML
    private TableColumn<GroceryItem, String> groceryNameColumn;

    @FXML
    private TableColumn<GroceryItem, Integer> quantityColumn;

    private final ObservableList<GroceryItem> groceryListItems =
            FXCollections.observableArrayList();

    /**
     * Initalizes the tables in Groceries tab
     */
    @FXML
    public void initialize() {

        // Existing Groceries table
        if (groceryTable != null) {

            existingNameColumn.setCellValueFactory(
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

            groceryTable.getSelectionModel()
                    .setSelectionMode(SelectionMode.SINGLE);
        }

        // Grocery List table
        if (groceryListTable != null) {

            groceryNameColumn.setCellValueFactory(
                    cellData -> new SimpleStringProperty(
                            cellData.getValue().getItemName()
                    )
            );

            quantityColumn.setCellValueFactory(
                    cellData -> new SimpleIntegerProperty(
                            cellData.getValue().getQuantity()
                    ).asObject()
            );

            groceryListTable.setItems(groceryListItems);

            groceryListTable.getSelectionModel()
                    .setSelectionMode(SelectionMode.SINGLE);
        }
    }

    /**
     * Navigates to the Grocery Tab
     */
    @FXML
    private void onGroceries(){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("grocery.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) Grocery.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Grocery");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a Grocery item from the Grocery List Table
     */
    @FXML
    private void onRemoveListItem() {

        GroceryItem selectedItem =
                groceryListTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            return;
        }

        groceryListItems.remove(selectedItem);
    }

    /**
     * Add an item to the Grocery List Table
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

        Stage popup = new Stage();
        popup.setTitle("Add Grocery Item");

        Scene scene = new Scene(layout, 300, 300);
        popup.setScene(scene);

        addButton.setOnAction(event -> {

            String name = nameField.getText().trim();
            String quantityText = quantityField.getText().trim();

            if (name.isEmpty() || quantityText.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }

            int quantity;

            try {
                quantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Quantity must be a number.");
                return;
            }

            if (quantity <= 0) {
                errorLabel.setText("Quantity must be greater than 0.");
                return;
            }

            groceryListItems.add(
                    new GroceryItem(name, quantity)
            );

            nameField.clear();
            quantityField.clear();
            errorLabel.setText("");
            nameField.requestFocus();
        });

        backButton.setOnAction(event -> popup.close());

        popup.show();
    }

    /**
     * Remove an item from the existing grocery table and from database
     */
    @FXML
    private void onRemoveExistingItem() {

        ExistingGrocery selectedItem =
                groceryTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            return;
        }

        boolean success =
                itemService.deleteItem(selectedItem.getItemID());

        if (success) {
            groceryItems.remove(selectedItem);
        } else {
            System.out.println("Failed to delete grocery.");
        }
    }

    /**
     * Move an item from grocery list into the database
     */
    @FXML
    private void onMoveToExisting() {
        GroceryItem selectedItem =
                groceryListTable.getSelectionModel().getSelectedItem();

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
            groceryListItems.remove(selectedItem);

            groceryItems.clear();
            groceryItems.addAll(itemService.getItemsForCurrentUser());
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
     * Navigates back to login page and clears the session ID
     */
    @FXML
    private void onSignout() {

        Session.clearId();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("authenticator-view.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) Grocery.getScene().getWindow();

            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Login / Sign Up");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to the Home page
     */
    @FXML
    private void onHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("home-dashboard.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) homeButton.getScene().getWindow();

            stage.setScene(new Scene(root, 720, 450));
            stage.setTitle("Lifestyle Levelling");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
