package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Coordinates the unified groceries, electricity, weather and overview screens. */
public final class MainController {
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final GroceryService groceryService = new GroceryService();
    private final ElectricityService electricityService = new ElectricityService();
    private final WeatherService weatherService = new WeatherService();
    private final WeatherPreferenceService weatherPreferenceService = new WeatherPreferenceService();
    private final ObservableList<GroceryItem> groceries = FXCollections.observableArrayList();
    private final FilteredList<GroceryItem> filteredGroceries = new FilteredList<>(groceries, item -> true);
    private final ObservableList<ElectricityRecord> electricityRecords = FXCollections.observableArrayList();
    private final ObservableList<WeatherDay> weatherDays = FXCollections.observableArrayList();

    private AuthenticationService.UserSession session;
    private long editingGroceryId;
    private WeatherForecast latestForecast;
    private boolean weatherLoading;

    @FXML private Label userEmailLabel;
    @FXML private Label dashboardGroceryCountLabel;
    @FXML private Label dashboardGroceryDetailLabel;
    @FXML private Label dashboardElectricityLabel;
    @FXML private Label dashboardElectricityDetailLabel;
    @FXML private Label dashboardWeatherLabel;
    @FXML private VBox dashboardActionsBox;

    @FXML private TextField groceryNameField;
    @FXML private ComboBox<String> groceryCategoryBox;
    @FXML private TextField groceryQuantityField;
    @FXML private ComboBox<String> groceryUnitBox;
    @FXML private DatePicker groceryPurchaseDatePicker;
    @FXML private DatePicker groceryExpiryDatePicker;
    @FXML private TextField grocerySearchField;
    @FXML private Label groceryFormStatusLabel;
    @FXML private Label groceryRecommendationLabel;
    @FXML private Button grocerySaveButton;
    @FXML private TableView<GroceryItem> groceryTable;
    @FXML private TableColumn<GroceryItem, String> groceryNameColumn;
    @FXML private TableColumn<GroceryItem, String> groceryCategoryColumn;
    @FXML private TableColumn<GroceryItem, String> groceryQuantityColumn;
    @FXML private TableColumn<GroceryItem, String> groceryExpiryColumn;
    @FXML private TableColumn<GroceryItem, String> groceryStatusColumn;

    @FXML private DatePicker electricityMonthPicker;
    @FXML private TextField electricityKwhField;
    @FXML private TextField electricityBillField;
    @FXML private Label electricityFormStatusLabel;
    @FXML private Label electricityRecommendationLabel;
    @FXML private TableView<ElectricityRecord> electricityTable;
    @FXML private TableColumn<ElectricityRecord, String> electricityMonthColumn;
    @FXML private TableColumn<ElectricityRecord, String> electricityKwhColumn;
    @FXML private TableColumn<ElectricityRecord, String> electricityBillColumn;
    @FXML private BarChart<String, Number> electricityChart;

    @FXML private TextField weatherLocationField;
    @FXML private Button weatherSearchButton;
    @FXML private ProgressIndicator weatherProgress;
    @FXML private Label weatherStatusLabel;
    @FXML private Label weatherSunnyIntervalLabel;
    @FXML private Label weatherLaundryLabel;
    @FXML private Label weatherWalkLabel;
    @FXML private TableView<WeatherDay> weatherTable;
    @FXML private TableColumn<WeatherDay, String> weatherDateColumn;
    @FXML private TableColumn<WeatherDay, String> weatherSummaryColumn;
    @FXML private TableColumn<WeatherDay, String> weatherTemperatureColumn;
    @FXML private TableColumn<WeatherDay, String> weatherRainColumn;
    @FXML private TableColumn<WeatherDay, String> weatherLaundryColumn;
    @FXML private TableColumn<WeatherDay, String> weatherWalkColumn;

    @FXML
    private void initialize() {
        configureGroceryControls();
        configureElectricityControls();
        configureWeatherControls();
    }

    public void setSession(AuthenticationService.UserSession session) {
        this.session = session;
        userEmailLabel.setText(session.email());
        try {
            weatherLocationField.setText(weatherPreferenceService.findForUser(session.userId())
                    .map(WeatherService.Location::displayName)
                    .orElse("Brisbane"));
        } catch (SQLException e) {
            weatherLocationField.setText("Brisbane");
            setWeatherStatus("Your saved location could not be loaded; enter a location to continue.", false);
        }
        onRefreshAll();
    }

    private void configureGroceryControls() {
        groceryCategoryBox.setItems(FXCollections.observableArrayList(
                "Fruit", "Vegetables", "Dairy", "Meat & seafood", "Bakery", "Pantry", "Frozen", "Other"
        ));
        groceryUnitBox.setItems(FXCollections.observableArrayList(
                "item(s)", "g", "kg", "ml", "L", "pack(s)"
        ));
        groceryCategoryBox.setValue("Other");
        groceryUnitBox.setValue("item(s)");
        groceryPurchaseDatePicker.setValue(LocalDate.now());
        groceryExpiryDatePicker.setValue(LocalDate.now().plusDays(7));

        groceryNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        groceryCategoryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCategory()));
        groceryQuantityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                displayNumber(data.getValue().getQuantity()) + " " + data.getValue().getUnit()
        ));
        groceryExpiryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                SHORT_DATE.format(data.getValue().getExpiryDate())
        ));
        groceryStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(groceryStatusText(data.getValue())));
        groceryTable.setItems(filteredGroceries);
        grocerySearchField.textProperty().addListener((observable, oldValue, newValue) -> applyGroceryFilter(newValue));
        groceryTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(GroceryItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("expired-row", "expiring-row");
                if (!empty && item != null) {
                    switch (item.getExpiryStatus()) {
                        case EXPIRED -> getStyleClass().add("expired-row");
                        case EXPIRING_TODAY, EXPIRING_SOON -> getStyleClass().add("expiring-row");
                        default -> { }
                    }
                }
            }
        });
    }

    private void configureElectricityControls() {
        electricityMonthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        electricityMonthColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                MONTH_LABEL.format(data.getValue().getBillingMonth().atDay(1))
        ));
        electricityKwhColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                displayDecimal(data.getValue().getKwh())
        ));
        electricityBillColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                displayMoney(data.getValue().getBillAmount())
        ));
        electricityTable.setItems(electricityRecords);
        electricityTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, record) -> {
            if (record != null) {
                electricityMonthPicker.setValue(record.getBillingMonth().atDay(1));
                electricityKwhField.setText(record.getKwh().toPlainString());
                electricityBillField.setText(record.getBillAmount().toPlainString());
            }
        });
    }

    private void configureWeatherControls() {
        weatherDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(SHORT_DATE.format(data.getValue().getDate())));
        weatherSummaryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getWeatherDescription()));
        weatherTemperatureColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format(
                Locale.ENGLISH, "%.0f–%.0f °C",
                (double) data.getValue().getMinTemperatureC(),
                (double) data.getValue().getMaxTemperatureC()
        )));

        weatherRainColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format(
                Locale.ENGLISH, "%.0f%% | %.1f mm",
                (double) data.getValue().getPrecipitationProbabilityMaxPercent(),
                (double) data.getValue().getRainSumMm()
        )));
        weatherLaundryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isLaundrySuitable() ? "Recommended" : "Not ideal"
        ));
        weatherWalkColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().isWalkSuitable() ? "Recommended" : "Not ideal"
        ));
        weatherTable.setItems(weatherDays);
        weatherTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(WeatherDay day, boolean empty) {
                super.updateItem(day, empty);
                setTooltip(empty || day == null ? null : new Tooltip(
                        "Laundry: " + day.getLaundryReason() + "\nWalk: " + day.getWalkReason()
                ));
            }
        });
    }

    @FXML
    private void onRefreshAll() {
        if (session == null) {
            return;
        }
        refreshGroceries();
        refreshElectricity();
        refreshOverview();
    }

    private void refreshGroceries() {
        try {
            groceries.setAll(groceryService.findByUserId(session.userId()));
            updateGroceryRecommendation();
        } catch (SQLException e) {
            setStatus(groceryFormStatusLabel, "Could not load groceries: " + friendlyDatabaseMessage(e), false);
        }
    }

    private void refreshElectricity() {
        try {
            electricityRecords.setAll(electricityService.findByUserId(session.userId()));
            renderElectricityChart();
            ElectricityInsights insights = ElectricityInsights.calculate(electricityRecords);
            electricityRecommendationLabel.setText(String.join("  •  ", insights.getRecommendations()));
        } catch (SQLException e) {
            setStatus(electricityFormStatusLabel, "Could not load bills: " + friendlyDatabaseMessage(e), false);
        }
    }

    @FXML
    private void onSaveGrocery() {
        try {
            String name = groceryNameField.getText();
            String category = groceryCategoryBox.getValue();
            String unit = groceryUnitBox.getValue();
            double quantity = Double.parseDouble(groceryQuantityField.getText().trim());
            LocalDate purchased = groceryPurchaseDatePicker.getValue();
            LocalDate expiry = groceryExpiryDatePicker.getValue();

            if (editingGroceryId == 0) {
                groceryService.add(session.userId(), name, category, quantity, unit, purchased, expiry);
                setStatus(groceryFormStatusLabel, "Item added to your inventory.", true);
            } else {
                GroceryItem edited = new GroceryItem(
                        editingGroceryId, session.userId(), name, category, quantity, unit, purchased, expiry
                );
                if (!groceryService.update(session.userId(), edited)) {
                    throw new SQLException("The selected item no longer exists");
                }
                setStatus(groceryFormStatusLabel, "Item updated.", true);
            }
            resetGroceryForm(false);
            refreshGroceries();
            refreshOverview();
        } catch (NumberFormatException e) {
            setStatus(groceryFormStatusLabel, "Quantity must be a positive number.", false);
        } catch (IllegalArgumentException e) {
            setStatus(groceryFormStatusLabel, friendlyValidationMessage(e), false);
        } catch (SQLException e) {
            setStatus(groceryFormStatusLabel, "Could not save this item: " + friendlyDatabaseMessage(e), false);
        }
    }

    @FXML
    private void onEditGrocery() {
        GroceryItem item = groceryTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            setStatus(groceryFormStatusLabel, "Select an item in the table first.", false);
            return;
        }
        editingGroceryId = item.getId();
        groceryNameField.setText(item.getName());
        groceryCategoryBox.setValue(item.getCategory());
        groceryQuantityField.setText(displayNumber(item.getQuantity()));
        groceryUnitBox.setValue(item.getUnit());
        groceryPurchaseDatePicker.setValue(item.getPurchaseDate());
        groceryExpiryDatePicker.setValue(item.getExpiryDate());
        grocerySaveButton.setText("Update item");
        setStatus(groceryFormStatusLabel, "Editing " + item.getName() + ".", true);
    }

    @FXML
    private void onDeleteGrocery() {
        GroceryItem item = groceryTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            setStatus(groceryFormStatusLabel, "Select an item to remove.", false);
            return;
        }
        if (!confirm("Remove grocery item?", "Remove “" + item.getName() + "” from your current stock?")) {
            return;
        }
        try {
            groceryService.delete(session.userId(), item.getId());
            resetGroceryForm(true);
            refreshGroceries();
            refreshOverview();
            setStatus(groceryFormStatusLabel, "Item removed.", true);
        } catch (SQLException e) {
            setStatus(groceryFormStatusLabel, "Could not remove this item: " + friendlyDatabaseMessage(e), false);
        }
    }

    @FXML
    private void onClearGroceryForm() {
        resetGroceryForm(true);
    }

    private void resetGroceryForm(boolean clearStatus) {
        editingGroceryId = 0;
        groceryNameField.clear();
        groceryQuantityField.clear();
        groceryCategoryBox.setValue("Other");
        groceryUnitBox.setValue("item(s)");
        groceryPurchaseDatePicker.setValue(LocalDate.now());
        groceryExpiryDatePicker.setValue(LocalDate.now().plusDays(7));
        grocerySaveButton.setText("Add item");
        groceryTable.getSelectionModel().clearSelection();
        if (clearStatus) {
            groceryFormStatusLabel.setText("");
        }
    }

    private void applyGroceryFilter(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filteredGroceries.setPredicate(item -> normalized.isEmpty()
                || item.getName().toLowerCase(Locale.ROOT).contains(normalized)
                || item.getCategory().toLowerCase(Locale.ROOT).contains(normalized));
    }

    private void updateGroceryRecommendation() {
        if (groceries.isEmpty()) {
            groceryRecommendationLabel.setText("Add an item to receive a recommendation.");
            return;
        }
        GroceryItem first = groceries.stream()
                .min(Comparator.comparing(GroceryItem::getExpiryDate).thenComparing(GroceryItem::getName))
                .orElseThrow();
        groceryRecommendationLabel.setText(first.getName() + ": " + first.getRecommendation());
    }

    @FXML
    private void onSaveElectricity() {
        try {
            LocalDate selectedDate = electricityMonthPicker.getValue();
            if (selectedDate == null) {
                throw new IllegalArgumentException("billing month is required");
            }
            YearMonth month = YearMonth.from(selectedDate);
            BigDecimal kwh = new BigDecimal(electricityKwhField.getText().trim());
            BigDecimal bill = new BigDecimal(electricityBillField.getText().trim());
            electricityService.upsert(session.userId(), month, kwh, bill);
            setStatus(electricityFormStatusLabel, "Monthly bill saved. Existing months are updated safely.", true);
            electricityKwhField.clear();
            electricityBillField.clear();
            electricityTable.getSelectionModel().clearSelection();
            refreshElectricity();
            refreshOverview();
        } catch (NumberFormatException e) {
            setStatus(electricityFormStatusLabel, "Usage and bill amount must be valid numbers.", false);
        } catch (IllegalArgumentException e) {
            setStatus(electricityFormStatusLabel, friendlyValidationMessage(e), false);
        } catch (SQLException e) {
            setStatus(electricityFormStatusLabel, "Could not save this bill: " + friendlyDatabaseMessage(e), false);
        }
    }

    @FXML
    private void onDeleteElectricity() {
        ElectricityRecord record = electricityTable.getSelectionModel().getSelectedItem();
        if (record == null) {
            setStatus(electricityFormStatusLabel, "Select a month in the table first.", false);
            return;
        }
        if (!confirm("Delete monthly bill?", "Delete the record for " + MONTH_LABEL.format(record.getBillingMonth().atDay(1)) + "?")) {
            return;
        }
        try {
            electricityService.delete(session.userId(), record.getId());
            refreshElectricity();
            refreshOverview();
            setStatus(electricityFormStatusLabel, "Monthly bill deleted.", true);
        } catch (SQLException e) {
            setStatus(electricityFormStatusLabel, "Could not delete this bill: " + friendlyDatabaseMessage(e), false);
        }
    }

    private void renderElectricityChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int start = Math.max(0, electricityRecords.size() - 12);
        for (int index = start; index < electricityRecords.size(); index++) {
            ElectricityRecord record = electricityRecords.get(index);
            series.getData().add(new XYChart.Data<>(
                    MONTH_LABEL.format(record.getBillingMonth().atDay(1)), record.getKwh()
            ));
        }
        electricityChart.getData().setAll(List.of(series));
    }

    @FXML
    private void onLoadWeather() {
        if (weatherLoading) {
            return;
        }
        String location = weatherLocationField.getText() == null ? "" : weatherLocationField.getText().trim();
        if (location.isEmpty()) {
            setWeatherStatus("Enter a city or suburb first.", false);
            return;
        }

        weatherLoading = true;
        weatherSearchButton.setDisable(true);
        weatherLocationField.setDisable(true);
        weatherProgress.setVisible(true);
        weatherProgress.setManaged(true);
        setWeatherStatus("Finding " + location + " and loading its forecast…", true);

        Task<WeatherForecast> task = new Task<>() {
            @Override
            protected WeatherForecast call() throws Exception {
                return weatherService.getForecast(location);
            }
        };
        task.setOnSucceeded(event -> {
            finishWeatherLoad();
            latestForecast = task.getValue();
            renderWeatherForecast(latestForecast);
            try {
                weatherPreferenceService.save(session.userId(), new WeatherService.Location(
                        latestForecast.getLocationName(),
                        latestForecast.getLatitude(),
                        latestForecast.getLongitude(),
                        latestForecast.getTimezone()
                ));
            } catch (SQLException exception) {
                setWeatherStatus("Forecast loaded, but this location could not be saved for next time.", false);
            }
            refreshOverview();
        });
        task.setOnFailed(event -> {
            finishWeatherLoad();
            Throwable failure = task.getException();
            setWeatherStatus(weatherFailureMessage(failure), false);
        });

        Thread worker = new Thread(task, "weather-forecast-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private void finishWeatherLoad() {
        weatherLoading = false;
        weatherSearchButton.setDisable(false);
        weatherLocationField.setDisable(false);
        weatherProgress.setVisible(false);
        weatherProgress.setManaged(false);
    }

    private void renderWeatherForecast(WeatherForecast forecast) {
        weatherDays.setAll(forecast.getDays());
        weatherLocationField.setText(forecast.getLocationName());
        setWeatherStatus("7-day forecast for " + forecast.getLocationName() + " (" + forecast.getTimezone() + ").", true);
        weatherSunnyIntervalLabel.setText(forecast.getSunnyIntervalSummary());

        Optional<WeatherDay> laundryDay = forecast.getDays().stream().filter(WeatherDay::isLaundrySuitable).findFirst();
        Optional<WeatherDay> walkDay = forecast.getDays().stream().filter(WeatherDay::isWalkSuitable).findFirst();
        weatherLaundryLabel.setText(laundryDay
                .map(day -> SHORT_DATE.format(day.getDate()) + " — " + day.getLaundryReason())
                .orElse("No ideal drying day is forecast this week; use covered drying where possible."));
        weatherWalkLabel.setText(walkDay
                .map(day -> SHORT_DATE.format(day.getDate()) + " — " + day.getWalkReason())
                .orElse("No ideal walking day is forecast this week; consider a short indoor activity."));
    }

    private void refreshOverview() {
        if (session == null) {
            return;
        }

        dashboardGroceryCountLabel.setText(String.valueOf(groceries.size()));
        long urgentCount = groceries.stream()
                .filter(item -> item.getExpiryStatus() != GroceryItem.ExpiryStatus.FRESH)
                .count();
        dashboardGroceryDetailLabel.setText(urgentCount == 0
                ? (groceries.isEmpty() ? "Add your current stock" : "Nothing expiring in the next 3 days")
                : urgentCount + (urgentCount == 1 ? " item needs attention" : " items need attention"));

        ElectricityInsights insights = ElectricityInsights.calculate(electricityRecords);
        dashboardElectricityLabel.setText(insights.getLatestKwh()
                .map(value -> displayDecimal(value) + " kWh")
                .orElse("—"));
        dashboardElectricityDetailLabel.setText(insights.getLatestMonthChangePercent()
                .map(change -> (change.signum() > 0 ? "+" : "") + displayDecimal(change) + "% vs previous month")
                .orElse(electricityRecords.isEmpty() ? "Add your first bill" : "Add another month to compare"));

        if (latestForecast == null) {
            dashboardWeatherLabel.setText("Check weather");
        } else {
            Optional<WeatherDay> nextOutdoor = latestForecast.getDays().stream().filter(WeatherDay::isWalkSuitable).findFirst();
            dashboardWeatherLabel.setText(nextOutdoor
                    .map(day -> SHORT_DATE.format(day.getDate()))
                    .orElse("No ideal day"));
        }

        dashboardActionsBox.getChildren().clear();
        groceries.stream().min(Comparator.comparing(GroceryItem::getExpiryDate)).ifPresent(item ->
                addDashboardAction("FOOD", "Use " + item.getName(), item.getRecommendation())
        );
        if (!electricityRecords.isEmpty()) {
            String recommendation = insights.getRecommendations().isEmpty()
                    ? "Keep monitoring monthly use."
                    : insights.getRecommendations().get(0);
            addDashboardAction("ENERGY", "Review your latest bill", recommendation);
        }
        if (latestForecast != null) {
            latestForecast.getDays().stream().filter(WeatherDay::isLaundrySuitable).findFirst().ifPresent(day ->
                    addDashboardAction("WEATHER", "Line-dry on " + SHORT_DATE.format(day.getDate()), day.getLaundryReason())
            );
        }
        if (dashboardActionsBox.getChildren().isEmpty()) {
            Label empty = new Label("Add groceries, an electricity bill, or a weather location to get personalised actions.");
            empty.setWrapText(true);
            empty.getStyleClass().add("empty-copy");
            dashboardActionsBox.getChildren().add(empty);
        }
    }

    private void addDashboardAction(String tag, String title, String detail) {
        Label tagLabel = new Label(tag);
        tagLabel.getStyleClass().add("action-tag");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-title");
        Label detailLabel = new Label(detail);
        detailLabel.setWrapText(true);
        detailLabel.getStyleClass().add("action-detail");
        VBox copy = new VBox(3, titleLabel, detailLabel);
        HBox row = new HBox(12, tagLabel, copy);
        row.getStyleClass().add("action-row");
        dashboardActionsBox.getChildren().add(row);
    }

    @FXML
    private void onSignOut() {
        try {
            Launcher.showAuthentication((Stage) userEmailLabel.getScene().getWindow());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "The sign-in screen could not be opened.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private static String groceryStatusText(GroceryItem item) {
        long days = item.getDaysUntilExpiry();
        return switch (item.getExpiryStatus()) {
            case EXPIRED -> "Expired";
            case EXPIRING_TODAY -> "Use today";
            case EXPIRING_SOON -> days + (days == 1 ? " day left" : " days left");
            case FRESH -> days + (days == 1 ? " day left" : " days left");
        };
    }

    private static String displayNumber(double value) {
        if (Math.rint(value) == value) {
            return Long.toString(Math.round(value));
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String displayDecimal(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private static String displayMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static void setStatus(Label label, String message, boolean success) {
        label.setText(message);
        label.getStyleClass().removeAll("success-status", "neutral-status");
        if (success) {
            label.getStyleClass().add("success-status");
        }
    }

    private void setWeatherStatus(String message, boolean success) {
        setStatus(weatherStatusLabel, message, success);
    }

    private static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle("Lifestyle Levelling");
        alert.setHeaderText(title);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static String friendlyValidationMessage(IllegalArgumentException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Check the values and try again.";
        }
        return Character.toUpperCase(message.charAt(0)) + message.substring(1) + ".";
    }

    private static String friendlyDatabaseMessage(SQLException exception) {
        String message = exception.getMessage();
        if (message != null && (message.toLowerCase(Locale.ROOT).contains("busy")
                || message.toLowerCase(Locale.ROOT).contains("locked"))) {
            return "local data is busy; wait a moment and try again";
        }
        return "local storage is unavailable; please try again";
    }

    private static String weatherFailureMessage(Throwable failure) {
        if (failure instanceof WeatherService.WeatherServiceException
                && failure.getMessage() != null
                && !failure.getMessage().isBlank()) {
            return failure.getMessage();
        }
        if (failure instanceof IllegalArgumentException) {
            return "The location is not valid. Try a city with its region or country.";
        }
        return "Weather could not be loaded. Check your connection and try again.";
    }
}
