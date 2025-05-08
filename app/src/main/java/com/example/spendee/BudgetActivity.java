package com.example.spendee;

// --- THÊM CÁC IMPORT CẦN THIẾT ---
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker; // << IMPORT
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // << IMPORT
import java.text.SimpleDateFormat; // << IMPORT
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale; // << IMPORT
import java.util.Map;
import java.util.Date; // << IMPORT (Cần cho SimpleDateFormat nếu dùng)
// --- KẾT THÚC THÊM IMPORT ---


public class BudgetActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private static final String TAG = "BudgetActivity";

    private Toolbar toolbar;
    private RecyclerView rvBudgets;
    private TextView tvNoBudgets, tvSelectedMonthYearBudget;
    private Button btnChangeMonthYearBudget;
    private FloatingActionButton fabAddBudget; // Kiểu dữ liệu đã được import

    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetList;
    private AppDatabase db;
    private Map<Integer, String> categoryNameMap;
    private List<Category> expenseCategoryList;
    private ArrayAdapter<Category> categorySpinnerAdapter;

    private int currentSelectedYear;
    private int currentSelectedMonth;

    // Khai báo SimpleDateFormat ở đây nếu dùng chung
    private final SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Log.d(TAG, "onCreate: Starting");

        db = AppDatabase.getInstance(this);
        categoryNameMap = new HashMap<>();
        expenseCategoryList = new ArrayList<>();
        currentBudgetList = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar_budget);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Ngân Sách");
        }

        rvBudgets = findViewById(R.id.rv_budgets);
        tvNoBudgets = findViewById(R.id.tv_no_budgets);
        fabAddBudget = findViewById(R.id.fab_add_budget); // Giờ sẽ tìm được
        tvSelectedMonthYearBudget = findViewById(R.id.tv_selected_month_year_budget);
        btnChangeMonthYearBudget = findViewById(R.id.btn_change_month_year_budget);

        budgetAdapter = new BudgetAdapter(this, currentBudgetList, this);
        rvBudgets.setAdapter(budgetAdapter);

        categorySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategoryList);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Calendar calendar = Calendar.getInstance();
        currentSelectedYear = calendar.get(Calendar.YEAR);
        currentSelectedMonth = calendar.get(Calendar.MONTH);
        updateSelectedMonthYearDisplay();

        loadRequiredData();

        fabAddBudget.setOnClickListener(v -> showAddEditBudgetDialog(null)); // Giờ sẽ hoạt động

        btnChangeMonthYearBudget.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void updateSelectedMonthYearDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentSelectedYear);
        cal.set(Calendar.MONTH, currentSelectedMonth);
        // SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault()); // Dùng biến thành viên
        tvSelectedMonthYearBudget.setText(monthYearDisplayFormat.format(cal.getTime())); // Giờ sẽ hoạt động
    }

    private void showMonthYearPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month); // Giờ sẽ hoạt động
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year); // Giờ sẽ hoạt động

        if (monthPicker == null || yearPicker == null) {
            Log.e(TAG, "showMonthYearPickerDialog: monthPicker or yearPicker is null!");
            Toast.makeText(this, "Lỗi hiển thị dialog.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Các lệnh setMinValue, setMaxValue, setValue... giờ sẽ hoạt động
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        String[] displayMonths = {"Thg 1", "Thg 2", "Thg 3", "Thg 4", "Thg 5", "Thg 6", "Thg 7", "Thg 8", "Thg 9", "Thg 10", "Thg 11", "Thg 12"};
        monthPicker.setDisplayedValues(displayMonths);
        monthPicker.setValue(currentSelectedMonth + 1);
        int currentYearValue = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYearValue - 5);
        yearPicker.setMaxValue(currentYearValue + 5);
        yearPicker.setValue(currentSelectedYear);

        builder.setTitle("Chọn Tháng/Năm");
        builder.setPositiveButton("Chọn", (dialog, which) -> {
            currentSelectedMonth = monthPicker.getValue() - 1; // Giờ sẽ hoạt động
            currentSelectedYear = yearPicker.getValue(); // Giờ sẽ hoạt động
            updateSelectedMonthYearDisplay();
            loadRequiredData();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void loadRequiredData() {
        Log.d(TAG, "loadRequiredData: Loading categories and budgets for " + (currentSelectedMonth+1) + "/" + currentSelectedYear);
        new LoadBudgetDataTask(currentSelectedYear, currentSelectedMonth).execute();
    }

    // --- AsyncTask LoadBudgetDataTask giữ nguyên ---
    private class LoadBudgetDataTask extends AsyncTask<Void, Void, List<Budget>> {
        // ... (Nội dung giữ nguyên) ...
        private final int year;
        private final int month;
        private Map<Integer, String> localCategoryMap = new HashMap<>();
        private List<Category> localExpenseCategories = new ArrayList<>();

        LoadBudgetDataTask(int year, int month) {
            this.year = year;
            this.month = month;
        }
        @Override
        protected List<Budget> doInBackground(Void... voids) {
            // ... (Nội dung giữ nguyên) ...
            Log.d(TAG, "LoadBudgetDataTask: doInBackground - Started");
            List<Budget> budgetsForMonth = new ArrayList<>();
            try {
                List<Category> allCategories = db.categoryDAO().getAllCategories();
                if (allCategories != null) {
                    for (Category cat : allCategories) {
                        localCategoryMap.put(cat.getId(), cat.getName());
                        if (!cat.isIncomeCategory()) {
                            localExpenseCategories.add(cat);
                        }
                    }
                }
                budgetsForMonth = db.budgetDAO().getBudgetsForMonth(year, month);
                Calendar calStart = Calendar.getInstance();
                calStart.set(year, month, 1, 0, 0, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                long startTime = calStart.getTimeInMillis();
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(startTime);
                calEnd.add(Calendar.MONTH, 1);
                long endTime = calEnd.getTimeInMillis();
                for (Budget budget : budgetsForMonth) {
                    budget.setCategoryName(localCategoryMap.getOrDefault(budget.getCategoryId(), "Không rõ"));
                    Double spent = db.transactionDAO().getSumOfExpensesForCategoryInPeriod(budget.getCategoryId(), startTime, endTime);
                    budget.setSpentAmount(spent != null ? spent : 0.0);
                }
            } catch (Exception e) { Log.e(TAG, "LoadBudgetDataTask: Error loading data", e); return null; }
            return budgetsForMonth;
        }
        @Override
        protected void onPostExecute(List<Budget> loadedBudgets) {
            // ... (Nội dung giữ nguyên) ...
            Log.d(TAG, "LoadBudgetDataTask: onPostExecute");
            expenseCategoryList.clear(); expenseCategoryList.addAll(localExpenseCategories); categorySpinnerAdapter.notifyDataSetChanged();
            categoryNameMap = localCategoryMap;
            if (loadedBudgets != null) {
                currentBudgetList.clear(); currentBudgetList.addAll(loadedBudgets); budgetAdapter.updateData(currentBudgetList);
                if (currentBudgetList.isEmpty()) { tvNoBudgets.setVisibility(View.VISIBLE); rvBudgets.setVisibility(View.GONE); }
                else { tvNoBudgets.setVisibility(View.GONE); rvBudgets.setVisibility(View.VISIBLE); }
            } else {
                Toast.makeText(BudgetActivity.this, "Lỗi khi tải dữ liệu ngân sách", Toast.LENGTH_SHORT).show();
                tvNoBudgets.setVisibility(View.VISIBLE); rvBudgets.setVisibility(View.GONE); currentBudgetList.clear(); budgetAdapter.updateData(currentBudgetList);
            }
        }
    }

    // --- Dialog showAddEditBudgetDialog giữ nguyên ---
    private void showAddEditBudgetDialog(final Budget existingBudget) {
        // ... (Nội dung giữ nguyên) ...
        AlertDialog.Builder builder = new AlertDialog.Builder(this); LayoutInflater inflater = this.getLayoutInflater(); View dialogView = inflater.inflate(R.layout.dialog_add_edit_budget, null); builder.setView(dialogView);
        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_budget_category); final EditText edtAmount = dialogView.findViewById(R.id.edt_budget_amount); final Button btnSave = dialogView.findViewById(R.id.btn_save_budget); final Button btnCancel = dialogView.findViewById(R.id.btn_cancel_budget); final TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_budget_title);
        spinnerCategory.setAdapter(categorySpinnerAdapter);
        if (existingBudget != null) {
            tvTitle.setText("Sửa Ngân Sách"); edtAmount.setText(String.valueOf(existingBudget.getLimitAmount()));
            for (int i = 0; i < expenseCategoryList.size(); i++) { if (expenseCategoryList.get(i).getId() == existingBudget.getCategoryId()) { spinnerCategory.setSelection(i); break; } }
            spinnerCategory.setEnabled(false);
        } else { tvTitle.setText("Thêm Ngân Sách Mới"); spinnerCategory.setEnabled(true); }
        final AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem(); String amountStr = edtAmount.getText().toString();
            if (selectedCategory == null) { Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show(); return; } if (amountStr.isEmpty()) { Toast.makeText(this, "Vui lòng nhập hạn mức", Toast.LENGTH_SHORT).show(); return; }
            try {
                double amount = Double.parseDouble(amountStr); if (amount <= 0) { Toast.makeText(this, "Hạn mức phải lớn hơn 0", Toast.LENGTH_SHORT).show(); return; }
                Budget budgetToSave; if (existingBudget != null) { budgetToSave = existingBudget; budgetToSave.setLimitAmount(amount); } else { budgetToSave = new Budget(selectedCategory.getId(), amount, currentSelectedYear, currentSelectedMonth); }
                new SaveBudgetTask().execute(budgetToSave); dialog.dismiss();
            } catch (NumberFormatException e) { Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show(); }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss()); dialog.show();
    }

    // --- AsyncTask SaveBudgetTask giữ nguyên ---
    private class SaveBudgetTask extends AsyncTask<Budget, Void, Void> {
        // ... (Nội dung giữ nguyên) ...
        @Override protected Void doInBackground(Budget... budgets) { if (budgets.length > 0 && budgets[0] != null) { try { db.budgetDAO().insertOrUpdate(budgets[0]); } catch (Exception e) { Log.e(TAG, "SaveBudgetTask: Error saving/updating budget", e); } } return null; }
        @Override protected void onPostExecute(Void aVoid) { loadRequiredData(); Toast.makeText(BudgetActivity.this, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show(); }
    }

    // --- AsyncTask DeleteBudgetTask giữ nguyên ---
    private class DeleteBudgetTask extends AsyncTask<Budget, Void, Void> {
        // ... (Nội dung giữ nguyên) ...
        @Override protected Void doInBackground(Budget... budgets) { if (budgets.length > 0 && budgets[0] != null) { try { db.budgetDAO().delete(budgets[0]); } catch (Exception e) { Log.e(TAG, "DeleteBudgetTask: Error deleting budget", e); } } return null; }
        @Override protected void onPostExecute(Void aVoid) { loadRequiredData(); Toast.makeText(BudgetActivity.this, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show(); }
    }

    // --- Xử lý sự kiện Adapter giữ nguyên ---
    @Override public void onEditClick(Budget budget) { showAddEditBudgetDialog(budget); }
    @Override public void onDeleteClick(Budget budget) { new AlertDialog.Builder(this) .setTitle("Xác nhận xóa") .setMessage("Bạn có chắc chắn muốn xóa ngân sách cho danh mục '" + categoryNameMap.getOrDefault(budget.getCategoryId(), "N/A") + "' không?") .setPositiveButton("Xóa", (dialog, which) -> { new DeleteBudgetTask().execute(budget); }) .setNegativeButton("Hủy", null) .show(); }

    // onOptionsItemSelected giữ nguyên
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}