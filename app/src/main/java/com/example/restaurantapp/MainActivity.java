package com.example.restaurantapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handler to delay the transition to LoginActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity after 3 seconds
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close the splash screen activity
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}
