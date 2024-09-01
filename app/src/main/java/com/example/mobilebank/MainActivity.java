package com.example.mobilebank;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private EditText searchBar;
    private Spinner bankSpinner;
    private RecyclerView bankRecyclerView,last_transactionView;
    private BankAdapter bankAdapter;
    private TransactionAdapter transactionAdaptar;
    private List<BankAccount> bankAccounts;
    private List<Transaction> lasttransactions;
    private BankDataBaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "onCreate: Initializing components");


        // searchBar = findViewById(R.id.searchBar);
        //bankSpinner = findViewById(R.id.bankSpinner);
        bankRecyclerView = findViewById(R.id.bankRecyclerView);
        last_transactionView = findViewById(R.id.transactionRecyclerView);

        // Initialize bank accounts list (Dummy data for now)
        bankAccounts = new ArrayList<>();
        lasttransactions = new ArrayList<>();

        bankAccounts.add(new BankAccount("State Bank of India", "1234567890", 50487.49, "sbi"));
        bankAccounts.add(new BankAccount("ICICI", "9876543210", 26888.59, "icici"));
        bankAccounts.add(new BankAccount("HDFC", "1234567891", 12888.12, "hdfc"));
        bankAccounts.add(new BankAccount("Axis Bank", "9876543211", 7898.39, "axis"));
        lasttransactions.add(new Transaction("SBI","2 Aug",5000,"Kirana"));

        List<String> bankNames = new ArrayList<>();
        for (BankAccount account : bankAccounts) {
            bankNames.add(account.getBankName());
        }

        bankAdapter = new BankAdapter(bankAccounts, this);
        transactionAdaptar = new TransactionAdapter(lasttransactions,this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);  // 2 columns
        bankRecyclerView.setLayoutManager(gridLayoutManager);
        bankRecyclerView.setAdapter(bankAdapter);

        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        last_transactionView.setLayoutManager(linearLayoutManager);
        last_transactionView.setAdapter(transactionAdaptar);


        // Request SMS permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        }
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
            startActivity(intent);
            finish(); // Optionally finish MainActivity so that it won't be in the back stack
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}

