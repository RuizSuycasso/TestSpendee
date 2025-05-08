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
import java.util.ArrayList;
import java.util.HashMap; // << THÊM IMPORT
import java.util.List;
import java.util.Locale;
import java.util.Map; // << THÊM IMPORT

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ViewHolder> {

    private static final String TAG = "ReportDetailAdapter";
    private List<Transaction> transactionList;
    private boolean isIncomeAdapter;
    // << THÊM MAP ĐỂ LƯU TÊN CATEGORY >>
    private Map<Integer, String> categoryMap;


    public ReportDetailAdapter(List<Transaction> initialTransactionList, boolean isIncomeAdapter) {
        this.transactionList = new ArrayList<>(initialTransactionList != null ? initialTransactionList : new ArrayList<>());
        this.isIncomeAdapter = isIncomeAdapter;
        // Khởi tạo categoryMap rỗng
        this.categoryMap = new HashMap<>();
        Log.d(TAG, "Adapter initialized. Initial list size: " + this.transactionList.size() + ", isIncome: " + isIncomeAdapter);
    }

    // << THÊM PHƯƠNG THỨC ĐỂ ACTIVITY SET CATEGORY MAP >>
    public void setCategoryMap(Map<Integer, String> categoryMap) {
        this.categoryMap = (categoryMap != null) ? categoryMap : new HashMap<>();
        Log.d(TAG, "Category map updated for " + (isIncomeAdapter ? "Income" : "Expense") + " adapter. Map size: " + this.categoryMap.size());
        // Có thể cần notifyDataSetChanged nếu map được cập nhật sau khi list đã có dữ liệu
        // Nếu map được set trước khi updateData thì không cần.
        // Để an toàn, có thể thêm notifyDataSetChanged() nếu bạn thấy cần thiết.
        // notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called for isIncome=" + isIncomeAdapter);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_transaction_detail, parent, false);
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
            // Có thể đặt text mặc định cho holder ở đây nếu muốn
            holder.tvDescription.setText("Lỗi giao dịch");
            holder.tvAmount.setText("");
            return;
        }

        // --- LẤY TÊN CATEGORY THAY VÌ DESCRIPTION ---
        Integer categoryId = transaction.getCategoryId();
        String categoryName = "Chưa phân loại"; // Giá trị mặc định
        if (categoryId != null && categoryMap.containsKey(categoryId)) {
            categoryName = categoryMap.get(categoryId);
        } else if (categoryId != null) {
            Log.w(TAG, "onBindViewHolder: Category ID " + categoryId + " not found in map for isIncome=" + isIncomeAdapter);
            // categoryName = "ID: " + categoryId; // Hiển thị ID để debug
        } else {
            Log.w(TAG, "onBindViewHolder: Transaction at position " + position + " has null categoryId.");
        }
        // --- KẾT THÚC LẤY TÊN CATEGORY ---

        Log.d(TAG, "onBindViewHolder - Pos: " + position +
                ", CategoryId: " + categoryId +
                ", CategoryName: " + categoryName + // Đã đổi từ Desc sang CategoryName
                ", Amount: " + transaction.getAmount() +
                ", isIncome: " + transaction.isIncome());

        // Hiển thị Tên Category vào tvDescription
        holder.tvDescription.setText(categoryName); // <-- THAY ĐỔI Ở ĐÂY

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(transaction.getAmount());
        holder.tvAmount.setText(formattedAmount);

        if (isIncomeAdapter) {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        int count = transactionList != null ? transactionList.size() : 0;
        // Log.d(TAG, "getItemCount called for isIncome=" + isIncomeAdapter + ", size: " + count); // Bỏ log nếu quá nhiều
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription; // TextView này giờ sẽ hiển thị Category Name
        TextView tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            // Đảm bảo ID này đúng trong item_report_transaction_detail.xml
            tvDescription = itemView.findViewById(R.id.tv_transaction_detail_description);
            tvAmount = itemView.findViewById(R.id.tv_transaction_detail_amount);
        }
    }

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
        notifyDataSetChanged();
        Log.d(TAG, "updateData finished, notified. getItemCount() now reports: " + getItemCount());
    }
}