package com.example.lifestylelevellingasianlifestyle;

import org.mindrot.jbcrypt.BCrypt;//hashing
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class AuthenticationService {
    public boolean signUp(String email, String password) {

        //hashing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users (password, email) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            //email in use
            if (e.getMessage().contains("UNIQUE constraint failed: users.email")){
                //UI for email in use
            }else{
                e.printStackTrace();
            }

            return false;
        }
    }

    public boolean login(String email, String password) {

        String sql = "SELECT password FROM users WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            var result = stmt.executeQuery();

            if (result.next()) {
                String storedHash = result.getString("password");

                return BCrypt.checkpw(password, storedHash);
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
