package com.example.projectlol;

public class userModel {
    private String userId;
    private String name;
    private double balance;

    public userModel() {}

    public userModel(String userId, String name, double balance) {
        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }

    // Геттеры
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
}