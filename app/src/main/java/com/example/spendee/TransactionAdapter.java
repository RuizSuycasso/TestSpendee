package com.example.spendee;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections; // << THÊM DÒNG IMPORT NÀY
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private static final String TAG = "TransactionAdapter";
    private List<Transaction> transactionList;
    private Map<Integer, String> categoryMap;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = new ArrayList<>(transactionList != null ? transactionList : new ArrayList<>());
        this.categoryMap = new HashMap<>();
        Log.d(TAG, "Adapter initialized. Initial list size: " + this.transactionList.size());
    }

    public void setCategoryMap(Map<Integer, String> categoryMap) {
        this.categoryMap = (categoryMap != null) ? categoryMap : new HashMap<>();
        Log.d(TAG, "Category map updated. Map size: " + this.categoryMap.size());
    }

    public void updateData(List<Transaction> newTransactions) {
        Log.d(TAG, "updateData called. New list size from source: " + (newTransactions != null ? newTransactions.size() : "null"));
        if (this.transactionList == null) {
            this.transactionList = new ArrayList<>();
        }
        this.transactionList.clear();
        if (newTransactions != null) {
            List<Transaction> displayList = new ArrayList<>(newTransactions);
            Collections.reverse(displayList); // Dòng này bây giờ sẽ hoạt động
            this.transactionList.addAll(displayList);
        }
        Log.d(TAG, "updateData internal list updated. Internal list size: " + this.transactionList.size());
        notifyDataSetChanged();
        Log.d(TAG, "updateData finished, notified. getItemCount() now reports: " + getItemCount());
    }


    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (transactionList == null || position < 0 || position >= transactionList.size()) {
            Log.e(TAG, "onBindViewHolder: Invalid position or null list.");
            return;
        }
        Transaction transaction = transactionList.get(position);
        if (transaction == null) {
            Log.e(TAG, "onBindViewHolder: Transaction object at position " + position + " is null.");
            return;
        }

        double amount = transaction.getAmount();
        Integer categoryId = transaction.getCategoryId();
        String categoryName = "Chưa phân loại";
        if (categoryId != null && categoryMap.containsKey(categoryId)) {
            categoryName = categoryMap.get(categoryId);
        } else if (categoryId != null) {
            Log.w(TAG, "onBindViewHolder: Category ID " + categoryId + " not found in map.");
        } else {
            Log.w(TAG, "onBindViewHolder: Transaction at position " + position + " has null categoryId.");
        }

        Log.d(TAG, "onBindViewHolder - Pos: " + position +
                ", Amount: " + amount +
                ", isIncome: " + transaction.isIncome() +
                ", CategoryId: " + categoryId +
                ", CategoryName: " + categoryName);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(amount);

        holder.tvType.setText(categoryName);

        if (transaction.isIncome()) {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvAmount.setText("+" + formattedAmount);
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
            holder.tvAmount.setText("-" + formattedAmount);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(transaction.getDate()));
        holder.tvDate.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        int count = transactionList != null ? transactionList.size() : 0;
        return count;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAmount;
        public TextView tvType;
        public TextView tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}