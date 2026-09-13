package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides database interactions with grocery items
 * Handles retrieving, adding and deleting items
 * Specific to the current logged in user
 */
public class ItemService {

    public List<ExistingGrocery> getItemsForCurrentUser() {

        List<ExistingGrocery> groceries = new ArrayList<>();

        String sql = """
        SELECT itemId, itemName, userId, expiryDate, dateAdded
        FROM Groceries
        WHERE userId = ?
        """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Get the ID of the currently logged-in user
            int userId = Session.getUserId();

            stmt.setInt(1, userId);

            ResultSet result = stmt.executeQuery();

            while (result.next()) {

                int itemId = result.getInt("itemId");
                String itemName = result.getString("itemName");
                int itemUserId = result.getInt("userId");
                String expiryDate = result.getString("expiryDate");
                String dateAdded = result.getString("dateAdded");

                ExistingGrocery grocery = new ExistingGrocery(
                        itemId,
                        itemName,
                        itemUserId,
                        expiryDate,
                        dateAdded
                );

                groceries.add(grocery);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groceries;
    }

    /**
     * Adds an item to the SQL database
     * @param itemName name of grocery item
     * @param expiryDate expiry date that the user input
     * @param dateAdded date that the user added the iem
     * @return true or false based on whether added successfully
     */
    public boolean addItem(String itemName, String expiryDate, String dateAdded) {

        String getIdSql = """
        SELECT COALESCE(MAX(itemId), 0) + 1
        FROM Groceries
        """;

        String insertSql = """ 
        INSERT INTO Groceries (itemId, itemName, userId, expiryDate, dateAdded) 
        VALUES (?, ?, ?, ?, ?) 
        """;

        try (Connection conn = Database.connect()) {

            // Get the ID of the currently logged-in user
            int userId = Session.getUserId();

            int nextItemId;

            try(PreparedStatement idStatement =
                        conn.prepareStatement(getIdSql);
                ResultSet result = idStatement.executeQuery()){
                result.next();
                nextItemId = result.getInt(1);
            }

            try(PreparedStatement stmt =
                    conn.prepareStatement(insertSql)){
                stmt.setInt(1, nextItemId);
                stmt.setString(2, itemName);
                stmt.setInt(3, userId);
                stmt.setString(4, expiryDate);
                stmt.setString(5, dateAdded);

                stmt.executeUpdate();

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes an item from the SQL database
     * @param itemId id of the item being deleted
     * @return true or false based on whether done successfully
     */
    public boolean deleteItem(int itemId) {

        String sql = """
            DELETE FROM Groceries
            WHERE itemId = ?
            AND userId = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int userId = Session.getUserId();

            stmt.setInt(1, itemId);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
