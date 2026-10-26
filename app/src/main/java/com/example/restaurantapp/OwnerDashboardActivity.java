package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OwnerDashboardActivity extends AppCompatActivity {

    // Firebase
    private DatabaseReference ordersReference;
    private DatabaseReference menuItemsReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // UI Components for Key Metrics
    private TextView tvTotalOrders;
    private TextView tvTotalRevenue;
    private TextView tvTotalCustomers;
    private TextView tvAverageOrderValue;
    private TextView tvTopCategory;

    // Bar Chart for Popular Dishes by Category
    private BarChart barChart;

    // Buttons
    private Button btnManagerView;
    private Button btnLogout;

    // Peak Hours TextViews
    private TextView tvPeakHour1, tvPeakHour1Orders;
    private TextView tvPeakHour2, tvPeakHour2Orders;
    private TextView tvPeakHour3, tvPeakHour3Orders;

    // Data storage
    private int totalOrders = 0;
    private double totalRevenue = 0.0;
    private int totalCustomers = 0;
    private double averageOrderValue = 0.0;
    private String topCategory = "N/A";

    // Category sales data
    private Map<String, Integer> categorySales = new HashMap<>();

    // Hourly order data for peak hours analysis
    private Map<Integer, Integer> hourlyOrders = new HashMap<>();

    // Menu items cache for category lookup
    private Map<String, String> menuItemCategories = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ordersReference = FirebaseDatabase.getInstance().getReference("Orders");
        menuItemsReference = FirebaseDatabase.getInstance().getReference("MenuItems");

        // Initialize UI Components
        initializeViews();

        // Initialize category maps
        initializeCategoryMaps();

        // Setup Bar Chart
        setupBarChart();

        // Load menu items first, then fetch orders
        loadMenuItems();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalCustomers = findViewById(R.id.tvTotalCustomers);
        tvAverageOrderValue = findViewById(R.id.tvAvgOrderValue);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        barChart = findViewById(R.id.barChartSales);
        tvPeakHour1 = findViewById(R.id.tvPeakHour1);
        tvPeakHour1Orders = findViewById(R.id.tvPeakHour1Orders);
        tvPeakHour2 = findViewById(R.id.tvPeakHour2);
        tvPeakHour2Orders = findViewById(R.id.tvPeakHour2Orders);
        tvPeakHour3 = findViewById(R.id.tvPeakHour3);
        tvPeakHour3Orders = findViewById(R.id.tvPeakHour3Orders);
        btnManagerView = findViewById(R.id.btnManagerView);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void initializeCategoryMaps() {
        // Initialize hourly orders (0-23 hours)
        for (int i = 0; i < 24; i++) {
            hourlyOrders.put(i, 0);
        }
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawValueAboveBar(true);
    }

    private void loadMenuItems() {
        menuItemsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                menuItemCategories.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AdminMenuItem item = snapshot.getValue(AdminMenuItem.class);
                        if (item != null && item.getName() != null && item.getCategory() != null) {
                            menuItemCategories.put(item.getName().trim(), item.getCategory().trim());
                        }
                    }
                }
                // After loading menu items, fetch orders
                fetchOrdersData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Even if menu items fail, still try to fetch orders
                fetchOrdersData();
            }
        });
    }

    private void fetchOrdersData() {
        ordersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Reset counters
                totalOrders = 0;
                totalRevenue = 0.0;
                totalCustomers = 0;
                averageOrderValue = 0.0;
                topCategory = "N/A";

                // Initialize category sales for all known categories
                categorySales.clear();
                for (String cat : menuItemCategories.values()) {
                    if (cat != null && !cat.trim().isEmpty()) {
                        categorySales.put(cat.trim(), 0);
                    }
                }

                // Reset hourly data
                for (int i = 0; i < 24; i++) {
                    hourlyOrders.put(i, 0);
                }

                // Track unique customers
                Map<String, Boolean> uniqueCustomers = new HashMap<>();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        // Process each order
                        totalOrders++;

                        // Get order total
                        String totalPrice = orderSnapshot.child("totalPrice").getValue(String.class);
                        if (totalPrice != null) {
                            try {
                                String priceStr = totalPrice.replace("Rs ", "").replace("₹", "").trim();
                                totalRevenue += Double.parseDouble(priceStr);
                            } catch (NumberFormatException e) {
                                // Skip invalid prices
                            }
                        }

                        // Track unique customers by phone number
                        String phoneNumber = orderSnapshot.child("phoneNumber").getValue(String.class);
                        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                            uniqueCustomers.put(phoneNumber.trim(), true);
                        }

                        // Get order timestamp for peak hours analysis
                        Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);
                        if (timestamp != null) {
                            int hour = getHourFromTimestamp(timestamp);
                            Integer current = hourlyOrders.get(hour);
                            hourlyOrders.put(hour, (current != null ? current : 0) + 1);
                        }

                        // Get dish name and parse items & categories
                        String dishName = orderSnapshot.child("dishName").getValue(String.class);
                        if (dishName != null) {
                            parseOrderDishes(dishName);
                        }
                    }

                    // Calculate metrics
                    totalCustomers = uniqueCustomers.size();
                    if (totalOrders > 0) {
                        averageOrderValue = totalRevenue / totalOrders;
                    }

                    // Find top category by sales count
                    int maxSales = 0;
                    for (Map.Entry<String, Integer> entry : categorySales.entrySet()) {
                        if (entry.getValue() > maxSales) {
                            maxSales = entry.getValue();
                            topCategory = entry.getKey();
                        }
                    }

                    // Update UI
                    updateMetricsDisplay();
                    updateBarChart();
                    updatePeakHoursDisplay();
                } else {
                    // No orders data
                    updateMetricsDisplay();
                    updateBarChart();
                    updatePeakHoursDisplay();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OwnerDashboardActivity.this,
                    "Error fetching data: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseOrderDishes(String dishNameStr) {
        if (dishNameStr == null || dishNameStr.trim().isEmpty()) {
            return;
        }

        // dishName format: "Crispy Corn (x2), Cold Coffee (x1)" or "Noodles (x1)"
        String[] items = dishNameStr.split(",");
        for (String rawItem : items) {
            String item = rawItem.trim();
            if (item.isEmpty()) continue;

            String name = item;
            int qty = 1;

            int openParen = item.lastIndexOf("(x");
            if (openParen == -1) {
                openParen = item.lastIndexOf("(X");
            }

            if (openParen != -1 && item.endsWith(")")) {
                name = item.substring(0, openParen).trim();
                String qtyStr = item.substring(openParen + 2, item.length() - 1).trim();
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ignored) {
                }
            }

            // Find category
            String category = null;
            if (menuItemCategories.containsKey(name)) {
                category = menuItemCategories.get(name);
            } else {
                for (Map.Entry<String, String> entry : menuItemCategories.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(name)) {
                        category = entry.getValue();
                        break;
                    }
                }
            }

            if (category != null && !category.trim().isEmpty()) {
                String catName = category.trim();
                Integer currentQty = categorySales.get(catName);
                categorySales.put(catName, (currentQty != null ? currentQty : 0) + qty);
            }
        }
    }

    private int getHourFromTimestamp(long timestamp) {
        java.util.Date date = new java.util.Date(timestamp);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(java.util.Calendar.HOUR_OF_DAY);
    }


    private void updateMetricsDisplay() {
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvTotalRevenue.setText(String.format("₹%.2f", totalRevenue));
        tvTotalCustomers.setText(String.valueOf(totalCustomers));
        tvAverageOrderValue.setText(String.format("₹%.2f", averageOrderValue));
        tvTopCategory.setText(topCategory);
    }

    private void updateBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>(categorySales.keySet());
        Collections.sort(categories);

        if (categories.isEmpty()) {
            categories.add("Starters");
            categories.add("Beverages");
        }

        int[] palette = {
            0xFF3B82F6, // Blue -> Starters / Cat 1
            0xFF10B981, // Green -> Beverages / Cat 2
            0xFFF59E0B, // Amber
            0xFF8B5CF6, // Purple
            0xFFEC4899, // Pink
            0xFF06B6D4, // Cyan
            0xFFEF4444  // Red
        };

        int[] colors = new int[categories.size()];

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            int sales = categorySales.getOrDefault(category, 0);
            entries.add(new BarEntry(i, sales));
            colors[i] = palette[i % palette.length];
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(0xFF1E293B);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);

        // X-axis setup
        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(categories));
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(categories.size());
        xAxis.setTextColor(0xFF64748B);
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);

        // Left axis setup
        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setTextColor(0xFF64748B);
        leftAxis.setTextSize(11f);
        leftAxis.setAxisMinimum(0f);

        // Right axis setup
        com.github.mikephil.charting.components.YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Legend setup on Top Right
        com.github.mikephil.charting.components.Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setForm(com.github.mikephil.charting.components.Legend.LegendForm.SQUARE);
        legend.setFormSize(10f);
        legend.setTextSize(11f);
        legend.setTextColor(0xFF1E293B);
        legend.setWordWrapEnabled(true);

        com.github.mikephil.charting.components.LegendEntry[] legendEntries =
            new com.github.mikephil.charting.components.LegendEntry[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            com.github.mikephil.charting.components.LegendEntry entry =
                new com.github.mikephil.charting.components.LegendEntry();
            entry.label = categories.get(i);
            entry.formColor = colors[i];
            legendEntries[i] = entry;
        }
        legend.setCustom(legendEntries);

        barChart.notifyDataSetChanged();
        barChart.invalidate();
        barChart.animateY(800);
    }

    private void updatePeakHoursDisplay() {
        // Find peak hours (top 3 hours with most orders)
        ArrayList<Map.Entry<Integer, Integer>> hourlyList = new ArrayList<>(hourlyOrders.entrySet());
        hourlyList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int count = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyList) {
            if (entry.getValue() > 0 && count < 3) {
                int hour = entry.getKey();
                String timePeriod = getTimePeriod(hour);
                if (count == 0) {
                    tvPeakHour1.setText(timePeriod);
                    tvPeakHour1Orders.setText(entry.getValue() + " orders");
                } else if (count == 1) {
                    tvPeakHour2.setText(timePeriod);
                    tvPeakHour2Orders.setText(entry.getValue() + " orders");
                } else if (count == 2) {
                    tvPeakHour3.setText(timePeriod);
                    tvPeakHour3Orders.setText(entry.getValue() + " orders");
                }
                count++;
            }
        }

        if (count == 0) {
            tvPeakHour1.setText("--:--");
            tvPeakHour1Orders.setText("0 orders");
            tvPeakHour2.setText("--:--");
            tvPeakHour2Orders.setText("0 orders");
            tvPeakHour3.setText("--:--");
            tvPeakHour3Orders.setText("0 orders");
        }
    }

    private String getTimePeriod(int hour) {
        if (hour == 0) return "12 AM";
        if (hour < 12) return hour + " AM";
        if (hour == 12) return "12 PM";
        return (hour - 12) + " PM";
    }

    private void setupButtonListeners() {
        // Manager View Button
        btnManagerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OwnerDashboardActivity.this, ManagerDashboardActivity.class);
                startActivity(intent);
            }
        });

        // Logout Button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(OwnerDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadMenuItems();
    }
}
