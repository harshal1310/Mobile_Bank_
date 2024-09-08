package com.example.mobilebank;

public class SMSModel {
    private String sender,message,datetime,accountnumber,transactionType,transactionName;
    private long timeStamp;
    private Double avlBal,transactionamount;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public SMSModel(String sender, String message, long date) {
        this.sender = sender;
        this.message = message;
        this.timeStamp = date;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getAvlBal() {
        return avlBal;
    }

    public void setAvlBal(Double avlBal) {
        this.avlBal = avlBal;
    }

    public Double getTransactionamount() {
        return transactionamount;
    }

    public void setTransactionamount(Double transactionamount) {
        this.transactionamount = transactionamount;
    }

    public String getMessage() {
        return message;
    }

    public SMSModel() {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return timeStamp;
    }

    public void setDate(long date) {
        this.timeStamp = date;
    }
}
