package com.example.lifestylelevellingasianlifestyle;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A class that handles authentication such as signing up as a new user or logging in as an existing user.
 */
public class AuthenticationService {
    /**
     * Method to sign up as a new user
     * @param email the email of user
     * @param password the chosen password of user
     * @return true or false based on whether sign up was successful
     */
    public boolean signUp(String email, String password) {

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO users (password, email) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            //Email in use
            if (e.getMessage().contains("UNIQUE constraint failed: users.email")){
                //ADD POPUP NOTIFICATION
            }else{
                e.printStackTrace();
            }

            return false;
        }
    }

    /**
     * Method to log in as an existing user
     * @param email the email of the user
     * @param password the chosen password of the user
     * @return the userId from the database of the current logged in user
     */
    public int login(String email, String password) {

        String sql = "SELECT userId, password FROM users WHERE email = ?";

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            var result = stmt.executeQuery();

            if (result.next()) {
                int userId = result.getInt("userID");
                String storedHash = result.getString("password");

                if (BCrypt.checkpw(password, storedHash)){
                    return userId;
                }
            }

            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
