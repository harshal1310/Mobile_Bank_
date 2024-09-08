package com.example.mobilebank;

import android.content.Intent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.klinker.android.logger.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private Context context;
    private static Map<String, Integer> logoMap; // Map to hold logo resource names and IDs


    public static void initializeLogoMap() {
        logoMap = new HashMap<>();
        logoMap.put(Constants.SBI, R.drawable.sbi); // Replace with actual drawable names
        logoMap.put(Constants.HDFC, R.drawable.hdfc);
        logoMap.put(Constants.ICICI,R.drawable.icici);
        logoMap.put(Constants.AXIS,R.drawable.axis);
        // Add more mappings as needed
    }

    public TransactionAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
        initializeLogoMap();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.last_transaction_items, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Log.i("in bind","in bind");
        Log.d("name vLUE:",transaction.getTransactionDate());
        holder.bankName.setText(transaction.getBank() + "  " + transaction.getAcoountNumber());
        holder.transactionDate.setText(transaction.getTransactionDate());
        holder.transactionAmount.setText(String.format("₹ %.2f", transaction.getAmount()));
        holder.transactionType.setText(transaction.getTransactionType());

        Integer imageResId = logoMap.get(transaction.getLogoResId());
        if (imageResId != null) {
            holder.ImageIcon.setImageResource(imageResId);
        } else {
            holder.ImageIcon.setImageResource(R.drawable.sbi); // Fallback logo
        }



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("bankName", transaction.getBank());
            intent.putExtra("accountNumber",transaction.getAcoountNumber());
            intent.putExtra("transactionDate", transaction.getTransactionDate());
            intent.putExtra("transactionAmount", String.format("₹ %.2f", transaction.getAmount()));
            intent.putExtra("transactionType", transaction.getTransactionType());
            intent.putExtra("transactionDescription", transaction.getTransactionMsg());
            context.startActivity(intent);
        });




    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionDate;
        TextView transactionAmount;
        TextView bankName;
        TextView transactionType;
        ImageView ImageIcon;
        LinearLayoutManager linearLayoutManager;


        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            bankName = itemView.findViewById(R.id.bankName);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionType = itemView.findViewById(R.id.transactionType);
            ImageIcon = itemView.findViewById(R.id.transactionImage);
        }
    }
}
