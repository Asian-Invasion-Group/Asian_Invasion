package com.example.lifestylelevellingasianlifestyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:sqlite:Lifestyle-levelling-Asian-lifestyle/data/users.db";//directory of database

    //database connection
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
