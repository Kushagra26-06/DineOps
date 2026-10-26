package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(String name, String price, String category) {
        // Check if item already exists in cart
        for (CartItem item : cartItems) {
            if (item.getName().equals(name)) {
                item.incrementQuantity();
                return;
            }
        }
        // Add new item
        cartItems.add(new CartItem(name, price, category, 1));
    }

    public void addToCart(String name, String price) {
        addToCart(name, price, "");
    }

    public void removeFromCart(int position) {
        cartItems.remove(position);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            try {
                String priceStr = item.getPrice().replace("Rs ", "").trim();
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (NumberFormatException e) {
                // Skip invalid prices
            }
        }
        return total;
    }
}
