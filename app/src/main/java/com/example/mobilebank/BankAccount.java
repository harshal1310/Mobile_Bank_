package com.example.mobilebank;

public class BankAccount {
    private String bankName;
    private String accountNumber;
    private double balance;
    private String logoResId;
    private String DateTime;
    private double transactionAmount;
    private double avlBal;

    public double getAvlBal() {
        return avlBal;
    }

    public void setAvlBal(double avlBal) {
        this.avlBal = avlBal;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getSmsMsg() {
        return smsMsg;
    }

    public void setSmsMsg(String smsMsg) {
        this.smsMsg = smsMsg;
    }

    private String transactionType;
    private String smsMsg;
    //public void insertRecords(String sender, String accountNumber, Double transactionAmount, String transactionType, String smsDate, String smsMsg, Double avlBal) {

    public BankAccount() {
    }

    public BankAccount(String bankName, String accountNumber, double balance, String logoResId) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.logoResId = logoResId;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }



    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(String logoResId) {
        this.logoResId = logoResId;
    }

    public double getBalance() {
        return balance;
    }

}
