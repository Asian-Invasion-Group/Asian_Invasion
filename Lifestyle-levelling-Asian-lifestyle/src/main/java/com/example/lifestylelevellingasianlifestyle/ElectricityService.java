package com.example.lifestylelevellingasianlifestyle;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Persists and queries monthly electricity records. Every operation includes user_id in its
 * predicate, preventing one signed-in user from reading or changing another user's records.
 */
public final class ElectricityService {
    @FunctionalInterface
    public interface ConnectionFactory {
        Connection open() throws SQLException;
    }

    private final ConnectionFactory connectionFactory;

    public ElectricityService() {
        this(Database::connect);
    }

    public ElectricityService(ConnectionFactory connectionFactory) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory");
    }

    /** Returns a user's records in ascending billing-month order. */
    public List<ElectricityRecord> findByUserId(long userId) throws SQLException {
        requireUserId(userId);
        String sql = """
                SELECT id, user_id, billing_month, kwh, bill_amount
                FROM electricity_records
                WHERE user_id = ?
                ORDER BY billing_month ASC
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ElectricityRecord> records = new ArrayList<>();
                while (resultSet.next()) {
                    records.add(readRecord(resultSet));
                }
                return List.copyOf(records);
            }
        }
    }

    public Optional<ElectricityRecord> findByMonth(long userId, YearMonth billingMonth) throws SQLException {
        requireUserId(userId);
        Objects.requireNonNull(billingMonth, "billingMonth");
        try (Connection connection = connectionFactory.open()) {
            return findByMonth(connection, userId, billingMonth);
        }
    }

    /** Inserts a new month. A duplicate user/month is reported as a {@link SQLException}. */
    public ElectricityRecord add(
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) throws SQLException {
        validatePersistentValues(userId, billingMonth, kwh, billAmount);
        String sql = """
                INSERT INTO electricity_records (user_id, billing_month, kwh, bill_amount)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, userId, billingMonth, kwh, billAmount);
            statement.executeUpdate();
            return requireByMonth(connection, userId, billingMonth);
        }
    }

    /** Inserts a month or replaces its kWh and bill amount for the same user. */
    public ElectricityRecord upsert(
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) throws SQLException {
        validatePersistentValues(userId, billingMonth, kwh, billAmount);
        String sql = """
                INSERT INTO electricity_records (user_id, billing_month, kwh, bill_amount)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(user_id, billing_month) DO UPDATE SET
                    kwh = excluded.kwh,
                    bill_amount = excluded.bill_amount
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setValues(statement, userId, billingMonth, kwh, billAmount);
            statement.executeUpdate();
            return requireByMonth(connection, userId, billingMonth);
        }
    }

    /** Updates kWh and amount for an existing user/month without creating a missing row. */
    public boolean update(
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) throws SQLException {
        validatePersistentValues(userId, billingMonth, kwh, billAmount);
        String sql = """
                UPDATE electricity_records
                SET kwh = ?, bill_amount = ?
                WHERE user_id = ? AND billing_month = ?
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, kwh);
            statement.setBigDecimal(2, billAmount);
            statement.setLong(3, userId);
            statement.setString(4, billingMonth.toString());
            return statement.executeUpdate() == 1;
        }
    }

    /** Updates a record by id, still requiring ownership by userId. */
    public boolean update(
            long userId,
            long recordId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) throws SQLException {
        validatePersistentValues(userId, billingMonth, kwh, billAmount);
        requireRecordId(recordId);
        String sql = """
                UPDATE electricity_records
                SET billing_month = ?, kwh = ?, bill_amount = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, billingMonth.toString());
            statement.setBigDecimal(2, kwh);
            statement.setBigDecimal(3, billAmount);
            statement.setLong(4, recordId);
            statement.setLong(5, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long userId, YearMonth billingMonth) throws SQLException {
        requireUserId(userId);
        Objects.requireNonNull(billingMonth, "billingMonth");
        String sql = "DELETE FROM electricity_records WHERE user_id = ? AND billing_month = ?";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, billingMonth.toString());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long userId, long recordId) throws SQLException {
        requireUserId(userId);
        requireRecordId(recordId);
        String sql = "DELETE FROM electricity_records WHERE id = ? AND user_id = ?";

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, recordId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public ElectricityInsights insightsFor(long userId) throws SQLException {
        return ElectricityInsights.calculate(findByUserId(userId));
    }

    private Optional<ElectricityRecord> findByMonth(
            Connection connection,
            long userId,
            YearMonth billingMonth
    ) throws SQLException {
        String sql = """
                SELECT id, user_id, billing_month, kwh, bill_amount
                FROM electricity_records
                WHERE user_id = ? AND billing_month = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, billingMonth.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(readRecord(resultSet)) : Optional.empty();
            }
        }
    }

    private ElectricityRecord requireByMonth(
            Connection connection,
            long userId,
            YearMonth billingMonth
    ) throws SQLException {
        return findByMonth(connection, userId, billingMonth)
                .orElseThrow(() -> new SQLException("Electricity record was not found after write"));
    }

    private static ElectricityRecord readRecord(ResultSet resultSet) throws SQLException {
        String monthValue = resultSet.getString("billing_month");
        try {
            return new ElectricityRecord(
                    resultSet.getLong("id"),
                    resultSet.getLong("user_id"),
                    YearMonth.parse(monthValue),
                    decimal(resultSet, "kwh"),
                    decimal(resultSet, "bill_amount")
            );
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new SQLException("Invalid electricity record in the database", exception);
        }
    }

    private static BigDecimal decimal(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        if (value == null) {
            throw new SQLException(column + " must not be null");
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new SQLException(column + " is not a valid decimal", exception);
        }
    }

    private static void setValues(
            PreparedStatement statement,
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) throws SQLException {
        statement.setLong(1, userId);
        statement.setString(2, billingMonth.toString());
        statement.setBigDecimal(3, kwh);
        statement.setBigDecimal(4, billAmount);
    }

    private static void validatePersistentValues(
            long userId,
            YearMonth billingMonth,
            BigDecimal kwh,
            BigDecimal billAmount
    ) {
        requireUserId(userId);
        Objects.requireNonNull(billingMonth, "billingMonth");
        Objects.requireNonNull(kwh, "kwh");
        Objects.requireNonNull(billAmount, "billAmount");
        if (kwh.signum() <= 0) {
            throw new IllegalArgumentException("kwh must be greater than zero");
        }
        if (billAmount.signum() < 0) {
            throw new IllegalArgumentException("billAmount must not be negative");
        }
    }

    private static void requireUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }

    private static void requireRecordId(long recordId) {
        if (recordId <= 0) {
            throw new IllegalArgumentException("recordId must be positive");
        }
    }
}
