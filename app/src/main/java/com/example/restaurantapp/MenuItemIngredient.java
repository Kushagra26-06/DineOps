package com.example.restaurantapp;

public class MenuItemIngredient {
    private String inventoryItemId;
    private double quantity;
    private String menuName; // For display purposes

    public MenuItemIngredient() {
        // Default constructor for Firebase
    }

    public MenuItemIngredient(String inventoryItemId, double quantity) {
        this.inventoryItemId = inventoryItemId;
        this.quantity = quantity;
    }

    public String getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(String inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
}
