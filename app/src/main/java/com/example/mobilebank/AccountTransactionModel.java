package com.example.mobilebank;

public class AccountTransactionModel {
    private String name;
    private String date;
    private String reference;
    private String amount;
    private boolean isIncome;
    private String transactionName;
    private String logoResId;

    public String getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(String logoResId) {
        this.logoResId = logoResId;
    }

    public AccountTransactionModel(String name, String date, String amount, boolean isIncome) {
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    // Getters
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getReference() { return reference; }
    public String getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
}
