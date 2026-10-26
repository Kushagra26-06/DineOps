package com.example.restaurantapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    private Context context;
    private List<menuitem> menuList;
    private int layoutResource;  // Dynamic Layout

    public MenuAdapter(Context context, List<menuitem> menuList, int layoutResource) {
        this.context = context;
        this.menuList = menuList;
        this.layoutResource = layoutResource;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        menuitem item = menuList.get(position);

        holder.name.setText(item.getName());
        holder.price.setText("Price: Rs " + item.getPrice());

        // Order Now Button Click Listener
        holder.orderNowButton.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(item.getName(), item.getPrice().trim(), item.getCategory());
            Intent intent = new Intent(v.getContext(), OrderActivity.class);
            v.getContext().startActivity(intent);
        });

        // Add to Cart Button Click Listener
        holder.addToCartButton.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(item.getName(), item.getPrice().trim(), item.getCategory());
            Toast.makeText(v.getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        Button orderNowButton;
        TextView name, price;
        Button addToCartButton;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            orderNowButton = itemView.findViewById(R.id.orderNowButton);
        }
    }
}


