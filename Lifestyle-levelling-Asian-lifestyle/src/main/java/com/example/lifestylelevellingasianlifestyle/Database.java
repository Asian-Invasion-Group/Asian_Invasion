package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles connection between program and SQLite Database file
 */
public class Database {
//    private static final String URL = "jdbc:sqlite:Lifestyle-levelling-Asian-lifestyle/data/users.db";//directory of database
//    private static final String URL = "jdbc:sqlite:data/users.db";


    // gets the reletive path based off the current directory
    private static final String URL =
            "jdbc:sqlite:" + getDatabasePath();

    private static String getDatabasePath() {
        String projectPath = System.getProperty("user.dir");

        java.io.File database = new java.io.File(
                projectPath, "data/users.db"
        );

        if (!database.exists()) {
            database = new java.io.File(
                    projectPath,
                    "Lifestyle-levelling-Asian-lifestyle/data/users.db"
            );
        }

        return database.getAbsolutePath();
    }

    /**
     * Connects with the database
     * @return connection to database
     * @throws SQLException if connection cannot be established
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
