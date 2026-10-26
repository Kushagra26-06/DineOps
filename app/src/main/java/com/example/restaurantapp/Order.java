package com.example.restaurantapp;

public class Order {
    private String orderId;
    private String userName;
    private String phoneNumber;
    private String dishName;
    private String tableNumber;
    private String size;
    private String numberOfPlates;
    private String totalPrice;
    private long timestamp;
    private String status;

    public Order() {
        // Default constructor for Firebase
    }

    public Order(String orderId, String userName, String phoneNumber, String dishName, String tableNumber, String size, String numberOfPlates, String totalPrice, long timestamp) {
        this.orderId = orderId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.dishName = dishName;
        this.tableNumber = tableNumber;
        this.size = size;
        this.numberOfPlates = numberOfPlates;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.status = "Pending"; // Default status
    }

    public Order(String orderId, String userName, String phoneNumber, String dishName, String tableNumber, String size, String numberOfPlates, String totalPrice, long timestamp, String status) {
        this.orderId = orderId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.dishName = dishName;
        this.tableNumber = tableNumber;
        this.size = size;
        this.numberOfPlates = numberOfPlates;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getNumberOfPlates() {
        return numberOfPlates;
    }

    public void setNumberOfPlates(String numberOfPlates) {
        this.numberOfPlates = numberOfPlates;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
