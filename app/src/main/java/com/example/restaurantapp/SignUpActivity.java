package com.example.restaurantapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private Spinner spinner;
    private Button buttonSignUp;
    private TextView tvLoginLink;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinner = findViewById(R.id.spinner);
        buttonSignUp = findViewById(R.id.button);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Setup spinner with roles
        String[] userRoles = {"Select Role", "Owner", "Customer", "Staff", "Manager"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userRoles);
        spinner.setAdapter(adapter);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get input values
                String fname = editTextFirstName.getText().toString().trim();
                String lname = editTextLastName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String role = spinner.getSelectedItem().toString().trim();

                // Validate input fields
                if (TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) ||
                        TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                        role.equals("Select Role")) {
                    Toast.makeText(SignUpActivity.this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog
                progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.setMessage("Signing Up...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Create a new user with FirebaseAuth
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                // Sign up succeeded, now store additional user data in Realtime Database
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();
                                    // Create a User object with additional info
                                    User user = new User(fname, lname, email, role);
                                    // Save the user data under "Users/{uid}"
                                    mDatabase.child(uid).setValue(user)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                                    
                                                    // Role-based routing after signup
                                                    Intent intent;
                                                    switch (role) {
                                                        case "Owner":
                                                            intent = new Intent(SignUpActivity.this, OwnerDashboardActivity.class);
                                                            break;
                                                        case "Manager":
                                                            intent = new Intent(SignUpActivity.this, ManagerDashboardActivity.class);
                                                            break;
                                                        case "Staff":
                                                            intent = new Intent(SignUpActivity.this, StaffActivity.class);
                                                            break;
                                                        case "Customer":
                                                            intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                                            break;
                                                        default:
                                                            // Default to HomeActivity if role is unknown
                                                            intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                                            break;
                                                    }
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Failed to store user data", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                // Sign up failed; display error message
                                Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
