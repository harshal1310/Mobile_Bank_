package com.example.mobilebank;

public class Transaction {
    private String transactionName;
    private String transactionDate;
    private double amount;
    private String Bank;

    public Transaction(String transactionName, String transactionDate, double amount, String bank) {
        this.transactionName = transactionName;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.Bank = bank;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getBank() {
        return Bank;
    }

    public void setBank(String bank) {
        Bank = bank;
    }
}
