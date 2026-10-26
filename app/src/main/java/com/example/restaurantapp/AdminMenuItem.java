package com.example.restaurantapp;

public class AdminMenuItem {
    private String itemId;
    private String name;
    private String price;
    private String category;
    private java.util.List<MenuItemIngredient> ingredients; // List of ingredients needed

    public AdminMenuItem() {
        // Default constructor for Firebase
    }

    public AdminMenuItem(String itemId, String name, String price, String category) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.category = category;
        this.ingredients = new java.util.ArrayList<>();
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public java.util.List<MenuItemIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(java.util.List<MenuItemIngredient> ingredients) {
        this.ingredients = ingredients;
    }
}
