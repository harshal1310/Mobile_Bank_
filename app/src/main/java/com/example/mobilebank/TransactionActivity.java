package com.example.mobilebank;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.logger.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TransactionActivity extends AppCompatActivity {
    private TextView textView, accountNumberTextView, bankNameTextView, totalAmountTextView;
    private SMSDatabaseHelper dbHelper;
    private BankDataBaseHelper accountdbHelper;
    private ClipboardManager clipboardManager;
    private ClipData clipData;
    // Define regex patterns
    String bankPattern = "(?i)(SBI|ICICI|HDFC|AXIS|KOTAK|PNB|BOB|CANARA|IDBI|YES BANK|RBL|INDUSIND|DBS|HSBC|CITI|BOI|IDFC|UCO|UJJIVAN)";
    //      String transactionCrdeitorPattern = "(?i)(credit(ed)?|sent?|withdraw?)";
    //   String  transactionDebitorPattern = "(?i)(debit(ed)?|deposit(ed)?)";

    String transactionDebitorPattern = "(?i)(debit(ed)?|sent?|withdraw?)";

    String transactionCrdeitorPattern = "(?i)(credit(ed)?|deposit(ed)?)";


    //     String amountPattern = "(?i)(?:INR|RS)\\s*([\\d,]+(?:\\.\\d+)?)";

    String amountPattern = "(?i)(INR|Rs\\.?)(\\s*[\\d,]+(?:\\.\\d+)?)";


    String accountPattern = "(?i)(?:account\\s*(?:no\\s*)?|A\\/C\\s*|acc(?:ount)?\\s*no\\s*|acc\\s*no\\s*|acct\\s*)\\D*(\\d{4,})\\b";

    //  String cardPattern = "(?i)(?:card\\s*no\\s*|card\\s*)\\D*(\\d{4,})\\b";
    String cardPattern = "(?i)(?:card\\s*(?:number|no|ending)?\\s*(?:with)?\\s*)[Xx]*\\d+";

    String balancePattern = "(?i)(?:Avl|Balance)\\s*(?:Bal(?:ance)?)?[:\\-]?\\s*(?:INR|Rs\\.?)?\\s*([\\d,]+(?:\\.\\d{1,2})?)";


    // Compile the patterns
    Pattern bankRegex = Pattern.compile(bankPattern);
    Pattern transactionRegexWithdraw = Pattern.compile(transactionDebitorPattern);
    Pattern transactionRegexCredit = Pattern.compile(transactionCrdeitorPattern);

    Pattern amountRegex = Pattern.compile(amountPattern);
    Pattern accountRegex = Pattern.compile(accountPattern);
    Pattern cardRegex = Pattern.compile(cardPattern);
    Pattern balanceRegex = Pattern.compile(balancePattern);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textView = findViewById(R.id.smstext);
        bankNameTextView = findViewById(R.id.bankNameTextView);
        accountNumberTextView = findViewById(R.id.accountNumberTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        dbHelper = new SMSDatabaseHelper(this);
        accountdbHelper = new BankDataBaseHelper(this);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(TransactionActivity.this, "clicked", Toast.LENGTH_SHORT).show();
              //    buttonForward();
                HashMap<String, String> BankNamesWithAccounts = new HashMap<>();

                List<SMSModel> list = readsms(BankNamesWithAccounts);

                for (SMSModel sms : list) {
                    // textView.append("SMSMSG Added in list:"+sms.getMessage() + sms.getSender() + sms.getDatetime());

                    insertRecords(sms.getSender(), sms.getAccountnumber(),sms.getTransactionamount(),sms.getTransactionType(),sms.getDatetime(),sms.getMessage(),sms.getAvlBal());
                    //    public void insertRecords(String sender, String accountNumber, Double transactionAmount, String transactionType, String smsDate, String smsMsg, Double avlBal) {
                }

               HashMap<String,BankAccount> accounts = getAccountsInfo("", "");


            }
        });

        displayTransactionSummary();
        clipData = ClipData.newPlainText("label", textView.getText().toString());
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clipData);

    }

    private HashMap<String, BankAccount> getRecentTransaction() {
        HashMap<String, BankAccount> recentTransactionsMap = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String recentTransactions = "SELECT " +
                SMSDatabaseHelper.COLUMN_SENDER + ", " +
                SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + ", " +
                SMSDatabaseHelper.COLUMN_AMOUNT + ", " +
                SMSDatabaseHelper.COLUMN_TIMESTAMP +
                " FROM " + SMSDatabaseHelper.TABLE_NAME + // Add table name
                " ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC " + // Sort by timestamp in descending order
                " LIMIT 10"; // Limit to 10 most recent transactions

        try (Cursor bankNameCursor = db.rawQuery(recentTransactions, null)) {
            if (bankNameCursor != null && bankNameCursor.moveToFirst()) {
                do {
                    String bankName = bankNameCursor.getString(0);
                    String accountNumber = bankNameCursor.getString(1);
                    double amount = bankNameCursor.getDouble(2);
                    String timestamp = bankNameCursor.getString(3);

                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setAccountNumber(accountNumber);
                    bankAccount.setDateTime(timestamp);
                    bankAccount.setBankName(bankName);
                    bankAccount.setTransactionAmount(amount);
                    recentTransactionsMap.put(accountNumber, bankAccount);

                } while (bankNameCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TransactionActivity", "Error fetching recent transactions", e);
        } finally {
            db.close(); // Close the database connection
        }
        return recentTransactionsMap;
    }


    private void displayTransactionSummary() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        // Query to get bank name
        String bankNameQuery = "SELECT DISTINCT sender FROM " + SMSDatabaseHelper.TABLE_NAME;
        try (Cursor bankNameCursor = db.rawQuery(bankNameQuery, null)) {
            if (bankNameCursor.moveToFirst()) {
                String bankName = bankNameCursor.getString(0);
                bankNameTextView.setText("Bank Name: " + bankName);
            }
        } catch (Exception e) {
            Log.e("TransactionActivity", "Error fetching bank name", e);
        }

        // Query to get account number

    }


    public void buttonForward() {
      /*  HashMap<String, String> BankNamesWithAccounts = new HashMap<>();
        try {
            // Ensure permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                textView.setText("SMS permission not granted.");
                return;
            }

            // Query SMS inbox
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            SimpleDateFormat comparisonFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Date to compare against (01/08/2024)
            Date cutoffDate = null;
            try {
                cutoffDate = comparisonFormat.parse("13/01/2024");
            } catch (ParseException e) {

            }


            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    //   String formattedDate = dateFormat.format(new Date(timestamp));
                    String formattedDate = dateFormat.format(new Date(timestamp));


                    // Create a Date object from the timestamp
                    Date messageDate = new Date(timestamp);

                    try {
                        dateFormat.parse(formattedDate);
                    } catch (Exception e) {

                    }

                    // Check if the message date is after 01/08/2024
                    if (messageDate.after(cutoffDate)) { //&&(issmsDateGreaterThanLastDate(formattedDate))) {

                        // Check if sender matches known banks
                        Matcher bankMatcher = bankRegex.matcher(message);
                        Matcher transactionMatcherCreditor = transactionRegexCredit.matcher(message);
                        Matcher transactionMatcherDebitor = transactionRegexWithdraw.matcher(message);
                        Matcher cardMatcher = cardRegex.matcher(message);


                        Matcher amountMatcher = amountRegex.matcher(message);
                        Matcher accountMatcher = accountRegex.matcher(message);
                        Matcher avlBalanceMatcher = balanceRegex.matcher(message);
                        String transactionType = "";
                        String accountNumber = "";
                        String amountNumber = "";
                        String bankName = "";
                        if (bankMatcher.find()) {

                            bankName = bankMatcher.group();

                            if (transactionMatcherCreditor.find()) {
                                transactionType = "Credit";
                            } else if (transactionMatcherDebitor.find()) {
                                transactionType = "Debit";
                            }


                            if (!transactionType.isBlank()) {
                                if (cardMatcher.find()) {
                                    if (BankNamesWithAccounts.size() > 0) {//if card
                                        if (BankNamesWithAccounts.containsKey(bankName) && (!message.equalsIgnoreCase("transfer"))) {
                                            accountNumber = BankNamesWithAccounts.get(bankName);


                                        }
                                    }


                                    if (accountNumber.length() > 2) {
                                        accountNumber = accountNumber.substring(accountNumber.length() - 3);
                                    } else {
                                        accountNumber = "";
                                    }
                                    if (amountMatcher.find()) {
                                        amountNumber = amountMatcher.group(2);
                                    }

                                } else if (accountMatcher.find()) {
                                    accountNumber = accountMatcher.group();
                                    if (accountNumber.length() > 2) {
                                        accountNumber = accountNumber.substring(accountNumber.length() - 3);
                                    } else {
                                        accountNumber = "";
                                    }
                                    if (amountMatcher.find()) {
                                        amountNumber = amountMatcher.group(2);
                                    }


                                }


                                if (!transactionType.isBlank() && !accountNumber.isBlank() && !amountNumber.isBlank()) { //&& (!message.contains("transfer"))) {

                                    BankNamesWithAccounts.put(bankName, accountNumber);
                                    //
                                    Double lastAmount = 0.0, transactionAmount = 0.0, avlBal = -1.0;
                                    Integer rowID = -1;
                                    String latestDatetime = "";

                                    amountNumber = amountNumber.replaceAll(",", ""); // Remove commas from the balance


                                    transactionAmount = Double.parseDouble(amountNumber);
                                    transactionAmount = transactionAmount * 1.0;

                                    if (avlBalanceMatcher.find()) {
                                        String balance = avlBalanceMatcher.group(1);
                                        if (balance != null) {
                                            balance = balance.replaceAll(",", ""); // Remove commas from the balance
                                        }
                                        avlBal = Double.parseDouble(balance);
                                        avlBal = avlBal * 1.0;
                                    }


                                    if (transactionType.equalsIgnoreCase("Debit")) {
                                        transactionAmount = transactionAmount * (-1.0);
                                    }


                                    if (isSmsDateGreaterThanLastDate(accountNumber, transactionType, transactionAmount, formattedDate)) {

                                        SQLiteDatabase db = dbHelper.getReadableDatabase();


                                        String avlBalanceQuery = "SELECT " + SMSDatabaseHelper.COLUMN_ID + "," + SMSDatabaseHelper.COLUMN_TOTAL_BALANCE + "," +
                                                SMSDatabaseHelper.COLUMN_TIMESTAMP +
                                                " FROM " + SMSDatabaseHelper.TABLE_NAME +
                                                " WHERE " + SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + " = ? AND " +
                                                SMSDatabaseHelper.COLUMN_AVL_BALANCE + " != -1 " + // Check condition for avl_balance
                                                "ORDER BY " + SMSDatabaseHelper.COLUMN_TIMESTAMP + " DESC LIMIT 1"; // Fetch the latest entry by ID
                                        // Check condition for avl_balance

                                        try (Cursor accountCursor = db.rawQuery(avlBalanceQuery, new String[]{accountNumber})) {
                                            if (accountCursor.moveToFirst()) {
                                                lastAmount = accountCursor.getDouble(1); // Fetch the total balance
                                                rowID = accountCursor.getInt(0);
                                                latestDatetime = accountCursor.getString(2);
                                                //   textView.append("\nReading from DB:\n" + lastAmount + "\nAccount Number: " + accountNumber + "\n");
                                            }
                                        } catch (Exception e) {
                                            Log.e("TransactionActivity", "Error fetching last transaction amount", e);
                                        }


                                        if (rowID > -1) {// IF already avl bal there
                                            if (avlBal > -1) {


                                                dbHelper.addTransaction(sender, message, transactionAmount, transactionType, accountNumber, avlBal, avlBal, formattedDate);

                                                //     textView.append("From: " + sender + "\nMSg:" + message + "\nAccount: " + accountNumber + "\nTransactionAmount: " + amountNumber + "\nTransaction Type: " + transactionType +
                                                //           "\nTotal Bal:" +   avlBal + "\nAVL BAL:" + avlBal + "\nDate: " + formattedDate + "\n\n");


                                            } else {


                                                double TOTALBAL = lastAmount + transactionAmount;


                                                Date lastDate = null;
                                                Date smsDate = null;
                                                try {
                                                    lastDate = dateFormat.parse(latestDatetime);
                                                    smsDate = dateFormat.parse(formattedDate);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }

                                                // Compare dates
                                                if (lastDate != null && smsDate != null) {
                                                    if (smsDate.after(lastDate)) {
                                                        // lastDatetime is greater than smsDatetime
                                                        //   textView.append("\n need to update." + smsDate  + " avlBal " +avlBal + "  lastAmount " + lastAmount  +"\n");
                                                        dbHelper.updateTransaction(rowID, lastAmount + avlBal);
                                                    } else {
                                                        // smsDatetime is greater than or equal to lastDatetime
                                                        //       textView.append("\nNo Need to update.\n");
                                                    }
                                                }


//                                            textView.append("From: " + sender + "\nMSg:" + message + "\nAccount: " + accountNumber + "\nTransactionAmount: " + amountNumber + "\nTransaction Type: " + transactionType + "\nlast Amo:" + lastAmount + "\nTotal Bal:" + TOTALBAL + "\nAVL BAL:" + avlBal + "\nDate: " + formattedDate + "\n\n");

                                                dbHelper.addTransaction(sender, message, transactionAmount, transactionType, accountNumber, TOTALBAL, avlBal, formattedDate);


                                            }

                                        } //no avl  msg yet
                                        else {

                                            String accountNumberQuery = "SELECT " + SMSDatabaseHelper.COLUMN_TOTAL_BALANCE +
                                                    " FROM " + SMSDatabaseHelper.TABLE_NAME + " WHERE " +
                                                    SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + " = ?" +
                                                    "ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC LIMIT 1";

                                            try (Cursor accountCursor = db.rawQuery(accountNumberQuery, new String[]{accountNumber})) {
                                                if (accountCursor.moveToFirst()) {
                                                    //             textView.append("\nreading from db \n"+accountCursor.getDouble(0) + "\n account num:"+accountNumber + "\n ");
                                                    lastAmount = accountCursor.getDouble(0);
                                                    lastAmount = lastAmount * 1.0;

                                                }
                                            } catch (Exception e) {
                                                Log.e("TransactionActivity", "Error fetching last transaction amount", e);
                                            }
                                            double TOTALBAL = lastAmount + transactionAmount;

                                            if (avlBal > -1) {
//                                            textView.append("From: " + sender + "\nMSg:" + message + "\nAccount: " + accountNumber + "\nTransactionAmount: " + amountNumber + "\nTransaction Type: " + transactionType + "\nlast Amo:" + lastAmount + "\nTotal Bal:" + avlBal + lastAmount + "\nAVL BAL:" + avlBal + "\nDate: " + formattedDate + "\n\n");

//                                            textView.append("\nvalue updated:"+String.valueOf(TOTALBAL));
                                                dbHelper.addTransaction(sender, message, transactionAmount, transactionType, accountNumber, avlBal + lastAmount, avlBal, formattedDate);


                                            } else {
                                                //          textView.append("From: " + sender + "\nMSg:" + message + "\nAccount: " + accountNumber + "\nTransactionAmount: " + amountNumber + "\nTransaction Type: " + transactionType + "\nlast Amo:" + lastAmount + "\nTotal Bal:" + TOTALBAL + "\nAVL BAL:" + avlBal + "\nDate: " + formattedDate + "\n\n");

                                                dbHelper.addTransaction(sender, message, transactionAmount, transactionType, accountNumber, TOTALBAL, avlBal, formattedDate);
                                            }

                                        }


                                    }


                                }
                            }

                        }
                    }


                } while (cursor.moveToNext());


                cursor.close();
            } else {
                textView.setText("No SMS messages found.");
            }


        } catch (Exception e) {
            textView.setText("An error occurred: " + e.getMessage());
            Log.e("TransactionActivity", "Error processing SMS", e);
        }*/
    }

    private HashMap<String, BankAccount> getAccountsInfo(String smsDatetime, String lastDateTime) {
        HashMap<String, BankAccount> storeAccountDetails = new HashMap<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //if avl balance not get
    /*    String accountInfoQuery1 = "SELECT DISTINCT " +
                "t.account_number, " +
                "t.datetime AS latest_datetime, " +
                "t.bank_name, " +
                "t.total_balance, " +
                "t.avl_balance " +
                "FROM transactions t " +
                "INNER JOIN ( " +
                "SELECT account_number, MAX(datetime) AS max_datetime " +
                "FROM transactions " +
                "WHERE avl_balance != -1 " +
                "GROUP BY account_number " +
                ") latest ON t.account_number = latest.account_number AND t.datetime = latest.max_datetime " +
                "WHERE t.avl_balance != -1 " +
                "ORDER BY t.datetime DESC;";


        String accountInfoQuery = "SELECT DISTINCT " +
                SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + ", " +
                SMSDatabaseHelper.COLUMN_SENDER + ", " + // Add sender column
                SMSDatabaseHelper.COLUMN_TOTAL_BALANCE + ", " +
                SMSDatabaseHelper.COLUMN_TIMESTAMP + // Include timestamp column
                " FROM " + SMSDatabaseHelper.TABLE_NAME +
                " ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC";//+ // Order by timestamp in descending order//
        //" LIMIT 5"; // Limit the results to 100

        String avlBalanceQuery = "SELECT DISTINCT " +
                SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + ", " +
                SMSDatabaseHelper.COLUMN_SENDER + "," +
                SMSDatabaseHelper.COLUMN_TOTAL_BALANCE + ", " +
                SMSDatabaseHelper.COLUMN_TIMESTAMP + "," +
                SMSDatabaseHelper.COLUMN_AVL_BALANCE +
                " FROM " + SMSDatabaseHelper.TABLE_NAME +
                " WHERE " + SMSDatabaseHelper.COLUMN_AVL_BALANCE + " != -1 " + // Check condition for avl_balance
                " ORDER BY " + SMSDatabaseHelper.COLUMN_TIMESTAMP; // Order by timestamp
        */

        String avlBalanceQuery1 = "SELECT DISTINCT " + SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER +
                ", " + SMSDatabaseHelper.COLUMN_SENDER +
                ", " + SMSDatabaseHelper.COLUMN_TOTAL_BALANCE +
                ", " + SMSDatabaseHelper.COLUMN_TIMESTAMP +
                " FROM " + SMSDatabaseHelper.TABLE_NAME +
                " ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC"; // Fetch the latest entry by ID
        // Check condition for avl_balance

        String accountNumber = "";
        String BankName = "";
        String DateTime = "";
        Double TotalBal = 0.0;
        Double avlBal = 0.0;
        try (Cursor accountNumberCursor = db.rawQuery(avlBalanceQuery1, null)) {
            if (accountNumberCursor != null && accountNumberCursor.moveToFirst()) {
                do {
                    accountNumber = accountNumberCursor.getString(0); // Account Number column
                    DateTime = accountNumberCursor.getString(3);//time
                    BankName = accountNumberCursor.getString(1);

                    TotalBal = accountNumberCursor.getDouble(2);
                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setAccountNumber(accountNumber);
                    bankAccount.setBankName(BankName);
                    bankAccount.setBalance(TotalBal);
                    bankAccount.setDateTime(DateTime);
                    if (storeAccountDetails.containsKey(accountNumber)) {
                     //    textView.append("\nBankAccount:"+bankAccount);
                        textView.append("\nAccount Number: " + accountNumber + "\nBank Name: " + BankName + "\n" + "Balance:" + TotalBal + "\nAvlBal:" + avlBal + "\nDate:" + DateTime);

//
                        String getDateTime = storeAccountDetails.get(accountNumber).getDateTime();

//                        textView.append("\nlastDate:"+getDateTime);

                    } else {
                        storeAccountDetails.put(accountNumber, bankAccount);
                        textView.append("\nfirst time\n");
                        textView.append("\nAccount Number: " + accountNumber + "\nBank Name: " + BankName + "\n" + "Balance:" + TotalBal + "\nAvlBal:" + avlBal + "\nDate:" + DateTime);

                    }


                    //      Toast.makeText(this, Balance + "ACC: "+accountNumber, Toast.LENGTH_SHORT).show();

                    // Display fetched values in the TextView
                    //    textView.append("\nAccount Number: " + accountNumber + "\nBank Name: " + BankName + "\n" + "Balance:" + TotalBal + "\nAvlBal:" + avlBal + "\nDate:" + DateTime );

                } while (accountNumberCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TransactionActivity", "Error fetching account number", e);
        }
        return storeAccountDetails;
    }

    boolean isSmsDateGreaterThanLastDate(String accountNumber, String transactionType, Double transactionAmount, String smsDateTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String latestDatetime = "";

        // Query to fetch the latest transaction entry for the given account number, transaction type, and amount
        String avlBalanceQuery = "SELECT " + SMSDatabaseHelper.COLUMN_TIMESTAMP +
                " FROM " + SMSDatabaseHelper.TABLE_NAME +
                " WHERE " + SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + " = ? AND " +
                SMSDatabaseHelper.COLUMN_TRANSACTION_TYPE + " = ? AND " +
                SMSDatabaseHelper.COLUMN_AMOUNT + " = ? " +
                " ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC LIMIT 1";
        String transactionAmountStr = String.valueOf(transactionAmount);

        try (Cursor accountCursor = db.rawQuery(avlBalanceQuery, new String[]{accountNumber, transactionType, transactionAmountStr})) {
            if (accountCursor.moveToFirst()) {
                // Get the latest timestamp
                latestDatetime = accountCursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("TransactionActivity", "Error fetching last transaction timestamp", e);
        }

        if (latestDatetime.isEmpty()) {
            // No previous transaction found, so the new SMS is considered greater.
            return true;
        }

        Date lastDate = null;
        Date smsDate = null;
        try {
            // Parse the date strings into Date objects
            lastDate = dateFormat.parse(latestDatetime);
            smsDate = dateFormat.parse(smsDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (lastDate != null && smsDate != null) {
            // Check if SMS date is after the last date
            long differenceInMillis = smsDate.getTime() - lastDate.getTime();
            long differenceInMinutes = differenceInMillis / (60 * 1000); // Convert milliseconds to minutes

            // Check if the SMS date is greater than the last date and the interval is more than 55 minutes
            return smsDate.after(lastDate) && differenceInMinutes > 55;
        }

        return false; // If parsing failed or any other issue occurred
    }

    private List<SMSModel> readsms(HashMap<String, String> BankNamesWithAccounts) {
        List<SMSModel> smsList = new ArrayList<>();
        try {
            // Ensure permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                textView.setText("SMS permission not granted.");
                return smsList;
            }

            // Query SMS inbox
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            SimpleDateFormat comparisonFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Date to compare against (01/08/2024)
            Date cutoffDate = comparisonFormat.parse("1/05/2024");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Date messageDate = new Date(timestamp);
                    String formattedDate = dateFormat.format(new Date(timestamp));


                    // Check if the message date is after 01/08/2024
                    if (messageDate.after(cutoffDate)) {
                        SMSModel sms = new SMSModel(sender, message, timestamp);
                        sms.setDatetime(formattedDate);
                        if (extractTransactionSMS(sms, BankNamesWithAccounts)) {
                            smsList.add(sms);
                        }
                    }

                } while (cursor.moveToNext());
                cursor.close();
            } else {
                textView.setText("No SMS messages found.");
            }

        } catch (Exception e) {
            textView.setText("An error occurred: " + e.getMessage());
            Log.e("TransactionActivity", "Error processing SMS", e);
        }

        return smsList.stream()
                .sorted((sms1, sms2) -> Long.compare(sms1.getDate(), sms2.getDate()))
                .collect(Collectors.toList());

}

    private boolean extractTransactionSMS(SMSModel sms, HashMap<String, String> BankNamesWithAccounts) {
        SMSModel validatedSMS = validataTransactionSMS(sms,BankNamesWithAccounts);
        return  validatedSMS!=null ? true : false;

    }

    public void insertRecords(String sender, String accountNumber, Double transactionAmount, String transactionType, String smsDate, String smsMsg, Double avlBal) {


        if (isSmsDateGreaterThanLastDate(accountNumber, transactionType, transactionAmount, smsDate)) {
            textView.append("SMSMSG Added in list:"+smsMsg +  "sender:" + sender + accountNumber + transactionAmount + transactionType + smsDate);


            Double lastAmount = 0.0;
            String latestDatetime = "";

            SQLiteDatabase db = dbHelper.getReadableDatabase();


            String avlBalanceQuery = "SELECT " + SMSDatabaseHelper.COLUMN_ID + "," + SMSDatabaseHelper.COLUMN_TOTAL_BALANCE + "," +
                    SMSDatabaseHelper.COLUMN_TIMESTAMP +
                    " FROM " + SMSDatabaseHelper.TABLE_NAME +
                    " WHERE " + SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + " = ? " +
                    "ORDER BY " + SMSDatabaseHelper.COLUMN_ID + " DESC LIMIT 1"; // Fetch the latest entry by ID
            // Check condition for avl_balance

            try (Cursor accountCursor = db.rawQuery(avlBalanceQuery, new String[]{accountNumber})) {
                if (accountCursor.moveToFirst()) {
                    lastAmount = accountCursor.getDouble(1); // Fetch the total balance

                }
            } catch (Exception e) {
                Log.e("TransactionActivity", "Error fetching last transaction amount", e);
            }
            if (avlBal > -1) {
                //dbHelper.addTransaction();
               // textView.append("\nFrom:"+ sender + "\nsms:"+smsMsg+"\ntransactionAmount:"+transactionAmount + "\ntransactionType:"+transactionType+"\naccounumber:"+accountNumber+"\nablbal:"+avlBal+"\ndate:"+smsDate);
            //    dbHelper.addTransaction(sender, smsMsg, transactionAmount, transactionType, accountNumber, avlBal, avlBal, smsDate);

            } else {
                double TOTALBAL = lastAmount + transactionAmount;
           //     textView.append("\nFrom:"+ sender + "\nsms:"+smsMsg+"\ntransactionAmount:"+transactionAmount + "\ntransactionType:"+transactionType+"\naccounumber:"+accountNumber+"\ntotalbal:"+TOTALBAL+"\ndate:"+smsDate);

              //  dbHelper.addTransaction(sender, smsMsg, transactionAmount, transactionType, accountNumber, TOTALBAL, avlBal, smsDate);
            }

        }
    }

    private SMSModel validataTransactionSMS(SMSModel sms,HashMap<String,String> BankNamesWithAccounts) {

            Matcher bankMatcher = bankRegex.matcher(sms.getMessage());
            Matcher transactionMatcherCreditor = transactionRegexCredit.matcher(sms.getMessage());
            Matcher transactionMatcherDebitor = transactionRegexWithdraw.matcher(sms.getMessage());
            Matcher cardMatcher = cardRegex.matcher(sms.getMessage());


            Matcher amountMatcher = amountRegex.matcher(sms.getMessage());
            Matcher accountMatcher = accountRegex.matcher(sms.getMessage());
            Matcher avlBalanceMatcher = balanceRegex.matcher(sms.getMessage());
            String transactionType = "";
            String accountNumber = "";
            String amountNumber = "";
            String bankName = "";
            if (bankMatcher.find()) {

                bankName = bankMatcher.group();

                if (transactionMatcherCreditor.find()) {
                    transactionType = "Credit";
                } else if (transactionMatcherDebitor.find()) {
                    transactionType = "Debit";
                }



                if (!transactionType.isBlank()) {
                if (cardMatcher.find()) {
                    if (BankNamesWithAccounts.size() > 0) {//if card
                        if (BankNamesWithAccounts.containsKey(bankName)) {//&& (!sms.getMessage().equalsIgnoreCase("transfer"))) {
                            accountNumber = BankNamesWithAccounts.get(bankName);


                        }
                    }


                    if (accountNumber.length() > 2) {
                        accountNumber = accountNumber.substring(accountNumber.length() - 3);
                    } else {
                        accountNumber = "";
                    }
                    if (amountMatcher.find()) {
                        amountNumber = amountMatcher.group(2);
                    }

                } else if (accountMatcher.find()) {
                    accountNumber = accountMatcher.group();
                    if (accountNumber.length() > 2) {
                        accountNumber = accountNumber.substring(accountNumber.length() - 3);
                    } else {
                        accountNumber = "";
                    }
                    if (amountMatcher.find()) {
                        amountNumber = amountMatcher.group(2);
                    }


                }


                if (!transactionType.isBlank() && !accountNumber.isBlank() && !amountNumber.isBlank()) { //&& (!message.contains("transfer"))) {
                   // public void insertRecords(String sender, String accountNumber, Double transactionAmount, String transactionType, String smsDate, String smsMsg, Double avlBal) {
                    Double lastAmount = 0.0, transactionAmount = 0.0, avlBal = -1.0;
                    Integer rowID = -1;
                    String latestDatetime = "";

                    amountNumber = amountNumber.replaceAll(",", ""); // Remove commas from the balance


                    transactionAmount = Double.parseDouble(amountNumber);
                    transactionAmount = transactionAmount * 1.0;
                    if (transactionType.equalsIgnoreCase("Debit")) {
                        transactionAmount = transactionAmount * (-1.0);
                    }

                    if (avlBalanceMatcher.find()) {
                        String balance = avlBalanceMatcher.group(1);
                        if (balance != null) {
                            balance = balance.replaceAll(",", ""); // Remove commas from the balance
                        }
                        avlBal = Double.parseDouble(balance);
                        avlBal = avlBal * 1.0;
                    }

                    sms.setAccountnumber(accountNumber);
                    sms.setTransactionamount(transactionAmount);
                  //  sms.setDatetime(latestDatetime);
                    sms.setTransactionType(transactionType);
                    sms.setAvlBal(avlBal);

                    return sms;
                }
            }
        }


        return null;
    }
}

