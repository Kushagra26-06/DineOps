package com.example.restaurantapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OrderActivity extends AppCompatActivity {
    private EditText etUserName, etPhoneNumber;
    private TextView tvEmptyCart, tvCartTotal;
    private Button btnConfirmOrder;
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        cartManager = CartManager.getInstance();

        etUserName = findViewById(R.id.etUserName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Check if item was added from menu
        Intent intent = getIntent();
        boolean addToCart = intent.getBooleanExtra("addToCart", false);
        String itemName = intent.getStringExtra("itemName");
        String itemPrice = intent.getStringExtra("itemPrice");

        if (addToCart && itemName != null && itemPrice != null) {
            cartManager.addToCart(itemName, itemPrice);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        }

        // Load cart items
        loadCartItems();

        // Confirm Order Button Click
        btnConfirmOrder.setOnClickListener(v -> sendOrderData());

        // Bottom Navigation Buttons
        Button btnNavMenu = findViewById(R.id.btnNavMenu);
        if (btnNavMenu != null) {
            btnNavMenu.setOnClickListener(v -> {
                Intent intentNav = new Intent(OrderActivity.this, MenuActivity.class);
                startActivity(intentNav);
                finish();
            });
        }

        Button btnNavOrder = findViewById(R.id.btnNavOrder);
        if (btnNavOrder != null) {
            btnNavOrder.setOnClickListener(v -> {
                // Already on OrderActivity
            });
        }
    }

    private void loadCartItems() {
        List<CartItem> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            btnConfirmOrder.setEnabled(false);
            btnConfirmOrder.setAlpha(0.5f);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.VISIBLE);
            btnConfirmOrder.setEnabled(true);
            btnConfirmOrder.setAlpha(1.0f);

            cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemClickListener() {
                @Override
                public void onRemoveClick(int position) {
                    cartManager.removeFromCart(position);
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    updateCartTotal();
                    loadCartItems(); // Refresh to check if cart is empty
                }
            });
            recyclerViewCart.setAdapter(cartAdapter);
        }

        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = cartManager.getTotalPrice();
        tvCartTotal.setText("Cart Total: Rs " + (long) total);
    }

    private void sendOrderData() {
        List<CartItem> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = etUserName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (userName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(OrderActivity.this);
        progressDialog.setMessage("Placing Order...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Deduct inventory before placing order
        InventoryManager.getInstance().deductInventoryForOrder(cartItems, (success, errorMessage) -> {
            if (!success) {
                progressDialog.dismiss();
                Toast.makeText(OrderActivity.this, "Inventory update failed: " + errorMessage, Toast.LENGTH_LONG).show();
                return;
            }

            // Continue with order placement
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

            // Create order string with all items
            StringBuilder dishNames = new StringBuilder();
            int totalPlates = 0;
            for (CartItem item : cartItems) {
                if (dishNames.length() > 0) {
                    dishNames.append(", ");
                }
                dishNames.append(item.getName()).append(" (x").append(item.getQuantity()).append(")");
                totalPlates += item.getQuantity();
            }

            double totalPrice = cartManager.getTotalPrice();

            String orderId = ordersRef.push().getKey();
            if (orderId == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to generate order ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Order order = new Order(
                    orderId,
                    userName,
                    phoneNumber,
                    dishNames.toString(),
                    "1",
                    "N/A",
                    String.valueOf(totalPlates),
                    "Rs " + (long) totalPrice,
                    System.currentTimeMillis(),
                    "Pending"
            );

            ordersRef.child(orderId).setValue(order)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(OrderActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                            // Clear cart after successful order
                            cartManager.clearCart();
                            etUserName.setText("");
                            etPhoneNumber.setText("");

                            // Redirect to HomeActivity after successful order placement
                            Intent homeIntent = new Intent(OrderActivity.this, HomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(OrderActivity.this, "Order failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}
