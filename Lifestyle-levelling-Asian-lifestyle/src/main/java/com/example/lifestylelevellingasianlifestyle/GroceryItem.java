package com.example.lifestylelevellingasianlifestyle;

/**
 * Represents a grocery item inside the temporary grocery list\
 * Stores the item's name and quantity
 * Allows user to add directly from this list to database
 */
public class GroceryItem {

    private String itemName;
    private int quantity;

    /**
     * Constructs grocery item with specified details
     * @param itemName name of item
     * @param quantity quantity to get
     */
    public GroceryItem(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }
}
