package com.example.spendee;

// import android.graphics.Color; // Không cần Color nếu dùng ContextCompat
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

// <-- THÊM DÒNG IMPORT NÀY -->
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Giả định class Transaction tồn tại và có phương thức long getDate() và Integer getCategoryId()
// Giả định class Category tồn tại và có phương thức int getId(), String getName();

// <-- LỖI ở dòng 24 có thể do thiếu import này, hoặc do cấu hình project -->
public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ViewHolder> {

    private static final String TAG = "ReportDetailAdapter";
    private List<Transaction> transactionList;
    private boolean isIncomeAdapter;
    private Map<Integer, String> categoryMap;
    private SimpleDateFormat dateFormat;

    public ReportDetailAdapter(List<Transaction> initialTransactionList, boolean isIncomeAdapter) {
        this.transactionList = new ArrayList<>(initialTransactionList != null ? initialTransactionList : new ArrayList<>());
        this.isIncomeAdapter = isIncomeAdapter;
        this.categoryMap = new HashMap<>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); // Hoặc Locale("vi", "VN")
        Log.d(TAG, "Adapter initialized. Initial list size: " + this.transactionList.size() + ", isIncome: " + isIncomeAdapter);
    }

    public void setCategoryMap(Map<Integer, String> categoryMap) {
        this.categoryMap = (categoryMap != null) ? categoryMap : new HashMap<>();
        Log.d(TAG, "Category map updated for " + (isIncomeAdapter ? "Income" : "Expense") + " adapter. Map size: " + this.categoryMap.size());
        // notifyDataSetChanged(); // Vẫn có thể cần nếu bạn set map sau khi dữ liệu đã có và hiển thị
    }

    @NonNull
    @Override
    // <-- LỖI ở dòng 95, 96 liên quan đến itemView có thể do thiếu import RecyclerView -->
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called for isIncome=" + isIncomeAdapter);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_transaction_detail, parent, false); // Hoặc R.layout.item_report_detail tùy tên file thực tế
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (transactionList == null || position < 0 || position >= transactionList.size()) {
            Log.e(TAG, "onBindViewHolder: Invalid position or null list.");
            return;
        }
        Transaction transaction = transactionList.get(position);
        if (transaction == null) {
            Log.e(TAG, "onBindViewHolder: Transaction object at position " + position + " is null.");
            holder.tvDescription.setText("Lỗi giao dịch");
            holder.tvAmount.setText("");
            holder.tvDate.setText("");
            return;
        }

        Integer categoryId = transaction.getCategoryId();
        String categoryName = "Chưa phân loại";
        if (categoryId != null && categoryMap.containsKey(categoryId)) {
            categoryName = categoryMap.get(categoryId);
        } else if (categoryId != null) {
            Log.w(TAG, "onBindViewHolder: Category ID " + categoryId + " not found in map for isIncome=" + isIncomeAdapter);
        } else {
            Log.w(TAG, "onBindViewHolder: Transaction at position " + position + " has null categoryId.");
        }

        Log.d(TAG, "onBindViewHolder - Pos: " + position +
                ", CategoryId: " + categoryId +
                ", CategoryName: " + categoryName +
                ", Amount: " + transaction.getAmount() +
                ", Date (millis): " + transaction.getDate() +
                ", isIncome: " + transaction.isIncome());

        holder.tvDescription.setText(categoryName);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(transaction.getAmount());
        holder.tvAmount.setText(formattedAmount);

        // Sử dụng ContextCompat để lấy màu an toàn hơn và dùng màu mặc định của Android
        int color = isIncomeAdapter ? ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark)
                : ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark);
        holder.tvAmount.setTextColor(color);

        // --- HIỂN THỊ NGÀY THÁNG ---
        long dateMillis = transaction.getDate();
        if (dateMillis > 0) {
            holder.tvDate.setText(dateFormat.format(new Date(dateMillis)));
        } else {
            holder.tvDate.setText("Không có ngày");
        }
        // --- KẾT THÚC HIỂN THỊ NGÀY THÁNG ---

    }

    @Override
    public int getItemCount() {
        int count = transactionList != null ? transactionList.size() : 0;
        return count;
    }

    // <-- LỖI ở dòng 117 (RecyclerView) và 123 (super) có thể do thiếu import RecyclerView -->
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription; // Hiển thị tên Category
        TextView tvAmount;
        TextView tvDate; // TextView cho ngày tháng

        ViewHolder(View itemView) {
            super(itemView); // <-- LỖI 'super' ở đây
            // Ánh xạ các View từ layout item
            tvDescription = itemView.findViewById(R.id.tv_transaction_detail_description);
            tvAmount = itemView.findViewById(R.id.tv_transaction_detail_amount);
            tvDate = itemView.findViewById(R.id.tv_transaction_detail_date);
        }
    }

    // <-- LỖI ở dòng 141 (notifyDataSetChanged) có thể do thiếu import RecyclerView -->
    public void updateData(List<Transaction> newTransactions) {
        Log.d(TAG, "updateData called for " + (isIncomeAdapter ? "Income" : "Expense") + ". New list size from source: " + (newTransactions != null ? newTransactions.size() : "null"));
        if (this.transactionList == null) {
            this.transactionList = new ArrayList<>();
        }
        this.transactionList.clear();
        if (newTransactions != null) {
            this.transactionList.addAll(newTransactions);
        }
        Log.d(TAG, "updateData internal list updated. Internal list size: " + this.transactionList.size());
        notifyDataSetChanged(); // <-- LỖI 'notifyDataSetChanged' ở đây
        Log.d(TAG, "updateData finished, notified. getItemCount() now reports: " + getItemCount());
    }
}