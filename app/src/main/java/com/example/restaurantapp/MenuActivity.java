package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private List<menuitem> menuItemList;
    private List<AdminMenuItem> allMenuItems;
    private DatabaseReference menuRef;
    private TabLayout tabLayoutCategories;

    private final TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (tab != null) {
                filterMenuByCategory(tab.getPosition());
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            if (tab != null) {
                filterMenuByCategory(tab.getPosition());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        recyclerView = findViewById(R.id.recyclerViewMenu);
        tabLayoutCategories = findViewById(R.id.tabLayoutCategories);
        menuItemList = new ArrayList<>();
        allMenuItems = new ArrayList<>();
        menuRef = FirebaseDatabase.getInstance().getReference("MenuItems");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Order & Navigation buttons
        View.OnClickListener orderClickListener = v -> {
            Intent intent = new Intent(MenuActivity.this, OrderActivity.class);
            startActivity(intent);
        };

        Button btnOrder = findViewById(R.id.btnOrder);
        if (btnOrder != null) btnOrder.setOnClickListener(orderClickListener);

        Button btnNavOrder = findViewById(R.id.btnNavOrder);
        if (btnNavOrder != null) btnNavOrder.setOnClickListener(orderClickListener);

        Button btnNavMenu = findViewById(R.id.btnNavMenu);
        if (btnNavMenu != null) {
            btnNavMenu.setOnClickListener(v -> {
                // Already on MenuActivity
            });
        }

        // Load menu items from Firebase
        loadMenuItems();
    }

    private void loadMenuItems() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allMenuItems.clear();
                menuItemList.clear();

                // Collect all unique categories
                java.util.Set<String> categories = new java.util.LinkedHashSet<>();
                categories.add("All");

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        AdminMenuItem item = data.getValue(AdminMenuItem.class);
                        if (item != null) {
                            allMenuItems.add(item);
                            if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                                categories.add(item.getCategory().trim());
                            }
                        }
                    }
                }

                // Setup tabs safely without triggering listener prematurely
                tabLayoutCategories.clearOnTabSelectedListeners();
                tabLayoutCategories.removeAllTabs();
                for (String category : categories) {
                    tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(category));
                }

                // Show all items initially
                for (AdminMenuItem item : allMenuItems) {
                    menuitem menuItem = new menuitem(
                            item.getName(),
                            item.getPrice(),
                            item.getCategory(),
                            0
                    );
                    menuItemList.add(menuItem);
                }

                if (menuAdapter == null) {
                    menuAdapter = new MenuAdapter(MenuActivity.this, menuItemList, R.layout.menu_item_layout);
                    recyclerView.setAdapter(menuAdapter);
                } else {
                    menuAdapter.notifyDataSetChanged();
                }

                // Re-attach tab listener after adapter is ready
                tabLayoutCategories.addOnTabSelectedListener(tabSelectedListener);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterMenuByCategory(int position) {
        if (tabLayoutCategories == null) return;
        TabLayout.Tab tab = tabLayoutCategories.getTabAt(position);
        if (tab == null || tab.getText() == null) return;

        String selectedCategory = tab.getText().toString();

        menuItemList.clear();

        if (selectedCategory.equalsIgnoreCase("All")) {
            for (AdminMenuItem item : allMenuItems) {
                menuitem menuItem = new menuitem(
                        item.getName(),
                        item.getPrice(),
                        item.getCategory(),
                        0
                );
                menuItemList.add(menuItem);
            }
        } else {
            for (AdminMenuItem item : allMenuItems) {
                if (item.getCategory() != null && item.getCategory().trim().equalsIgnoreCase(selectedCategory.trim())) {
                    menuitem menuItem = new menuitem(
                            item.getName(),
                            item.getPrice(),
                            item.getCategory(),
                            0
                    );
                    menuItemList.add(menuItem);
                }
            }
        }

        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }
}
