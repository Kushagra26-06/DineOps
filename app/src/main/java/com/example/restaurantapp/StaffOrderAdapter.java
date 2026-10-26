package com.example.restaurantapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private DatabaseReference ordersRef;

    public StaffOrderAdapter(Context context, List<Order> orderList, DatabaseReference ordersRef) {
        this.context = context;
        this.orderList = orderList;
        this.ordersRef = ordersRef;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Set order details
        holder.tvDishName.setText(order.getDishName());
        holder.tvCustomerName.setText("Customer: " + order.getUserName());
        holder.tvTableNumber.setText("Table: " + order.getTableNumber());
        holder.tvQuantity.setText("Qty: " + order.getNumberOfPlates());
        holder.tvPrice.setText("Rs " + order.getTotalPrice());

        // Set status badge
        String status = order.getStatus();
        if (status == null) {
            status = "Pending";
        }
        holder.tvStatus.setText(status);

        // Set status badge color
        switch (status) {
            case "Pending":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F97316")); // Orange
                break;
            case "Preparing":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#2563EB")); // Blue
                break;
            case "Ready":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#16A34A")); // Green
                break;
            case "Delivered":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#64748B")); // Gray
                break;
            default:
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F97316")); // Default Orange
        }

        // Set button text and click listener based on current status
        final String orderId = order.getOrderId();

        switch (status) {
            case "Pending":
                holder.btnPreparing.setText("Preparing");
                holder.btnPreparing.setOnClickListener(v -> updateOrderStatus(orderId, "Preparing"));
                break;
            case "Preparing":
                holder.btnPreparing.setText("Ready");
                holder.btnPreparing.setOnClickListener(v -> updateOrderStatus(orderId, "Ready"));
                break;
            case "Ready":
                holder.btnPreparing.setText("Delivered");
                holder.btnPreparing.setOnClickListener(v -> updateOrderStatus(orderId, "Delivered"));
                break;
            case "Delivered":
                holder.btnPreparing.setText("Completed");
                holder.btnPreparing.setOnClickListener(v -> updateOrderStatus(orderId, "Completed"));
                break;
            default:
                holder.btnPreparing.setText("Preparing");
                holder.btnPreparing.setOnClickListener(v -> updateOrderStatus(orderId, "Preparing"));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        ordersRef.child(orderId).child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> {
                // Status updated successfully
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("StaffOrderAdapter", "Failed to update status: " + e.getMessage());
            });
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDishName, tvCustomerName, tvTableNumber, tvQuantity, tvPrice, tvStatus;
        Button btnPreparing;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnPreparing = itemView.findViewById(R.id.btnPreparing);
        }
    }
}
