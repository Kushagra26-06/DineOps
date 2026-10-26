package com.example.restaurantapp;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private static InventoryManager instance;
    private final DatabaseReference menuRef;
    private final DatabaseReference inventoryRef;

    private InventoryManager() {
        menuRef = FirebaseDatabase.getInstance().getReference("MenuItems");
        inventoryRef = FirebaseDatabase.getInstance().getReference("Inventory");
    }

    public static synchronized InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    // Deduct inventory when an order is placed
    public void deductInventoryForOrder(List<CartItem> cartItems, InventoryDeductionCallback callback) {
        // First, get all menu items to find their ingredients
        menuRef.get().addOnSuccessListener(menuSnapshot -> {
            Map<String, List<MenuItemIngredient>> menuIngredientsMap = new HashMap<>();
            
            for (DataSnapshot itemSnapshot : menuSnapshot.getChildren()) {
                AdminMenuItem menuItem = itemSnapshot.getValue(AdminMenuItem.class);
                if (menuItem != null && menuItem.getIngredients() != null) {
                    menuIngredientsMap.put(menuItem.getName(), menuItem.getIngredients());
                }
            }
            
            // Now get current inventory
            inventoryRef.get().addOnSuccessListener(inventorySnapshot -> {
                Map<String, InventoryItem> inventoryMap = new HashMap<>();
                Map<String, Double> deductions = new HashMap<>();
                
                for (DataSnapshot invSnapshot : inventorySnapshot.getChildren()) {
                    InventoryItem item = invSnapshot.getValue(InventoryItem.class);
                    if (item != null) {
                        inventoryMap.put(item.getItemId(), item);
                        deductions.put(item.getItemId(), 0.0);
                    }
                }
                
                // Calculate total deductions for each cart item
                for (CartItem cartItem : cartItems) {
                    List<MenuItemIngredient> ingredients = menuIngredientsMap.get(cartItem.getName());
                    if (ingredients != null) {
                        for (MenuItemIngredient ingredient : ingredients) {
                            double totalDeduction = ingredient.getQuantity() * cartItem.getQuantity();
                            String invItemId = ingredient.getInventoryItemId();
                            if (invItemId != null && deductions.containsKey(invItemId)) {
                                Double current = deductions.get(invItemId);
                                deductions.put(invItemId, (current != null ? current : 0.0) + totalDeduction);
                            }
                        }
                    }
                }
                
                // Apply deductions to inventory
                boolean[] allSuccess = {true};
                StringBuilder errorMessages = new StringBuilder();
                
                for (Map.Entry<String, Double> entry : deductions.entrySet()) {
                    String itemId = entry.getKey();
                    double deduction = entry.getValue();
                    
                    if (deduction > 0) {
                        InventoryItem item = inventoryMap.get(itemId);
                        if (item != null) {
                            double newQuantity = item.getQuantity() - deduction;
                            if (newQuantity >= 0) {
                                item.setQuantity((int) newQuantity);
                                item.setLastUpdated(System.currentTimeMillis());
                                inventoryRef.child(itemId).setValue(item)
                                    .addOnSuccessListener(aVoid -> Log.d("InventoryManager", "Deducted " + deduction + " from " + item.getName()))
                                    .addOnFailureListener(e -> {
                                        Log.e("InventoryManager", "Failed to update " + item.getName(), e);
                                        allSuccess[0] = false;
                                        errorMessages.append(item.getName()).append(", ");
                                    });
                            } else {
                                // Insufficient stock
                                allSuccess[0] = false;
                                errorMessages.append(item.getName()).append(" (insufficient), ");
                            }
                        }
                    }
                }
                
                if (callback != null) {
                    callback.onDeductionComplete(allSuccess[0], errorMessages.toString());
                }
            }).addOnFailureListener(e -> {
                Log.e("InventoryManager", "Failed to load inventory", e);
                if (callback != null) {
                    callback.onDeductionComplete(false, "Failed to load inventory");
                }
            });
        }).addOnFailureListener(e -> {
            Log.e("InventoryManager", "Failed to load menu items", e);
            if (callback != null) {
                callback.onDeductionComplete(false, "Failed to load menu items");
            }
        });
    }

    public interface InventoryDeductionCallback {
        void onDeductionComplete(boolean success, String errorMessage);
    }
}
