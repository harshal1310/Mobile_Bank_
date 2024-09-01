package com.example.mobilebank;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
public class BankDataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bankAccounts.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "Accounts";
    public static final String COLUMN_ID = "ID";
    public static final String BANKNAME = "BankName";
    public static final String ACCOUNTNUMBER = "Account_Number";
    public static final String TOTALBALANCE = "Balance";
    public static final String AVLBALANCE = "AVLBalance";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BANKNAME + " TEXT, " +
                    ACCOUNTNUMBER + " TEXT, " +
                    TOTALBALANCE + " DOUBLE, " +
                    AVLBALANCE + " DOUBLE);";


    public BankDataBaseHelper(Context context) {
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
    public void addAccount(String BankName,   String accountNumber,Double totalBalance,Double avlBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BANKNAME, BankName);
        values.put(ACCOUNTNUMBER, accountNumber);
        values.put(TOTALBALANCE, totalBalance);
        values.put(AVLBALANCE,avlBalance);

        db.insert(TABLE_NAME, null, values);

        db.close();
    }
}
