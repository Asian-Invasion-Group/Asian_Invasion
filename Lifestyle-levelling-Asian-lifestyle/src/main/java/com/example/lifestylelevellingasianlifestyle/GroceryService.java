package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists grocery inventory entries and enforces ownership on every read,
 * update and delete operation.
 */
public final class GroceryService {
    private static final String SELECT_BY_USER = """
            SELECT id, user_id, name, category, quantity, unit, purchase_date, expiry_date
            FROM groceries
            WHERE user_id = ?
            ORDER BY expiry_date ASC, id ASC
            """;

    private static final String INSERT = """
            INSERT INTO groceries
                (user_id, name, category, quantity, unit, purchase_date, expiry_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE groceries
            SET name = ?, category = ?, quantity = ?, unit = ?, purchase_date = ?, expiry_date = ?
            WHERE id = ? AND user_id = ?
            """;

    private static final String DELETE = """
            DELETE FROM groceries
            WHERE id = ? AND user_id = ?
            """;

    /**
     * Lists only the requested user's groceries, ordered by nearest expiry and
     * then by id for deterministic results.
     */
    public List<GroceryItem> findByUserId(long userId) throws SQLException {
        requireUserId(userId);

        List<GroceryItem> items = new ArrayList<>();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }
        }
        return List.copyOf(items);
    }

    /**
     * Adds an unpersisted item and returns a copy containing its database id.
     */
    public GroceryItem add(GroceryItem item) throws SQLException {
        if (item == null) {
            throw new IllegalArgumentException("item is required");
        }
        if (item.getId() != 0) {
            throw new IllegalArgumentException("a new grocery item must have id 0");
        }
        return insert(item);
    }

    public GroceryItem add(
            long userId,
            String name,
            String category,
            double quantity,
            String unit,
            LocalDate purchaseDate,
            LocalDate expiryDate
    ) throws SQLException {
        return add(new GroceryItem(
                userId,
                name,
                category,
                quantity,
                unit,
                purchaseDate,
                expiryDate
        ));
    }

    /**
     * Updates an item only when both its id and owner match. A missing item or
     * an item belonging to another user returns {@code false}.
     */
    public boolean update(long userId, GroceryItem item) throws SQLException {
        requireUserId(userId);
        if (item == null) {
            throw new IllegalArgumentException("item is required");
        }
        requireItemId(item.getId());
        if (item.getUserId() != userId) {
            throw new IllegalArgumentException("item userId must match the authenticated userId");
        }

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, item.getName());
            statement.setString(2, item.getCategory());
            statement.setDouble(3, item.getQuantity());
            statement.setString(4, item.getUnit());
            statement.setString(5, item.getPurchaseDate().toString());
            statement.setString(6, item.getExpiryDate().toString());
            statement.setLong(7, item.getId());
            statement.setLong(8, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(GroceryItem item) throws SQLException {
        if (item == null) {
            throw new IllegalArgumentException("item is required");
        }
        return update(item.getUserId(), item);
    }

    /**
     * Deletes an item only when it belongs to the supplied user.
     */
    public boolean delete(long userId, long itemId) throws SQLException {
        requireUserId(userId);
        requireItemId(itemId);

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, itemId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    private GroceryItem insert(GroceryItem item) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, item.getUserId());
            statement.setString(2, item.getName());
            statement.setString(3, item.getCategory());
            statement.setDouble(4, item.getQuantity());
            statement.setString(5, item.getUnit());
            statement.setString(6, item.getPurchaseDate().toString());
            statement.setString(7, item.getExpiryDate().toString());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to add grocery item");
            }

            long generatedId = readGeneratedId(connection, statement);
            return new GroceryItem(
                    generatedId,
                    item.getUserId(),
                    item.getName(),
                    item.getCategory(),
                    item.getQuantity(),
                    item.getUnit(),
                    item.getPurchaseDate(),
                    item.getExpiryDate()
            );
        }
    }

    private static long readGeneratedId(Connection connection, PreparedStatement statement) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                if (id > 0) {
                    return id;
                }
            }
        }

        // SQLite keeps this value per connection, so this fallback is safe
        // even when multiple application windows write concurrently.
        try (Statement fallback = connection.createStatement();
             ResultSet resultSet = fallback.executeQuery("SELECT last_insert_rowid()")) {
            if (resultSet.next() && resultSet.getLong(1) > 0) {
                return resultSet.getLong(1);
            }
        }
        throw new SQLException("Grocery item was added but its generated id was not returned");
    }

    private static GroceryItem mapItem(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        try {
            return new GroceryItem(
                    id,
                    resultSet.getLong("user_id"),
                    resultSet.getString("name"),
                    resultSet.getString("category"),
                    resultSet.getDouble("quantity"),
                    resultSet.getString("unit"),
                    readDate(resultSet, "purchase_date"),
                    readDate(resultSet, "expiry_date")
            );
        } catch (IllegalArgumentException exception) {
            throw new SQLException("Invalid grocery data for row " + id, exception);
        }
    }

    private static LocalDate readDate(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        if (value == null) {
            throw new SQLException("Missing " + column + " in grocery row");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new SQLException("Invalid ISO date in grocery column " + column + ": " + value, exception);
        }
    }

    private static void requireUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }

    private static void requireItemId(long itemId) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("itemId must be positive");
        }
    }
}
