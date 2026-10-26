package com.example.restaurantapp;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    // Default constructor required for Firebase calls
    public User() {}

    public User(String firstName, String lastName, String email, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    // Getters and setters (if needed)
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
