package com.example.restaurantapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StaffActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffOrderAdapter orderAdapter;
    private List<Order> orderList;
    private DatabaseReference ordersRef;

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        // Initialize Firebase Database reference
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOrders);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize order list
        orderList = new ArrayList<>();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new StaffOrderAdapter(this, orderList, ordersRef);
        recyclerView.setAdapter(orderAdapter);

        // Logout button click listener
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(StaffActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Load orders from Firebase
        loadOrders();
    }

    private void loadOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            // Set order ID from snapshot key
                            order.setOrderId(snapshot.getKey());

                            // Only add orders that are not "Completed"
                            if (!"Completed".equals(order.getStatus())) {
                                orderList.add(order);
                            }
                        }
                    } catch (Exception e) {
                        // Handle malformed data
                        android.util.Log.e("StaffActivity", "Error parsing order: " + e.getMessage());
                    }
                }

                // Update UI
                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StaffActivity.this, "Failed to load orders: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
}
