package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the Grocery Page UI
 * Functionality for adding and removing
 * Manages both Grocery List and Existing Grocery
 */
public class GroceryController {

    @FXML
    public Button homeButton;
    @FXML
    public Button utilityButton;
    @FXML
    public Button weatherButton;
    @FXML
    public Button groceryButton;
    @FXML
    public Button profileButton;

    @FXML
    private TableView<ExistingGrocery> groceryTable;
    @FXML
    private TableColumn<ExistingGrocery, String> existingNameColumn;
    @FXML
    private TableColumn<ExistingGrocery, String> expiryColumn;
    @FXML
    private TableColumn<ExistingGrocery, String> dateAddedColumn;
    @FXML
    private TableView<GroceryItem> groceryListTable;
    @FXML
    private TableColumn<GroceryItem, String> groceryNameColumn;
    @FXML
    private TableColumn<GroceryItem, Integer> quantityColumn;

    private final ObservableList<ExistingGrocery> groceryItems =
            FXCollections.observableArrayList();
    private final ObservableList<GroceryItem> groceryListItems =
            FXCollections.observableArrayList();

    private final ItemService itemService = new ItemService();

    /**
     * Initialiszes both the Grocery List and Existing Grocery tables
     * Sets up columns, load data etc
     */
    @FXML
    public void initialize() {

        // Existing Groceries table
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
        groceryItems.addAll(itemService.getItemsForCurrentUser());

        groceryTable.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);

        // Colour existing grocery rows
        groceryTable.setRowFactory(tableView ->
                new TableRow<ExistingGrocery>() {

                    @Override
                    protected void updateItem(
                            ExistingGrocery item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setStyle("");
                            return;
                        }

                        LocalDate expiryDate =
                                LocalDate.parse(item.getExpiryDate());

                        LocalDate today = LocalDate.now();

                        long daysUntilExpiry =
                                ChronoUnit.DAYS.between(today, expiryDate);

                        if (daysUntilExpiry <= 3) {
                            setStyle(
                                    "-fx-background-color: #ff9999;"
                            );
                        } else if (daysUntilExpiry <= 7) {
                            setStyle(
                                    "-fx-background-color: #ffcc80;"
                            );
                        } else {
                            setStyle("");
                        }
                    }
                }
        );

        // Grocery List table
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

    /**
     * Removes selected item from Grocery List
     */
    @FXML
    private void onRemoveListItem() {

        GroceryItem selectedItem =
                groceryListTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedItem == null) {
            return;
        }

        groceryListItems.remove(selectedItem);
    }

    /**
     * Add an item to Grocery List
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
                errorLabel.setText(
                        "Quantity must be greater than 0."
                );
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
     * Removes an item from existing grocery and from database
     */
    @FXML
    private void onRemoveExistingItem() {

        ExistingGrocery selectedItem =
                groceryTable.getSelectionModel()
                        .getSelectedItem();

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
     * Move an item from List to existing grocery
     */
    @FXML
    private void onMoveToExisting() {

        GroceryItem selectedItem =
                groceryListTable.getSelectionModel()
                        .getSelectedItem();

        if (selectedItem == null) {
            return;
        }

        String itemName = selectedItem.getItemName();
        int quantity = selectedItem.getQuantity();

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

        Scene scene = new Scene(
                layout,
                350,
                100 + (quantity * 70)
        );

        popup.setScene(scene);

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

            // Add each item to SQLite
            for (DatePicker datePicker : datePickers) {

                LocalDate expiryDate =
                        datePicker.getValue();

                boolean success = itemService.addItem(
                        itemName,
                        expiryDate.toString(),
                        LocalDate.now().toString()
                );

                if (!success) {
                    errorLabel.setText(
                            "Failed to add item to database."
                    );
                    return;
                }
            }

            groceryListItems.remove(selectedItem);

            groceryItems.clear();
            groceryItems.addAll(
                    itemService.getItemsForCurrentUser()
            );

            popup.close();
        });

        backButton.setOnAction(event -> popup.close());

        popup.show();
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
     * Navigates to the Grocery Tab
     */
    @FXML
    private void onGroceries() {
        NavigationController.onGroceries(groceryButton);
    }

    /**
     * Navigates to the Grocery Tab
     */
    @FXML
    private void onProfile() {
        NavigationController.onProfile(profileButton);
    }
}