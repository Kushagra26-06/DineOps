package com.example.restaurantapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView signUpTextView;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.username2);
        etPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.signup_text1);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Redirect to SignUpActivity when sign up text is clicked
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get input values
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    // Show progress dialog
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    // Use Firebase to sign in with email and password
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // Fetch user role from Firebase Realtime Database
                                        mDatabase.child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                progressDialog.dismiss();
                                                String role = dataSnapshot.getValue(String.class);
                                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();

                                                // Role-based routing
                                                Intent intent;
                                                if (role != null) {
                                                    switch (role) {
                                                        case "Owner":
                                                            intent = new Intent(LoginActivity.this, OwnerDashboardActivity.class);
                                                            break;
                                                        case "Manager":
                                                            intent = new Intent(LoginActivity.this, ManagerDashboardActivity.class);
                                                            break;
                                                        case "Staff":
                                                            intent = new Intent(LoginActivity.this, StaffActivity.class);
                                                            break;
                                                        case "Customer":
                                                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            break;
                                                        default:
                                                            // Default to HomeActivity if role is unknown
                                                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            break;
                                                    }
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // If role is not found, default to HomeActivity
                                                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                progressDialog.dismiss();
                                                Toast.makeText(LoginActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
