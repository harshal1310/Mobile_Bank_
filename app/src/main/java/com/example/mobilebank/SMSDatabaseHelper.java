package com.example.mobilebank;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


public class SMSDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sms_transactions.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "transactions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "bank_name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";
    public static final String COLUMN_ACCOUNT_NUMBER = "account_number"; // New column
    public static final String COLUMN_AVL_BALANCE = "avl_balance";
    public static final String COLUMN_TOTAL_BALANCE = "total_balance";
    public static final String COLUMN_MSG = "SMS_MSG";
    public static final String COLUMN_TIMESTAMP = "datetime"; // Updated to datetime    private static final String TABLE_CREATE =
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_AMOUNT + " DOUBLE, " +
                    COLUMN_TRANSACTION_TYPE + " TEXT, " +
                    COLUMN_TOTAL_BALANCE + " DOUBLE, " +
                    COLUMN_AVL_BALANCE + " DOUBLE, " +
                    COLUMN_MSG + " TEXT, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + // Updated to DATETIME
                    COLUMN_ACCOUNT_NUMBER + " TEXT);";

    public SMSDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addTransaction(String BankName,String Msg, Double amount, String transactionType, String accountNumber,Double totalBalance,Double avlBalance,String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, BankName);
        values.put(COLUMN_MSG,Msg);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_TYPE, transactionType);
        values.put(COLUMN_ACCOUNT_NUMBER, accountNumber);
        values.put(COLUMN_TOTAL_BALANCE, totalBalance);
        values.put(COLUMN_AVL_BALANCE,avlBalance);
        values.put(COLUMN_TIMESTAMP,date);

        db.insert(TABLE_NAME, null, values);

        db.close();
    }
    public int updateTransaction(long id,
                                 Double totalBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TOTAL_BALANCE, totalBalance);
       // values.put(COLUMN_TIMESTAMP,datetime);

        // Updating row
        int rowsAffected = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected; // Returns the number of rows affected
    }
}
