package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Stores the last resolved weather location for each local account. */
public final class WeatherPreferenceService {
    public Optional<WeatherService.Location> findForUser(long userId) throws SQLException {
        requireUserId(userId);
        String sql = """
                SELECT location_name, latitude, longitude, timezone
                FROM weather_preferences
                WHERE user_id = ?
                """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                try {
                    return Optional.of(new WeatherService.Location(
                            resultSet.getString("location_name"),
                            resultSet.getDouble("latitude"),
                            resultSet.getDouble("longitude"),
                            resultSet.getString("timezone")
                    ));
                } catch (IllegalArgumentException exception) {
                    throw new SQLException("Saved weather location is invalid", exception);
                }
            }
        }
    }

    public void save(long userId, WeatherService.Location location) throws SQLException {
        requireUserId(userId);
        if (location == null) {
            throw new IllegalArgumentException("location is required");
        }
        String sql = """
                INSERT INTO weather_preferences
                    (user_id, location_name, latitude, longitude, timezone)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(user_id) DO UPDATE SET
                    location_name = excluded.location_name,
                    latitude = excluded.latitude,
                    longitude = excluded.longitude,
                    timezone = excluded.timezone
                """;
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, location.displayName());
            statement.setDouble(3, location.latitude());
            statement.setDouble(4, location.longitude());
            statement.setString(5, location.timezone());
            statement.executeUpdate();
        }
    }

    private static void requireUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}
