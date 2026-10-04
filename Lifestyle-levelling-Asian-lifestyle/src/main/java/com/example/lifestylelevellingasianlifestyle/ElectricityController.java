package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;

/**
 * Controls the electricity tracking page.
 */
public class ElectricityController {
    @FXML
    private Button homeButton;

    @FXML
    private Button groceryButton;

    @FXML
    private DatePicker electricityMonthPicker;

    @FXML
    private TextField electricityUsageField;

    @FXML
    private TextField electricityBillField;

    @FXML
    private Label electricityStatusLabel;

    @FXML
    private Label electricityCostLabel;

    @FXML
    private Label electricityUsageLabel;

    @FXML
    private Label electricityRateLabel;

    @FXML
    private Label dailyAvgLabel;

    @FXML
    private Label vsLastMonthLabel;

    @FXML
    private Label reductionTargetLabel;

    @FXML
    private Label recommendationLabel;

    @FXML
    private TableView<ElectricityRecord> electricityTable;

    @FXML
    private TableColumn<ElectricityRecord, String> electricityMonthColumn;

    @FXML
    private TableColumn<ElectricityRecord, String> electricityUsageColumn;

    @FXML
    private TableColumn<ElectricityRecord, String> electricityBillColumn;

    @FXML
    private TableColumn<ElectricityRecord, String> electricityRateColumn;

    @FXML
    private LineChart<String, Number> electricityChart;

    private final ObservableList<ElectricityRecord> electricityRecords =
            FXCollections.observableArrayList();

    private final ElectricityService electricityService = new ElectricityService();

    /**
     * Initializes the table, chart and monthly summary.
     */
    @FXML
    public void initialize() {
        electricityMonthColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getDisplayMonth()
                )
        );

        electricityUsageColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.format(
                                Locale.ENGLISH,
                                "%.1f kWh",
                                cellData.getValue().getUsageKwh()
                        )
                )
        );

        electricityBillColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.format(
                                Locale.ENGLISH,
                                "$%.2f",
                                cellData.getValue().getBillAmount()
                        )
                )
        );

        electricityRateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        String.format(
                                Locale.ENGLISH,
                                "$%.3f",
                                cellData.getValue().getCostPerKwh()
                        )
                )
        );

        electricityTable.setItems(electricityRecords);
        electricityTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        electricityMonthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        electricityChart.setAnimated(false);

        refreshElectricityData();
    }

    /**
     * Adds a monthly electricity bill or updates the existing record for that month.
     */
    @FXML
    private void onSaveElectricity() {
        LocalDate selectedDate = electricityMonthPicker.getValue();
        String usageText = electricityUsageField.getText().trim();
        String billText = electricityBillField.getText().trim();

        if (selectedDate == null || usageText.isEmpty() || billText.isEmpty()) {
            showStatus("Please enter the month, usage and bill amount.", true);
            return;
        }

        YearMonth month = YearMonth.from(selectedDate);
        if (month.isAfter(YearMonth.now())) {
            showStatus("The billing month cannot be in the future.", true);
            return;
        }

        double usageKwh;
        double billAmount;

        try {
            usageKwh = parseNumber(usageText);
            billAmount = parseNumber(billText);
        } catch (NumberFormatException exception) {
            showStatus("Usage and bill amount must be valid numbers.", true);
            return;
        }

        if (usageKwh <= 0) {
            showStatus("Electricity usage must be greater than 0.", true);
            return;
        }

        if (billAmount < 0) {
            showStatus("Bill amount cannot be negative.", true);
            return;
        }

        if (electricityService.saveRecord(month, usageKwh, billAmount)) {
            electricityUsageField.clear();
            electricityBillField.clear();
            refreshElectricityData();
            showStatus("Monthly electricity record saved.", false);
        } else {
            showStatus("Unable to save the electricity record.", true);
        }
    }

    /**
     * Deletes the selected monthly electricity record.
     */
    @FXML
    private void onDeleteElectricity() {
        ElectricityRecord selectedRecord =
                electricityTable.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            showStatus("Please select a record to delete.", true);
            return;
        }

        if (electricityService.deleteRecord(selectedRecord.getRecordId())) {
            refreshElectricityData();
            showStatus("Electricity record deleted.", false);
        } else {
            showStatus("Unable to delete the electricity record.", true);
        }
    }

    /**
     * Clears all electricity input fields.
     */
    @FXML
    private void onClearElectricity() {
        electricityMonthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        electricityUsageField.clear();
        electricityBillField.clear();
        electricityStatusLabel.setText("");
    }

    /**
     * Navigates to the home dashboard.
     */
    @FXML
    private void onHome() {
        openPage("home-dashboard.fxml", "Lifestyle Levelling", homeButton);
    }

    /**
     * Navigates to the grocery page.
     */
    @FXML
    private void onGroceries() {openPage("grocery.fxml", "Grocery", groceryButton);
    }

    private void openPage(String resource, String title, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent root = loader.load();
            Stage stage = (Stage) sourceButton.getScene().getWindow();

            stage.setScene(new Scene(root, 850, 540));
            stage.setTitle(title);
            stage.show();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void refreshElectricityData() {
        electricityRecords.setAll(electricityService.getRecordsForCurrentUser());

        XYChart.Series<String, Number> usageSeries = new XYChart.Series<>();
        usageSeries.setName("Monthly usage");

        for (ElectricityRecord record : electricityRecords) {
            usageSeries.getData().add(new XYChart.Data<>(
                    record.getDisplayMonth(),
                    record.getUsageKwh()
            ));
        }

        electricityChart.getData().clear();
        electricityChart.getData().add(usageSeries);
        updateSummary();
    }

    private void updateSummary() {
        if (electricityRecords.isEmpty()) {
            electricityCostLabel.setText("$0.00");
            electricityUsageLabel.setText("0 kWh");
            electricityRateLabel.setText("$0.000 / kWh");
            dailyAvgLabel.setText("Daily average: 0 kWh");
            vsLastMonthLabel.setText("Add a monthly bill to begin comparing usage.");
            reductionTargetLabel.setText("5% reduction target: not available");
            recommendationLabel.setText(
                    "Add your electricity usage and bill amount to receive a recommendation."
            );
            return;
        }

        ElectricityRecord latest = electricityRecords.get(electricityRecords.size() - 1);
        double dailyAverage = latest.getUsageKwh()
                / latest.getBillingMonth().lengthOfMonth();
        double targetUsage = latest.getUsageKwh() * 0.95;
        double targetBill = targetUsage * latest.getCostPerKwh();

        electricityCostLabel.setText(String.format(
                Locale.ENGLISH,
                "$%.2f",
                latest.getBillAmount()
        ));
        electricityUsageLabel.setText(String.format(
                Locale.ENGLISH,
                "%.1f kWh",
                latest.getUsageKwh()
        ));
        electricityRateLabel.setText(String.format(
                Locale.ENGLISH,
                "$%.3f / kWh",
                latest.getCostPerKwh()
        ));
        dailyAvgLabel.setText(String.format(
                Locale.ENGLISH,
                "Daily average: %.1f kWh",
                dailyAverage
        ));
        reductionTargetLabel.setText(String.format(
                Locale.ENGLISH,
                "5%% reduction target: %.1f kWh / $%.2f",
                targetUsage,
                targetBill
        ));

        if (electricityRecords.size() == 1) {
            vsLastMonthLabel.setText("Add another month to compare your progress.");
            recommendationLabel.setText(
                    "Start with a 5% goal: switch off standby devices and use natural light."
            );
            return;
        }

        ElectricityRecord previous = electricityRecords.get(electricityRecords.size() - 2);
        double usageChange = (
                (latest.getUsageKwh() - previous.getUsageKwh())
                        / previous.getUsageKwh()
        ) * 100;

        vsLastMonthLabel.setText(String.format(
                Locale.ENGLISH,
                "vs previous month: %+.1f%%",
                usageChange
        ));

        if (usageChange > 5) {
            recommendationLabel.setText(
                    "Usage increased. Reduce heating or cooling time and switch off standby devices."
            );
        } else if (usageChange < -5) {
            recommendationLabel.setText(
                    "Great progress. Keep the habits that reduced your electricity usage."
            );
        } else {
            recommendationLabel.setText(
                    "Usage is stable. Aim for the 5% target by using appliances outside peak periods."
            );
        }
    }

    private double parseNumber(String text) {
        return Double.parseDouble(
                text.replace("$", "").replace(",", "").trim()
        );
    }

    private void showStatus(String message, boolean error) {
        electricityStatusLabel.setText(message);
        electricityStatusLabel.setStyle(
                error ? "-fx-text-fill: #b42318;" : "-fx-text-fill: #287a3d;"
        );
    }
}
