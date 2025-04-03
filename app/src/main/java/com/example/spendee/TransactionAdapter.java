package com.example.spendee;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        double amount = transaction.getAmount();
        String description = transaction.getDescription();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(amount);

        if (transaction.isIncome()) {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvType.setText(description);
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvType.setText(description);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(transaction.getDate()));
        holder.tvDate.setText(formattedDate);


    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAmount;
        public TextView tvType;
        public TextView tvDate;
        // public TextView tvDescription

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDate = itemView.findViewById(R.id.tv_date);
            // tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}