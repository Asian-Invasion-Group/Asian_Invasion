package com.example.lifestylelevellingasianlifestyle;

import org.mindrot.jbcrypt.BCrypt;//hashing
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;


public class AuthenticationService {
    public boolean signUp(String email, String password) {

        return signUpWithResult(email, password) == SignUpResult.SUCCESS;
    }

    public SignUpResult signUpWithResult(String email, String password) {

        if (email == null || password == null || password.length() < 8) {
            return SignUpResult.INVALID_INPUT;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return SignUpResult.INVALID_INPUT;
        }

        //hashing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users (password, email) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, normalizedEmail);

            stmt.executeUpdate();

            return SignUpResult.SUCCESS;

        } catch (SQLException e) {
            // SQLite extended codes vary, but the primary constraint code is stable.
            if (e.getErrorCode() == 19) {
                return SignUpResult.DUPLICATE_EMAIL;
            }
            System.err.println("Account registration storage error: " + e.getClass().getSimpleName());
            return SignUpResult.STORAGE_ERROR;
        }
    }

    public boolean login(String email, String password) {

        return authenticate(email, password).isPresent();
    }

    public Optional<UserSession> authenticate(String email, String password) {

        if (email == null || password == null || email.isBlank() || password.isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT userId, password, email FROM users WHERE lower(email) = lower(?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());

            var result = stmt.executeQuery();

            if (result.next()) {
                String storedHash = result.getString("password");

                if (BCrypt.checkpw(password, storedHash)) {
                    return Optional.of(new UserSession(result.getInt("userId"), result.getString("email")));
                }
            }

            return Optional.empty();

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Authentication failed because local storage could not be read.");
            return Optional.empty();
        }
    }

    public record UserSession(int userId, String email) {
    }

    public enum SignUpResult {
        SUCCESS,
        DUPLICATE_EMAIL,
        INVALID_INPUT,
        STORAGE_ERROR
    }
}
