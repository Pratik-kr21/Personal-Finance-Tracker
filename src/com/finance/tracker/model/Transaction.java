package com.finance.tracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String id;
    private TransactionType type;
    private String category;
    private double amount;
    private LocalDate date;
    private String paymentMethod;
    private String note;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Transaction() {
    }

    public Transaction(String id, TransactionType type, String category, double amount, LocalDate date, String paymentMethod, String note) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }

    public Transaction(TransactionType type, String category, double amount, LocalDate date, String paymentMethod, String note) {
        this(null, type, category, amount, date, paymentMethod, note);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}

