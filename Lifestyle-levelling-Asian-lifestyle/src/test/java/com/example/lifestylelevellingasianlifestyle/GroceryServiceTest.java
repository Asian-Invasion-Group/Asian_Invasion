package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroceryServiceTest {
    private static final long FIRST_USER = 101;
    private static final long SECOND_USER = 202;

    @TempDir
    Path tempDirectory;

    private String previousDatabasePath;
    private GroceryService service;

    @BeforeEach
    void setUp() throws SQLException {
        previousDatabasePath = System.getProperty(Database.DATABASE_PATH_PROPERTY);
        System.setProperty(
                Database.DATABASE_PATH_PROPERTY,
                tempDirectory.resolve("grocery-service-test.db").toString()
        );
        Database.initialize();
        createUser(FIRST_USER, "first@example.com");
        createUser(SECOND_USER, "second@example.com");
        service = new GroceryService();
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
    void addsAndListsOnlyTheUsersItemsOrderedByIsoExpiryDate() throws SQLException {
        GroceryItem later = service.add(
                FIRST_USER,
                "Oats'); DROP TABLE groceries; --",
                "Pantry",
                2,
                "kg",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 1)
        );
        GroceryItem sooner = service.add(
                FIRST_USER,
                "Yoghurt",
                "Dairy",
                4,
                "cups",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 14)
        );
        service.add(
                SECOND_USER,
                "Apples",
                "Fruit",
                6,
                "items",
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 13)
        );

        List<GroceryItem> firstUsersItems = service.findByUserId(FIRST_USER);

        assertAll(
                () -> assertEquals(List.of(sooner, later), firstUsersItems),
                () -> assertEquals(1, service.findByUserId(SECOND_USER).size()),
                () -> assertEquals("Oats'); DROP TABLE groceries; --", later.getName()),
                () -> assertEquals("2026-09-01", storedDate(later.getId(), "purchase_date")),
                () -> assertEquals("2026-10-01", storedDate(later.getId(), "expiry_date"))
        );
    }

    @Test
    void updateAndDeleteRequireTheOwningUser() throws SQLException {
        GroceryItem saved = service.add(
                FIRST_USER,
                "Rice",
                "Pantry",
                1,
                "kg",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 9, 1)
        );
        GroceryItem forgedForSecondUser = new GroceryItem(
                saved.getId(),
                SECOND_USER,
                "Changed by another user",
                "Pantry",
                5,
                "kg",
                saved.getPurchaseDate(),
                saved.getExpiryDate()
        );

        assertFalse(service.update(SECOND_USER, forgedForSecondUser));
        assertFalse(service.delete(SECOND_USER, saved.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(FIRST_USER, forgedForSecondUser));

        GroceryItem updated = new GroceryItem(
                saved.getId(),
                FIRST_USER,
                "Brown rice",
                "Whole grains",
                0.75,
                "kg",
                saved.getPurchaseDate(),
                saved.getExpiryDate()
        );

        assertTrue(service.update(FIRST_USER, updated));
        assertEquals(List.of(updated), service.findByUserId(FIRST_USER));
        assertTrue(service.delete(FIRST_USER, saved.getId()));
        assertFalse(service.delete(FIRST_USER, saved.getId()));
        assertTrue(service.findByUserId(FIRST_USER).isEmpty());
    }

    @Test
    void rejectsInvalidIdentifiersAndExistingIdsOnAdd() throws SQLException {
        GroceryItem saved = service.add(
                FIRST_USER,
                "Bread",
                "Bakery",
                1,
                "loaf",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 15)
        );

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.findByUserId(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.add((GroceryItem) null)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.add(saved)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.update(FIRST_USER, new GroceryItem(
                                FIRST_USER,
                                "Bread",
                                "Bakery",
                                1,
                                "loaf",
                                LocalDate.of(2026, 9, 10),
                                LocalDate.of(2026, 9, 15)
                        ))),
                () -> assertThrows(IllegalArgumentException.class, () -> service.delete(FIRST_USER, 0)),
                () -> assertThrows(SQLException.class, () -> service.add(
                        9999,
                        "Milk",
                        "Dairy",
                        1,
                        "L",
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 15)
                ))
        );
    }

    @Test
    void rejectsMalformedDatesReadFromTheDatabase() throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO groceries
                         (user_id, name, category, quantity, unit, purchase_date, expiry_date)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setLong(1, FIRST_USER);
            statement.setString(2, "Milk");
            statement.setString(3, "Dairy");
            statement.setDouble(4, 1);
            statement.setString(5, "L");
            statement.setString(6, "not-a-date");
            statement.setString(7, "2026-09-15");
            statement.executeUpdate();
        }

        assertThrows(SQLException.class, () -> service.findByUserId(FIRST_USER));
    }

    private void createUser(long id, String email) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (userId, password, email) VALUES (?, ?, ?)"
             )) {
            statement.setLong(1, id);
            statement.setString(2, "test-only-password-hash");
            statement.setString(3, email);
            statement.executeUpdate();
        }
    }

    private String storedDate(long itemId, String column) throws SQLException {
        if (!column.equals("purchase_date") && !column.equals("expiry_date")) {
            throw new IllegalArgumentException("unsupported date column");
        }
        String sql = "SELECT " + column + " FROM groceries WHERE id = ?";
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, itemId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("missing grocery test row " + itemId);
                }
                return resultSet.getString(1);
            }
        }
    }
}
