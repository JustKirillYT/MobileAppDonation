package com.example.projectlol;

public class Transaction {

    private String senderName;
    private double amount;
    private String message;
    private String status;

    // Конструктор
    public Transaction(String senderName, double amount, String message, String status) {
        this.senderName = senderName;
        this.amount = amount;
        this.message = message;
        this.status = status;
    }

    // Геттеры
    public String getSenderName() {
        return senderName;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}