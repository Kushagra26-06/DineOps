package com.example.restaurantapp;

public class menuitem {
    private String name;
    private String price;
    private String category;
    private int imageResource;

    public menuitem(String name, String price, String category, int imageResource) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageResource = imageResource;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getCategory() { return category; }
    public int getImageResource() { return imageResource; }
}
