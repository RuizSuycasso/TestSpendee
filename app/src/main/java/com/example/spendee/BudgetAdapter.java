package com.example.spendee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private static final String TAG = "BudgetAdapter";
    private List<Budget> budgetList;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final Context context; // Context để lấy màu

    // --- Interface cho sự kiện click nút Sửa/Xóa ---
    public interface OnBudgetActionListener {
        void onEditClick(Budget budget);
        void onDeleteClick(Budget budget);
    }
    private OnBudgetActionListener actionListener;
    // --- Kết thúc Interface ---

    public BudgetAdapter(Context context, List<Budget> budgetList, OnBudgetActionListener listener) {
        this.context = context;
        this.budgetList = new ArrayList<>(budgetList != null ? budgetList : new ArrayList<>());
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        holder.tvCategoryName.setText(budget.getCategoryName() != null ? budget.getCategoryName() : "Không rõ");
        holder.tvSpentVsLimit.setText(String.format(Locale.getDefault(), "Đã chi: %s / %s",
                currencyFormatter.format(budget.getSpentAmount()),
                currencyFormatter.format(budget.getLimitAmount())));

        // Tính toán và đặt progress bar
        int progress = 0;
        if (budget.getLimitAmount() > 0) {
            progress = (int) ((budget.getSpentAmount() / budget.getLimitAmount()) * 100);
        }
        holder.progressBar.setProgress(Math.min(progress, 100)); // Giới hạn tối đa là 100

        // Đổi màu progress bar nếu vượt quá ngân sách
        if (budget.getSpentAmount() > budget.getLimitAmount()) {
            holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.red)); // Tạo màu red trong colors.xml
        } else if (progress > 80) { // Ví dụ: đổi màu vàng nếu gần hết
            holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.yellow)); // Tạo màu yellow trong colors.xml
        }
        else {
            holder.progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.teal_700)); // Màu mặc định
        }


        // Gán sự kiện click cho nút sửa/xóa
        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClick(budget);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList != null ? budgetList.size() : 0;
    }

    public void updateData(List<Budget> newBudgets) {
        this.budgetList.clear();
        if (newBudgets != null) {
            this.budgetList.addAll(newBudgets);
        }
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        TextView tvSpentVsLimit;
        ProgressBar progressBar;
        ImageButton btnEdit;
        ImageButton btnDelete;

        BudgetViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_budget_category_name);
            tvSpentVsLimit = itemView.findViewById(R.id.tv_budget_spent_vs_limit);
            progressBar = itemView.findViewById(R.id.pb_budget_progress);
            btnEdit = itemView.findViewById(R.id.btn_edit_budget);
            btnDelete = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}