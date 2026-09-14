package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElectricityServiceTest {
    private static final long USER_ONE = 1L;
    private static final long USER_TWO = 2L;

    @TempDir
    Path temporaryDirectory;

    private Path databaseFile;
    private ElectricityService service;

    @BeforeEach
    void setUp() throws SQLException {
        databaseFile = temporaryDirectory.resolve("electricity-test.db");
        service = new ElectricityService(this::openConnection);

        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE users (userId INTEGER PRIMARY KEY)");
            statement.executeUpdate("INSERT INTO users (userId) VALUES (1), (2)");
            statement.executeUpdate("""
                    CREATE TABLE electricity_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        billing_month TEXT NOT NULL,
                        kwh REAL NOT NULL CHECK (kwh > 0),
                        bill_amount REAL NOT NULL CHECK (bill_amount >= 0),
                        UNIQUE (user_id, billing_month),
                        FOREIGN KEY (user_id) REFERENCES users(userId) ON DELETE CASCADE
                    )
                    """);
        }
    }

    @Test
    void addQueryAndUpsertAreOrderedAndUserIsolated() throws SQLException {
        service.add(USER_ONE, month(3), decimal("90"), decimal("30"));
        ElectricityRecord january = service.add(USER_ONE, month(1), decimal("110"), decimal("40"));
        service.add(USER_ONE, month(2), decimal("100"), decimal("35"));
        service.add(USER_TWO, month(1), decimal("999"), decimal("200"));

        List<ElectricityRecord> records = service.findByUserId(USER_ONE);
        assertEquals(List.of(month(1), month(2), month(3)),
                records.stream().map(ElectricityRecord::getBillingMonth).toList());

        ElectricityRecord replaced = service.upsert(
                USER_ONE, month(1), decimal("80"), decimal("25")
        );
        assertEquals(january.getId(), replaced.getId());
        assertDecimalEquals("80", replaced.getKwh());
        assertDecimalEquals("999", service.findByUserId(USER_TWO).get(0).getKwh());

        assertThrows(SQLException.class,
                () -> service.add(USER_ONE, month(1), decimal("70"), decimal("20")));
    }

    @Test
    void updateAndDeleteByIdRequireTheOwningUser() throws SQLException {
        ElectricityRecord record = service.add(USER_ONE, month(4), decimal("150"), decimal("60"));

        assertFalse(service.update(
                USER_TWO, record.getId(), month(5), decimal("140"), decimal("55")
        ));
        assertTrue(service.findByMonth(USER_ONE, month(4)).isPresent());

        assertTrue(service.update(
                USER_ONE, record.getId(), month(5), decimal("140"), decimal("55")
        ));
        assertFalse(service.findByMonth(USER_ONE, month(4)).isPresent());
        assertDecimalEquals("140", service.findByMonth(USER_ONE, month(5)).orElseThrow().getKwh());

        assertFalse(service.delete(USER_TWO, record.getId()));
        assertTrue(service.delete(USER_ONE, record.getId()));
        assertTrue(service.findByUserId(USER_ONE).isEmpty());
    }

    @Test
    void updateAndDeleteByMonthRequireTheOwningUser() throws SQLException {
        service.add(USER_ONE, month(6), decimal("120"), decimal("48"));
        service.add(USER_TWO, month(6), decimal("220"), decimal("88"));

        assertFalse(service.update(USER_TWO, month(7), decimal("1"), BigDecimal.ZERO));
        assertTrue(service.update(USER_ONE, month(6), decimal("100"), decimal("42")));
        assertDecimalEquals("100", service.findByMonth(USER_ONE, month(6)).orElseThrow().getKwh());
        assertDecimalEquals("220", service.findByMonth(USER_TWO, month(6)).orElseThrow().getKwh());

        assertTrue(service.delete(USER_ONE, month(6)));
        assertFalse(service.delete(USER_ONE, month(6)));
        assertTrue(service.findByMonth(USER_TWO, month(6)).isPresent());
    }

    @Test
    void insightsAndInputValidationHaveClearBoundaries() throws SQLException {
        service.add(USER_ONE, month(1), decimal("100"), decimal("40"));
        service.add(USER_ONE, month(2), decimal("120"), decimal("45"));

        ElectricityInsights insights = service.insightsFor(USER_ONE);
        assertDecimalEquals("20", insights.getLatestMonthChangePercent().orElseThrow());
        assertDecimalEquals("110", insights.getAverageKwh());
        assertDecimalEquals("108", insights.getNextMonthTargetKwh());

        assertThrows(IllegalArgumentException.class,
                () -> service.add(USER_ONE, month(3), BigDecimal.ZERO, decimal("1")));
        assertThrows(IllegalArgumentException.class,
                () -> service.add(USER_ONE, month(3), decimal("1"), decimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> service.findByUserId(0));
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.toAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private static YearMonth month(int month) {
        return YearMonth.of(2026, month);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private static void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
