package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMigrationTest {
    @TempDir
    Path tempDirectory;

    private String previousDatabasePath;
    private Path databasePath;

    @BeforeEach
    void createLegacyDatabase() throws SQLException {
        previousDatabasePath = System.getProperty(Database.DATABASE_PATH_PROPERTY);
        databasePath = tempDirectory.resolve("legacy-groceries.db");
        System.setProperty(Database.DATABASE_PATH_PROPERTY, databasePath.toString());

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE users (
                        userId INTEGER PRIMARY KEY AUTOINCREMENT,
                        password TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE Groceries (
                        itemName TEXT NOT NULL,
                        itemId INTEGER PRIMARY KEY,
                        userId INTEGER NOT NULL,
                        expiryDate TEXT NOT NULL,
                        dateAdded TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO users (userId, password, email)
                    VALUES (5, 'legacy-password-hash', 'legacy@example.com')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Groceries (itemName, itemId, userId, expiryDate, dateAdded)
                    VALUES
                        ('Brown rice', 41, 5, '2026-10-20', '2026-09-12'),
                        ('Orphaned oats', 42, 99, '2026-11-01', '2026-09-10'),
                        ('Invalid yoghurt', 43, 5, 'not-a-date', '2026-09-11')
                    """);
        }
    }

    @AfterEach
    void restoreDatabasePath() {
        if (previousDatabasePath == null) {
            System.clearProperty(Database.DATABASE_PATH_PROPERTY);
        } else {
            System.setProperty(Database.DATABASE_PATH_PROPERTY, previousDatabasePath);
        }
    }

    @Test
    void migratesLegacyGroceriesWithoutLosingTheOriginalRows() throws SQLException {
        Database.initialize();
        Database.initialize();

        try (Connection connection = Database.connect()) {
            Set<String> groceryColumns = columnNames(connection, "groceries");
            assertTrue(groceryColumns.containsAll(Set.of(
                    "id", "user_id", "name", "category", "quantity", "unit",
                    "purchase_date", "expiry_date", "created_at"
            )));
            assertFalse(groceryColumns.contains("itemname"));

            try (Statement statement = connection.createStatement();
                 ResultSet migrated = statement.executeQuery("""
                         SELECT id, user_id, name, category, quantity, unit,
                                purchase_date, expiry_date
                         FROM groceries
                         ORDER BY id
                         """)) {
                assertTrue(migrated.next());
                assertAll(
                        () -> assertEquals(41, migrated.getLong("id")),
                        () -> assertEquals(5, migrated.getLong("user_id")),
                        () -> assertEquals("Brown rice", migrated.getString("name")),
                        () -> assertEquals("Other", migrated.getString("category")),
                        () -> assertEquals(1.0, migrated.getDouble("quantity")),
                        () -> assertEquals("item(s)", migrated.getString("unit")),
                        () -> assertEquals("2026-09-12", migrated.getString("purchase_date")),
                        () -> assertEquals("2026-10-20", migrated.getString("expiry_date"))
                );
                assertFalse(migrated.next(), "orphaned and malformed legacy rows must not enter the live table");
            }

            assertEquals(3, rowCount(connection, "legacy_groceries_archive"));
            assertEquals(1, rowCount(connection, "groceries"));
        }
    }

    private Set<String> columnNames(Connection connection, String table) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name").toLowerCase());
            }
        }
        return columns;
    }

    private int rowCount(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }
}
