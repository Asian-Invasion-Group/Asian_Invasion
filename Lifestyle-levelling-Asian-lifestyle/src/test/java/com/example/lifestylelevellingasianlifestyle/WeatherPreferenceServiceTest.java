package com.example.lifestylelevellingasianlifestyle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherPreferenceServiceTest {
    @TempDir
    Path tempDirectory;

    private String previousDatabasePath;
    private WeatherPreferenceService service;

    @BeforeEach
    void setUp() throws Exception {
        previousDatabasePath = System.getProperty(Database.DATABASE_PATH_PROPERTY);
        System.setProperty(Database.DATABASE_PATH_PROPERTY, tempDirectory.resolve("preference-test.db").toString());
        Database.initialize();
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (userId, password, email) VALUES (?, ?, ?)")) {
            statement.setLong(1, 1);
            statement.setString(2, "test-hash");
            statement.setString(3, "person@example.com");
            statement.executeUpdate();
        }
        service = new WeatherPreferenceService();
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
    void savesAndUpdatesOneLocationPerUser() throws Exception {
        assertTrue(service.findForUser(1).isEmpty());

        WeatherService.Location brisbane = new WeatherService.Location(
                "Brisbane, Queensland, Australia", -27.47, 153.03, "Australia/Brisbane");
        service.save(1, brisbane);
        assertEquals(brisbane, service.findForUser(1).orElseThrow());

        WeatherService.Location hobart = new WeatherService.Location(
                "Hobart, Tasmania, Australia", -42.88, 147.33, "Australia/Hobart");
        service.save(1, hobart);
        assertEquals(hobart, service.findForUser(1).orElseThrow());
    }

    @Test
    void validatesOwnershipAndHonoursForeignKeys() {
        WeatherService.Location location = new WeatherService.Location(
                "Brisbane", -27.47, 153.03, "Australia/Brisbane");
        assertThrows(IllegalArgumentException.class, () -> service.findForUser(0));
        assertThrows(IllegalArgumentException.class, () -> service.save(1, null));
        assertThrows(Exception.class, () -> service.save(999, location));
    }
}
