package com.example.restaurantapp;

public class InventoryItem {
    private String itemId;
    private String name;
    private String category;
    private int quantity;
    private String unit;
    private int minThreshold;
    private long lastUpdated;

    public InventoryItem() {
        // Default constructor for Firebase
    }

    public InventoryItem(String itemId, String name, String category, int quantity, String unit, int minThreshold) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.minThreshold = minThreshold;
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }
}
