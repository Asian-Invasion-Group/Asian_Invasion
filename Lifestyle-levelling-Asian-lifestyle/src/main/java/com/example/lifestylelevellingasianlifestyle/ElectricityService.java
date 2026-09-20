package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides database operations for monthly electricity records.
 */
public class ElectricityService {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS ElectricityUsage (
                recordId INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER NOT NULL,
                billingMonth TEXT NOT NULL,
                usageKwh REAL NOT NULL CHECK (usageKwh > 0),
                billAmount REAL NOT NULL CHECK (billAmount >= 0),
                UNIQUE (userId, billingMonth),
                FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
            )
            """;

    /**
     * Returns all monthly records for the current user in chronological order.
     */
    public List<ElectricityRecord> getRecordsForCurrentUser() {
        List<ElectricityRecord> records = new ArrayList<>();

        String sql = """
                SELECT recordId, userId, billingMonth, usageKwh, billAmount
                FROM ElectricityUsage
                WHERE userId = ?
                ORDER BY billingMonth ASC
                """;

        try (Connection connection = Database.connect()) {
            ensureTable(connection);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Session.getUserId());

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(new ElectricityRecord(
                                result.getInt("recordId"),
                                result.getInt("userId"),
                                YearMonth.parse(result.getString("billingMonth")),
                                result.getDouble("usageKwh"),
                                result.getDouble("billAmount")
                        ));
                    }
                }
            }
        } catch (SQLException | RuntimeException exception) {
            exception.printStackTrace();
        }

        return records;
    }

    /**
     * Adds a monthly bill or updates the existing bill for the same month.
     */
    public boolean saveRecord(YearMonth month, double usageKwh, double billAmount) {
        String sql = """
                INSERT INTO ElectricityUsage (userId, billingMonth, usageKwh, billAmount)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (userId, billingMonth) DO UPDATE SET
                    usageKwh = excluded.usageKwh,
                    billAmount = excluded.billAmount
                """;

        try (Connection connection = Database.connect()) {
            ensureTable(connection);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Session.getUserId());
                statement.setString(2, month.toString());
                statement.setDouble(3, usageKwh);
                statement.setDouble(4, billAmount);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a monthly record belonging to the current user.
     */
    public boolean deleteRecord(int recordId) {
        String sql = """
                DELETE FROM ElectricityUsage
                WHERE recordId = ? AND userId = ?
                """;

        try (Connection connection = Database.connect()) {
            ensureTable(connection);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, recordId);
                statement.setInt(2, Session.getUserId());
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private void ensureTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        }
    }
}
