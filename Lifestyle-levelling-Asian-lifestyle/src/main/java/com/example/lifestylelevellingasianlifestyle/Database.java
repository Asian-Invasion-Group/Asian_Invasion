package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles connection between program and SQLite Database file
 */
public class Database {
    private static final String URL = "jdbc:sqlite:Lifestyle-levelling-Asian-lifestyle/data/users.db";//directory of database

    /**
     * Connects with the database
     * @return connection to database
     * @throws SQLException if connection cannot be established
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
