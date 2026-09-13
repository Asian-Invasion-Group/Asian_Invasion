package com.example.lifestylelevellingasianlifestyle;

/**
 * Grocery item that is stored in the database
 * Stores item ID, name, associated userID, expiry date and date added
 */
public class ExistingGrocery {

    private int itemID;
    private String itemName;
    private int userID;
    private String expiryDate;
    private String dateAdded;

    /**
     * Constructs an Existing Grocery with specified details
     * @param itemID unique ID of grocery item (within database)
     * @param itemName name of grocery item
     * @param userID the ID of the user who added the item
     * @param expiryDate the expiry date of item
     * @param dateAdded date that the item was added
     */
    public ExistingGrocery(int itemID, String itemName, int userID,
                           String expiryDate, String dateAdded) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.userID = userID;
        this.expiryDate = expiryDate;
        this.dateAdded = dateAdded;
    }


    public int getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public int getUserID() {
        return userID;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getDateAdded() {
        return dateAdded;
    }
}
