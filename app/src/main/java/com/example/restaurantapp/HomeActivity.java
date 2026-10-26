package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvUserName = findViewById(R.id.tvUserName);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnOrder = findViewById(R.id.btnOrder);
        Button btnNavMenu = findViewById(R.id.btnNavMenu);
        Button btnNavOrder = findViewById(R.id.btnNavOrder);

        if (tvUserName != null) {
            tvUserName.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
                startActivity(intent);
            });
        }

        View.OnClickListener menuListener = v -> {
            Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
            startActivity(intent);
        };

        View.OnClickListener orderListener = v -> {
            Intent intent = new Intent(HomeActivity.this, OrderActivity.class);
            startActivity(intent);
        };

        if (btnMenu != null) btnMenu.setOnClickListener(menuListener);
        if (btnNavMenu != null) btnNavMenu.setOnClickListener(menuListener);
        if (btnOrder != null) btnOrder.setOnClickListener(orderListener);
        if (btnNavOrder != null) btnNavOrder.setOnClickListener(orderListener);
    }
}
