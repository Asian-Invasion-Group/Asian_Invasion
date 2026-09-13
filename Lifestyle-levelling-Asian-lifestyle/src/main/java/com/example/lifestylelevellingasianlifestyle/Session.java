package com.example.lifestylelevellingasianlifestyle;

/**
 * Handles storing, clearing and retrieving ID of current user
 */
public class Session {
    private static int userId;

    /**
     * Clears the current stored ID of the user when signing out
     */
    public static void clearId() {
        userId = -1;
    }

    public static void setUserId(int Id) {
        userId = Id;
    }

    public static int getUserId() {
        return userId;
    }
}