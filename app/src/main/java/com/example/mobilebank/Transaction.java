package com.example.mobilebank;

public class Transaction {
    private String transactionMsg;
    private String transactionDate;
    private double transactionAmount;
    private String Bank;
    private String transactionType;
    private String acoountNumber;
    private String smsMsg;
    private String logoResId;
    private boolean isIncome;
    private String transactionName;


    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }
    public void setTransactioName(String transactionName) {
        this.transactionName = transactionName;
    }


    public String getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(String logoResId) {
        this.logoResId = logoResId;
    }

    public String getSmsMsg() {
        return smsMsg;
    }

    public void setSmsMsg(String smsMsg) {
        this.smsMsg = smsMsg;
    }

    public String getTransactionMsg() {
        return transactionMsg;
    }

    public String getAcoountNumber() {
        return acoountNumber;
    }

    public void setAcoountNumber(String acoountNumber) {
        this.acoountNumber = acoountNumber;
    }

    public void setTransactionMsg(String transactionMsg) {
        this.transactionMsg = transactionMsg;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Transaction(String bank, String accountNumber, String transactionType, String transactionMsg, String transactionDate, double transactionamount,String logoResId) {
        this.transactionMsg = transactionMsg;
        this.transactionDate = transactionDate;
        this.transactionAmount  = transactionamount;
        this.Bank = bank;
        this.transactionType = transactionType;
        this.acoountNumber = accountNumber;
        this.smsMsg = transactionMsg;
        this.logoResId = logoResId;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionMsg) {
        this.transactionMsg = transactionMsg;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getAmount() {
        return transactionAmount;
    }

    public void setAmount(double amount) {
        this.transactionAmount  = amount;
    }

    public String getBank() {
        return Bank;
    }

    public void setBank(String bank) {
        Bank = bank;
    }
}
