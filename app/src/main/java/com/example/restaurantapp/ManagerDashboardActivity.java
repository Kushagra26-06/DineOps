package com.example.restaurantapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private DatabaseReference menuRef, inventoryRef;
    private List<AdminMenuItem> menuItems;
    private List<InventoryItem> inventoryList;
    private MenuItemsAdapter menuAdapter;
    private InventoryAdapter inventoryAdapter;

    // Menu form
    private EditText etAdminItemName, etAdminItemPrice, etAdminItemCategory;
    private Button btnAddMenuItem;

    // Inventory Ingredient form
    private EditText etInvName, etInvCategory, etInvQuantity, etInvUnit, etInvThreshold;
    private Button btnAddInventoryItem;

    // Menu-to-Ingredient Mapping form
    private EditText etMappingMenuItem, etMappingInventoryItem, etMappingQuantity;
    private Button btnAddMapping;

    // Analytics & Header views
    private Button btnAdminLogout, btnCustomerView, btnStaffView;
    private TextView tvAnalyticsTotalOrders, tvAnalyticsRevenue, tvAnalyticsMenuItems;
    private TextView tvLowStockAlert;
    private MaterialCardView cardLowStockAlert;

    private RecyclerView rvInventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        menuRef = FirebaseDatabase.getInstance().getReference("MenuItems");
        inventoryRef = FirebaseDatabase.getInstance().getReference("Inventory");

        menuItems = new ArrayList<>();
        inventoryList = new ArrayList<>();

        // Initialize Menu views
        etAdminItemName = findViewById(R.id.etMenuName);
        etAdminItemPrice = findViewById(R.id.etMenuPrice);
        etAdminItemCategory = findViewById(R.id.etMenuCategory);
        btnAddMenuItem = findViewById(R.id.btnAddMenuItem);

        // Initialize Inventory Ingredient views
        etInvName = findViewById(R.id.etInvName);
        etInvCategory = findViewById(R.id.etInvCategory);
        etInvQuantity = findViewById(R.id.etInvQuantity);
        etInvUnit = findViewById(R.id.etInvUnit);
        etInvThreshold = findViewById(R.id.etInvThreshold);
        btnAddInventoryItem = findViewById(R.id.btnAddInventoryItem);

        // Initialize Mapping views
        etMappingMenuItem = findViewById(R.id.etMappingMenuItem);
        etMappingInventoryItem = findViewById(R.id.etMappingInventoryItem);
        etMappingQuantity = findViewById(R.id.etMappingQuantity);
        btnAddMapping = findViewById(R.id.btnAddMapping);

        // Header & Analytics
        btnAdminLogout = findViewById(R.id.btnLogout);
        btnCustomerView = findViewById(R.id.btnCustomerView);
        btnStaffView = findViewById(R.id.btnStaffView);
        tvAnalyticsTotalOrders = findViewById(R.id.tvActiveOrders);
        tvAnalyticsRevenue = findViewById(R.id.tvRevenue);
        tvAnalyticsMenuItems = findViewById(R.id.tvMenuItems);
        tvLowStockAlert = findViewById(R.id.tvLowStockAlert);
        cardLowStockAlert = findViewById(R.id.cardLowStockAlert);

        rvInventory = findViewById(R.id.rvInventory);

        // Setup RecyclerViews
        RecyclerView rvAdminMenuItems = findViewById(R.id.rvMenuItems);
        rvAdminMenuItems.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuItemsAdapter();
        rvAdminMenuItems.setAdapter(menuAdapter);

        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        inventoryAdapter = new InventoryAdapter();
        rvInventory.setAdapter(inventoryAdapter);

        // Load data
        loadMenuItems();
        loadInventory();
        loadAnalytics();

        // Button Click Listeners
        btnAddMenuItem.setOnClickListener(v -> addMenuItem());
        btnAddInventoryItem.setOnClickListener(v -> addInventoryItem());
        btnAddMapping.setOnClickListener(v -> addIngredientMapping());

        // Logout
        btnAdminLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ManagerDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ManagerDashboardActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Customer View
        btnCustomerView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Staff View
        btnStaffView.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboardActivity.this, StaffActivity.class);
            startActivity(intent);
        });
    }

    private void loadMenuItems() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuItems.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    AdminMenuItem item = data.getValue(AdminMenuItem.class);
                    if (item != null) {
                        menuItems.add(item);
                    }
                }
                menuAdapter.notifyDataSetChanged();
                tvAnalyticsMenuItems.setText(String.valueOf(menuItems.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInventory() {
        inventoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                inventoryList.clear();
                int lowStockCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    InventoryItem item = data.getValue(InventoryItem.class);
                    if (item != null) {
                        inventoryList.add(item);
                        if (item.isLowStock()) {
                            lowStockCount++;
                        }
                    }
                }
                inventoryAdapter.notifyDataSetChanged();
                if (cardLowStockAlert != null) {
                    cardLowStockAlert.setCardBackgroundColor(lowStockCount > 0 ? 0xFFEF4444 : 0xFF16A34A);
                }
                tvLowStockAlert.setText(lowStockCount > 0 ? (lowStockCount == 1 ? "1 item low on stock" : lowStockCount + " items low on stock") : "All stock levels OK");
                tvLowStockAlert.setTextColor(0xFFFFFFFF);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAnalytics() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalOrders = 0;
                double totalRevenue = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    totalOrders++;
                    Order order = data.getValue(Order.class);
                    if (order != null && order.getTotalPrice() != null) {
                        String priceStr = order.getTotalPrice().replace("Rs ", "").trim();
                        try {
                            totalRevenue += Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            // Skip invalid prices
                        }
                    }
                }
                tvAnalyticsTotalOrders.setText(String.valueOf(totalOrders));
                tvAnalyticsRevenue.setText("Rs " + (long) totalRevenue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMenuItem() {
        String name = etAdminItemName.getText().toString().trim();
        String price = etAdminItemPrice.getText().toString().trim();
        String category = etAdminItemCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = menuRef.push().getKey();
        if (itemId == null) return;

        AdminMenuItem menuItem = new AdminMenuItem(itemId, name, price, category);
        menuRef.child(itemId).setValue(menuItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ManagerDashboardActivity.this, "Menu item added successfully!", Toast.LENGTH_SHORT).show();
                etAdminItemName.setText("");
                etAdminItemPrice.setText("");
                etAdminItemCategory.setText("");
            } else {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to add menu item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addInventoryItem() {
        String name = etInvName.getText().toString().trim();
        String category = etInvCategory.getText().toString().trim();
        String quantityStr = etInvQuantity.getText().toString().trim();
        String unit = etInvUnit.getText().toString().trim();
        String thresholdStr = etInvThreshold.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) ||
                TextUtils.isEmpty(quantityStr) || TextUtils.isEmpty(unit) || TextUtils.isEmpty(thresholdStr)) {
            Toast.makeText(this, "Please fill all ingredient fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        int threshold;
        try {
            quantity = Integer.parseInt(quantityStr);
            threshold = Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity and threshold must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = inventoryRef.push().getKey();
        if (itemId == null) {
            Toast.makeText(this, "Failed to generate ingredient ID", Toast.LENGTH_SHORT).show();
            return;
        }

        InventoryItem item = new InventoryItem(itemId, name, category, quantity, unit, threshold);
        inventoryRef.child(itemId).setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ManagerDashboardActivity.this, "Ingredient added to inventory!", Toast.LENGTH_SHORT).show();
                etInvName.setText("");
                etInvCategory.setText("");
                etInvQuantity.setText("");
                etInvUnit.setText("");
                etInvThreshold.setText("");
            } else {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to add ingredient", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addIngredientMapping() {
        String menuItemName = etMappingMenuItem.getText().toString().trim();
        String invItemName = etMappingInventoryItem.getText().toString().trim();
        String quantityStr = etMappingQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(menuItemName) || TextUtils.isEmpty(invItemName) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Please fill all mapping fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double qtyPerServing;
        try {
            qtyPerServing = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find Menu Item
        AdminMenuItem targetMenuItem = null;
        for (AdminMenuItem item : menuItems) {
            if (item.getName() != null && item.getName().equalsIgnoreCase(menuItemName)) {
                targetMenuItem = item;
                break;
            }
        }

        if (targetMenuItem == null) {
            Toast.makeText(this, "Menu item '" + menuItemName + "' not found. Check spelling or add it first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Find Inventory Item
        InventoryItem targetInvItem = null;
        for (InventoryItem inv : inventoryList) {
            if (inv.getName() != null && inv.getName().equalsIgnoreCase(invItemName)) {
                targetInvItem = inv;
                break;
            }
        }

        if (targetInvItem == null) {
            Toast.makeText(this, "Ingredient '" + invItemName + "' not found in inventory. Add it to stock first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Add ingredient mapping
        if (targetMenuItem.getIngredients() == null) {
            targetMenuItem.setIngredients(new ArrayList<>());
        }

        MenuItemIngredient ingredientMapping = new MenuItemIngredient(targetInvItem.getItemId(), qtyPerServing);
        ingredientMapping.setMenuName(targetMenuItem.getName());

        List<MenuItemIngredient> existingList = targetMenuItem.getIngredients();
        boolean updated = false;
        for (int i = 0; i < existingList.size(); i++) {
            if (targetInvItem.getItemId().equals(existingList.get(i).getInventoryItemId())) {
                existingList.set(i, ingredientMapping);
                updated = true;
                break;
            }
        }
        if (!updated) {
            existingList.add(ingredientMapping);
        }

        menuRef.child(targetMenuItem.getItemId()).setValue(targetMenuItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ManagerDashboardActivity.this, "Ingredient mapping saved successfully!", Toast.LENGTH_SHORT).show();
                etMappingMenuItem.setText("");
                etMappingInventoryItem.setText("");
                etMappingQuantity.setText("");
            } else {
                Toast.makeText(ManagerDashboardActivity.this, "Failed to save mapping", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // RecyclerView Adapter for Menu Items
    private class MenuItemsAdapter extends RecyclerView.Adapter<MenuItemsAdapter.MenuItemViewHolder> {

        @NonNull
        @Override
        public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_menu, parent, false);
            return new MenuItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
            final AdminMenuItem item = menuItems.get(position);
            holder.tvName.setText(item.getName());
            holder.tvPrice.setText("Rs " + item.getPrice());
            holder.tvCategory.setText(item.getCategory());

            holder.btnDelete.setOnClickListener(v -> {
                menuRef.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManagerDashboardActivity.this, "Item removed from menu", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return menuItems.size();
        }

        class MenuItemViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice, tvCategory;
            Button btnDelete;

            MenuItemViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvAdminItemName);
                tvPrice = itemView.findViewById(R.id.tvAdminItemPrice);
                tvCategory = itemView.findViewById(R.id.tvAdminItemCategory);
                btnDelete = itemView.findViewById(R.id.btnDeleteAdminItem);
            }
        }
    }

    // RecyclerView Adapter for Inventory
    private class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

        @NonNull
        @Override
        public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
            return new InventoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
            final InventoryItem item = inventoryList.get(position);
            holder.tvName.setText(item.getName());
            holder.tvCategory.setText(item.getCategory());
            holder.tvQuantity.setText(item.getQuantity() + " " + item.getUnit());
            holder.tvThreshold.setText("Min: " + item.getMinThreshold());

            if (item.isLowStock()) {
                holder.itemView.setBackgroundColor(0xFFFEE2E2);
            } else {
                holder.itemView.setBackgroundColor(0xFFFFFFFF);
            }

            holder.btnUpdate.setOnClickListener(v -> showUpdateStockDialog(item));
            holder.btnDelete.setOnClickListener(v -> {
                inventoryRef.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManagerDashboardActivity.this, "Inventory item deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return inventoryList.size();
        }

        class InventoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvQuantity, tvThreshold;
            Button btnUpdate, btnDelete;

            InventoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvInventoryName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvThreshold = itemView.findViewById(R.id.tvMinThreshold);
                btnUpdate = itemView.findViewById(R.id.btnUpdate);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void showUpdateStockDialog(InventoryItem item) {
        final InventoryItem finalItem = item;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Stock");

        EditText etNewQuantity = new EditText(this);
        etNewQuantity.setText(String.valueOf(item.getQuantity()));
        etNewQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(etNewQuantity);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String quantityStr = etNewQuantity.getText().toString().trim();
            if (!TextUtils.isEmpty(quantityStr)) {
                int newQuantity = Integer.parseInt(quantityStr);
                finalItem.setQuantity(newQuantity);
                finalItem.setLastUpdated(System.currentTimeMillis());
                inventoryRef.child(finalItem.getItemId()).setValue(finalItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ManagerDashboardActivity.this, "Stock updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
