package com.example.mobilebank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankAdapter extends RecyclerView.Adapter<BankAdapter.BankViewHolder> {

    private List<BankAccount> bankAccounts;
    private Context context;
    private Map<String, Integer> logoMap; // Map to hold logo resource names and IDs

    public BankAdapter(List<BankAccount> bankAccounts, Context context) {
        this.bankAccounts = bankAccounts;
        this.context = context;
        initializeLogoMap(); // Initialize the logo resource map
    }
    private void initializeLogoMap() {
        logoMap = new HashMap<>();
        logoMap.put("sbi", R.drawable.sbi); // Replace with actual drawable names
        logoMap.put("hdfc", R.drawable.hdfc);
        logoMap.put("icici",R.drawable.icici);
        logoMap.put("axis",R.drawable.axis);
        // Add more mappings as needed
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank, parent, false);
        return new BankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        BankAccount account = bankAccounts.get(position);
        holder.bankName.setText(account.getBankName());
        holder.accountNumber.setText(account.getAccountNumber());
        holder.balance.setText(String.format("₹ %.2f", account.getBalance()));
        Integer imageResId = logoMap.get(account.getLogoResId());
        if (imageResId != null) {
            holder.bankImage.setImageResource(imageResId);
        } else {
            holder.bankImage.setImageResource(R.drawable.sbi); // Fallback logo
        }

        // Optionally set up another RecyclerView or any additional logic here if needed
    }

    @Override
    public int getItemCount() {
        return bankAccounts.size();
    }
    public void updateData(List<BankAccount> newBankAccounts) {
        this.bankAccounts = newBankAccounts;
        notifyDataSetChanged(); // Notify the adapter to refresh the data
    }

    public static class BankViewHolder extends RecyclerView.ViewHolder {
        TextView bankName, accountNumber, balance;
        ImageView bankImage;

        public BankViewHolder(@NonNull View itemView) {
            super(itemView);
            bankImage = itemView.findViewById(R.id.bankLogo);
            bankName = itemView.findViewById(R.id.bankName);
            accountNumber = itemView.findViewById(R.id.bankAccountNumber);
            balance = itemView.findViewById(R.id.bankBalance);
        }
    }
}
