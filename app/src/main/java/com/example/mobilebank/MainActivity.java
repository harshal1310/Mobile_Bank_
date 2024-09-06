package com.example.mobilebank;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.klinker.android.logger.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private RecyclerView bankRecyclerView,last_transactionView;
    private BankAdapter bankAdapter;
    private TransactionAdapter transactionAdaptar;
    private List<BankAccount> bankAccounts;
    private List<Transaction> lasttransactions;
    private SMSDatabaseHelper dbHelper;


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

    private ProgressBar progressBar;

   // private SMSDatabaseHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "onCreate: Initializing components");
        dbHelper = new SMSDatabaseHelper(this);

        bankRecyclerView = findViewById(R.id.bankRecyclerView);
        last_transactionView = findViewById(R.id.transactionRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        bankAccounts = new ArrayList<>();
        lasttransactions = new ArrayList<>();

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        } else {


            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            boolean isDataLoaded = sharedPreferences.getBoolean("isDataLoaded", false);

            if (!isDataLoaded) {
                // Start AsyncTask to load data in background
                new LoadSMSDataTask().execute();
            } else {
                // Load data from the database if already loaded
                loadDataFromDatabase();
            }



            new LoadSMSDataTask().execute();
        }
        /*

        HashMap<String,Transaction> recentTransactions = getRecentTransaction();
        recentTransactions.forEach((accountNumber,transaction)->{
            lasttransactions.add(new Transaction(transaction.getBank(), accountNumber, transaction.getTransactionType(), transaction.getTransactionMsg(),transaction.getTransactionDate(), transaction.getTransactionAmount()));

        });

        List<String> bankNames = new ArrayList<>();
        for (BankAccount account : bankAccounts) {
            bankNames.add(account.getBankName());
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        }


        HashMap<String, String> BankNamesWithAccounts = new HashMap<>();

        List<SMSModel> list =readsms(BankNamesWithAccounts);

        for (SMSModel sms : list) {
            insertRecords(sms.getSender(), sms.getAccountnumber(),sms.getTransactionamount(),sms.getTransactionType(),sms.getDatetime(),sms.getMessage(),sms.getAvlBal());
            //    public void insertRecords(String sender, String accountNumber, Double transactionAmount, String transactionType, String smsDate, String smsMsg, Double avlBal) {
        }

        HashMap<String,BankAccount> accounts = getAccountsInfo("", "");
      //  for(Map.entry<>:account.getE)
            accounts.forEach((accountNumber, bankAccount) -> {
                // Perform your desired operations with accountNumber and bankAccount
              //  System.out.println("Account Number: " + accountNumber);
                //System.out.println("Bank Name: " + bankAccount.getBankName());
               // System.out.println("Balance: " + bankAccount.getBalance());
            //    bankAccount.getDateTime();
                bankAccounts.add(new BankAccount(bankAccount.getBankName(), accountNumber, bankAccount.getBalance(), "axis"));

                // Add more operations as needed
            });

        bankAdapter = new BankAdapter(bankAccounts, this);
        transactionAdaptar = new TransactionAdapter(lasttransactions,this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);  // 2 columns
        bankRecyclerView.setLayoutManager(gridLayoutManager);
        bankRecyclerView.setAdapter(bankAdapter);

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        last_transactionView.setLayoutManager(linearLayoutManager);
        last_transactionView.setAdapter(transactionAdaptar);


        // Request SMS permission if not granted


     //   new Handler().postDelayed(() -> {
       //     Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
         //   startActivity(intent);
           // finish(); // Optionally finish MainActivity so that it won't be in the back stack
        //}, 2000); // 2000 milliseconds = 2 seconds

         */
    }


    private class LoadSMSDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress bar before starting background task
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Load SMS data in background thread
            HashMap<String, String> BankNamesWithAccounts = new HashMap<>();
            List<SMSModel> list = readsms(BankNamesWithAccounts);

            for (SMSModel sms : list) {
                insertRecords(sms.getSender(), sms.getAccountnumber(), sms.getTransactionamount(),
                        sms.getTransactionType(), sms.getDatetime(), sms.getMessage(), sms.getAvlBal());
            }

            HashMap<String, BankAccount> accounts = getAccountsInfo("", "");
            accounts.forEach((accountNumber, bankAccount) -> {
                bankAccounts.add(new BankAccount(bankAccount.getBankName(), accountNumber, bankAccount.getBalance(), "axis"));
            });

            List<Transaction> recentTransactions = getRecentTransaction();
            recentTransactions.forEach((transaction) -> {
                lasttransactions.add(new Transaction(transaction.getBank(), transaction.getAcoountNumber(), transaction.getTransactionType(),
                        transaction.getTransactionMsg(), transaction.getTransactionDate(), transaction.getTransactionAmount()));
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Hide progress bar after loading data
            progressBar.setVisibility(View.GONE);

            // Initialize adapters
            bankAdapter = new BankAdapter(bankAccounts, MainActivity.this);
            //transactionAdapter = new TransactionAdapter(lasttransactions, MainActivity.this);
            transactionAdaptar = new TransactionAdapter(lasttransactions,MainActivity.this);


            GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
            bankRecyclerView.setLayoutManager(gridLayoutManager);
            bankRecyclerView.setAdapter(bankAdapter);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
            last_transactionView.setLayoutManager(linearLayoutManager);
            last_transactionView.setAdapter(transactionAdaptar);
        }

    }

    private void loadDataFromDatabase() {
        HashMap<String, String> BankNamesWithAccounts = new HashMap<>();
        List<SMSModel> list = readsms(BankNamesWithAccounts);

        for (SMSModel sms : list) {
            insertRecords(sms.getSender(), sms.getAccountnumber(), sms.getTransactionamount(),
                    sms.getTransactionType(), sms.getDatetime(), sms.getMessage(), sms.getAvlBal());
        }

        HashMap<String, BankAccount> accounts = getAccountsInfo("", "");
        accounts.forEach((accountNumber, bankAccount) -> {
            bankAccounts.add(new BankAccount(bankAccount.getBankName(), accountNumber, bankAccount.getBalance(), "axis"));
        });

        List<Transaction> recentTransactions = getRecentTransaction();
        recentTransactions.forEach((transaction) -> {
            lasttransactions.add(new Transaction(transaction.getBank(), transaction.getAcoountNumber(), transaction.getTransactionType(),
                    transaction.getTransactionMsg(), transaction.getTransactionDate(), transaction.getTransactionAmount()));
        });
        bankAdapter = new BankAdapter(bankAccounts, MainActivity.this);
        //transactionAdapter = new TransactionAdapter(lasttransactions, MainActivity.this);
        transactionAdaptar = new TransactionAdapter(lasttransactions,MainActivity.this);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        bankRecyclerView.setLayoutManager(gridLayoutManager);
        bankRecyclerView.setAdapter(bankAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        last_transactionView.setLayoutManager(linearLayoutManager);
        last_transactionView.setAdapter(transactionAdaptar);


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
//                        textView.append("\nAccount Number: " + accountNumber + "\nBank Name: " + BankName + "\n" + "Balance:" + TotalBal + "\nAvlBal:" + avlBal + "\nDate:" + DateTime);

//
                        String getDateTime = storeAccountDetails.get(accountNumber).getDateTime();

//                        textView.append("\nlastDate:"+getDateTime);

                    } else {
                        storeAccountDetails.put(accountNumber, bankAccount);
                        //textView.append("\nfirst time\n");
                        //textView.append("\nAccount Number: " + accountNumber + "\nBank Name: " + BankName + "\n" + "Balance:" + TotalBal + "\nAvlBal:" + avlBal + "\nDate:" + DateTime);

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


    private List<SMSModel> readsms(HashMap<String, String> bankNamesWithAccounts) {
        List<SMSModel> smsList = new ArrayList<>();
        try {
            // Ensure permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
              //  textView.setText("SMS permission not granted.");
                return smsList;
            }

            // Query SMS inbox
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            SimpleDateFormat comparisonFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Date to compare against (01/08/2024)
           // Date cutoffDate = comparisonFormat.parse("13/08/2024");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                    Date messageDate = new Date(timestamp);
                    String formattedDate = dateFormat.format(new Date(timestamp));


                    // Check if the message date is after 01/08/2024
                  //  if (messageDate.after(cutoffDate)) {
                        SMSModel sms = new SMSModel(sender, message, timestamp);
                        sms.setDatetime(formattedDate);
                        if (extractTransactionSMS(sms, bankNamesWithAccounts)) {
                            smsList.add(sms);
                        }
                   // }

                } while (cursor.moveToNext());
                cursor.close();
            } else {
                //textView.setText("No SMS messages found.");
            }

        } catch (Exception e) {
           // textView.setText("An error occurred: " + e.getMessage());
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
                dbHelper.addTransaction(sender, smsMsg, transactionAmount, transactionType, accountNumber, avlBal, avlBal, smsDate);

            } else {
                double TOTALBAL = lastAmount + transactionAmount;
                dbHelper.addTransaction(sender, smsMsg, transactionAmount, transactionType, accountNumber, TOTALBAL, avlBal, smsDate);
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
    private  List<Transaction> getRecentTransaction() {
        List<Transaction> recentTransactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String recentTransactions = "SELECT " +
                SMSDatabaseHelper.COLUMN_SENDER + ", " +
                SMSDatabaseHelper.COLUMN_ACCOUNT_NUMBER + ", " +
                SMSDatabaseHelper.COLUMN_AMOUNT + ", " +
                SMSDatabaseHelper.COLUMN_TIMESTAMP + "," +
                SMSDatabaseHelper.COLUMN_MSG + "," +
                SMSDatabaseHelper.COLUMN_TRANSACTION_TYPE +

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
                    String transactionType = bankNameCursor.getString(4);
                    String transactionMsg = bankNameCursor.getString(5);

                    Transaction transaction =  new Transaction( bankName,  accountNumber,  transactionType,  transactionMsg,  timestamp,  amount);


                        //    bankAccount.setAccountNumber(accountNumber);
                  //  bankAccount.setDateTime(timestamp);
                    //bankAccount.setBankName(bankName);
                    //bankAccount.setTransactionAmount(amount);
                    recentTransactionList.add(transaction);

                } while (bankNameCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TransactionActivity", "Error fetching recent transactions", e);
        } finally {
            db.close(); // Close the database connection
        }
        return recentTransactionList;
    }


}

