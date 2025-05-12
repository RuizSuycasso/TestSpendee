package com.example.spendee;

// --- CÁC IMPORT CẦN THIẾT ---
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
// --- KẾT THÚC IMPORT ---


public class BudgetActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private static final String TAG = "BudgetActivity";

    private Toolbar toolbar;
    private RecyclerView rvBudgets;
    private TextView tvNoBudgets, tvSelectedMonthYearBudget;
    private Button btnChangeMonthYearBudget;
    private FloatingActionButton fabAddBudget;

    private View warningBudgetExceededBox; // Biến để giữ tham chiếu đến box cảnh báo (LinearLayout)
    private TextView tvWarningMessage; // Biến để giữ tham chiếu đến TextView trong box


    private BudgetAdapter budgetAdapter;
    private List<Budget> currentBudgetList;
    private AppDatabase db;
    private Map<Integer, String> categoryNameMap;
    private List<Category> expenseCategoryList;
    private ArrayAdapter<Category> categorySpinnerAdapter;

    private int currentSelectedYear;
    private int currentSelectedMonth;

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
        fabAddBudget = findViewById(R.id.fab_add_budget);
        tvSelectedMonthYearBudget = findViewById(R.id.tv_selected_month_year_budget);
        btnChangeMonthYearBudget = findViewById(R.id.btn_change_month_year_budget);

        // --- Ánh xạ box cảnh báo và TextView bên trong ---
        warningBudgetExceededBox = findViewById(R.id.warning_budget_box); // Tìm thẻ <include> bằng ID của nó
        if (warningBudgetExceededBox != null) {
            tvWarningMessage = warningBudgetExceededBox.findViewById(R.id.tv_warning_message); // Tìm TextView bên trong layout đã include
            if (tvWarningMessage == null) {
                Log.e(TAG, "TextView tv_warning_message not found inside included layout!");
            }
        } else {
            Log.e(TAG, "Included layout with ID warning_budget_box not found!");
        }
        // --- Kết thúc ánh xạ ---


        budgetAdapter = new BudgetAdapter(this, currentBudgetList, this);
        rvBudgets.setAdapter(budgetAdapter);

        categorySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategoryList);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Calendar calendar = Calendar.getInstance();
        currentSelectedYear = calendar.get(Calendar.YEAR);
        currentSelectedMonth = calendar.get(Calendar.MONTH);
        updateSelectedMonthYearDisplay();

        loadRequiredData();

        // Lỗi "Cannot resolve method 'showAddEditBudgetDialog'" xảy ra ở đây
        // vì phương thức showAddEditBudgetDialog chưa được định nghĩa (hoặc bị thiếu)
        fabAddBudget.setOnClickListener(v -> showAddEditBudgetDialog(null));

        btnChangeMonthYearBudget.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void updateSelectedMonthYearDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentSelectedYear);
        cal.set(Calendar.MONTH, currentSelectedMonth);
        tvSelectedMonthYearBudget.setText(monthYearDisplayFormat.format(cal.getTime()));
    }

    private void showMonthYearPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);

        if (monthPicker == null || yearPicker == null) {
            Log.e(TAG, "showMonthYearPickerDialog: monthPicker or yearPicker is null!");
            Toast.makeText(this, "Lỗi hiển thị dialog.", Toast.LENGTH_SHORT).show();
            return;
        }

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
            currentSelectedMonth = monthPicker.getValue() - 1;
            currentSelectedYear = yearPicker.getValue();
            updateSelectedMonthYearDisplay();
            loadRequiredData(); // Tải lại dữ liệu khi chọn tháng/năm mới
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void loadRequiredData() {
        Log.d(TAG, "loadRequiredData: Loading categories and budgets for " + (currentSelectedMonth+1) + "/" + currentSelectedYear);
        new LoadBudgetDataTask(currentSelectedYear, currentSelectedMonth).execute();
    }

    // --- AsyncTask LoadBudgetDataTask ---
    private class LoadBudgetDataTask extends AsyncTask<Void, Void, List<Budget>> {
        private final int year;
        private final int month;
        private Map<Integer, String> localCategoryMap = new HashMap<>();
        private List<Category> localExpenseCategories = new ArrayList<>();

        LoadBudgetDataTask(int year, int month) {
            this.year = year;
            this.month = month;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Có thể hiển thị ProgressBar tại đây
        }

        @Override
        protected List<Budget> doInBackground(Void... voids) {
            Log.d(TAG, "LoadBudgetDataTask: doInBackground - Started");
            List<Budget> budgetsForMonth = new ArrayList<>();
            try {
                // 1. Tải tất cả danh mục chi tiêu
                List<Category> allCategories = db.categoryDAO().getAllCategories();
                if (allCategories != null) {
                    for (Category cat : allCategories) {
                        localCategoryMap.put(cat.getId(), cat.getName());
                        if (!cat.isIncomeCategory()) { // Chỉ lấy danh mục chi tiêu
                            localExpenseCategories.add(cat);
                        }
                    }
                } else {
                    Log.w(TAG, "LoadBudgetDataTask: No categories found.");
                }

                // 2. Tải ngân sách cho tháng/năm được chọn
                budgetsForMonth = db.budgetDAO().getBudgetsForMonth(year, month);
                Log.d(TAG, "LoadBudgetDataTask: Found " + (budgetsForMonth != null ? budgetsForMonth.size() : 0) + " budgets for " + (month+1) + "/" + year);

                // 3. Tính tổng chi tiêu cho từng ngân sách trong tháng
                Calendar calStart = Calendar.getInstance();
                calStart.set(year, month, 1, 0, 0, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                long startTime = calStart.getTimeInMillis();

                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(startTime);
                calEnd.add(Calendar.MONTH, 1);
                calEnd.add(Calendar.MILLISECOND, -1); // Kết thúc vào cuối ngày cuối tháng
                long endTime = calEnd.getTimeInMillis();

                Log.d(TAG, "LoadBudgetDataTask: Time range for transactions: " + new Date(startTime) + " to " + new Date(calEnd.getTimeInMillis()));


                if (budgetsForMonth != null) {
                    for (Budget budget : budgetsForMonth) {
                        // Gán tên danh mục (cho hiển thị)
                        budget.setCategoryName(localCategoryMap.getOrDefault(budget.getCategoryId(), "Không rõ"));

                        // Tính tổng chi tiêu cho danh mục này trong khoảng thời gian
                        // Đảm bảo chỉ lấy giao dịch LOAI_CHI (hoặc is_income = 0)
                        // **** CHÚ Ý: Bạn cần có phương thức này trong TransactionDAO của bạn ****
                        // Ví dụ: getSumOfExpensesForCategoryInPeriodAndType(int categoryId, String type, long startTime, long endTime);
                        // Hoặc đảm bảo getSumOfExpensesForCategoryInPeriod đã lọc theo LOAI_CHI
                        // Mình giả định TransactionDAO có phương thức getSumOfExpensesForCategoryInPeriod
                        // và query bên trong nó đã lọc theo loại chi.
                        Double spent = db.transactionDAO().getSumOfExpensesForCategoryInPeriod(budget.getCategoryId(), startTime, endTime);
                        // Nếu TransactionDAO của bạn có phương thức lọc theo type:
                        // Double spent = db.transactionDAO().getSumOfTransactionsForCategoryInPeriodAndType(budget.getCategoryId(), "Chi", startTime, endTime);

                        budget.setSpentAmount(spent != null ? spent : 0.0);
                        Log.d(TAG, "LoadBudgetDataTask: Category ID " + budget.getCategoryId() + " spent: " + budget.getSpentAmount() + ", Limit: " + budget.getLimitAmount());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "LoadBudgetDataTask: Error loading data", e);
                return null; // Trả về null nếu có lỗi
            }
            return budgetsForMonth;
        }


        @Override
        protected void onPostExecute(List<Budget> loadedBudgets) {
            Log.d(TAG, "LoadBudgetDataTask: onPostExecute");
            // Ẩn ProgressBar tại đây (nếu có)

            // Cập nhật danh sách danh mục chi tiêu cho Spinner
            expenseCategoryList.clear();
            expenseCategoryList.addAll(localExpenseCategories);
            categorySpinnerAdapter.notifyDataSetChanged();

            // Cập nhật Map tên danh mục (cho Adapter)
            categoryNameMap = localCategoryMap;

            if (loadedBudgets != null) {
                currentBudgetList.clear();
                currentBudgetList.addAll(loadedBudgets);

                // --- KIỂM TRA VƯỢT NGÂN SÁCH VÀ HIỂN THỊ BOX CẢNH BÁO ---
                boolean anyBudgetExceeded = false;
                StringBuilder exceededCategoriesMessage = new StringBuilder(); // Dùng để liệt kê các danh mục vượt
                int exceededCount = 0;

                for (Budget budget : currentBudgetList) {
                    // Chỉ kiểm tra nếu hạn mức > 0 và tiền chi > hạn mức
                    if (budget.getLimitAmount() > 0 && budget.getSpentAmount() > budget.getLimitAmount()) {
                        anyBudgetExceeded = true;
                        exceededCount++;
                        String categoryName = budget.getCategoryName() != null ? budget.getCategoryName() : "Không rõ";
                        if (exceededCategoriesMessage.length() > 0) {
                            exceededCategoriesMessage.append(", ");
                        }
                        exceededCategoriesMessage.append(categoryName);
                        Log.d(TAG, "onPostExecute: Budget exceeded for category: " + categoryName);
                    }
                }

                budgetAdapter.updateData(currentBudgetList);

                // Điều khiển visibility của RecyclerView và No Budgets TextView
                if (currentBudgetList.isEmpty()) {
                    tvNoBudgets.setVisibility(View.VISIBLE);
                    rvBudgets.setVisibility(View.GONE);
                } else {
                    tvNoBudgets.setVisibility(View.GONE);
                    rvBudgets.setVisibility(View.VISIBLE);
                }

                // Hiển thị hoặc ẩn box cảnh báo dựa trên kết quả kiểm tra
                // Kiểm tra null trước khi truy cập view để tránh crash
                if (warningBudgetExceededBox != null && tvWarningMessage != null) {
                    if (anyBudgetExceeded) {
                        tvWarningMessage.setText("⚠️ Đã vượt ngân sách ở các danh mục: " + exceededCategoriesMessage.toString()); // Cập nhật nội dung chi tiết
                        warningBudgetExceededBox.setVisibility(View.VISIBLE); // Hiển thị box
                    } else {
                        warningBudgetExceededBox.setVisibility(View.GONE); // Ẩn box nếu không có ngân sách nào bị vượt
                    }
                } else {
                    Log.e(TAG, "Warning box views are null, cannot show/hide warning.");
                }


            } else {
                // Xử lý trường hợp tải dữ liệu thất bại
                Toast.makeText(BudgetActivity.this, "Lỗi khi tải dữ liệu ngân sách", Toast.LENGTH_SHORT).show();
                tvNoBudgets.setVisibility(View.VISIBLE);
                rvBudgets.setVisibility(View.GONE);
                currentBudgetList.clear();
                budgetAdapter.updateData(currentBudgetList); // Clear adapter data
                // Đảm bảo ẩn box khi có lỗi tải dữ liệu
                if (warningBudgetExceededBox != null) {
                    warningBudgetExceededBox.setVisibility(View.GONE);
                }
            }
        }
    }
    // --- Kết thúc AsyncTask LoadBudgetDataTask ---


    // --- Dialog showAddEditBudgetDialog (PHƯƠNG THỨC BỊ THIẾU TRONG LỖI 2) ---
    private void showAddEditBudgetDialog(final Budget existingBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        // Đảm bảo bạn có layout dialog_add_edit_budget.xml
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_budget, null);
        builder.setView(dialogView);

        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_budget_category);
        final EditText edtAmount = dialogView.findViewById(R.id.edt_budget_amount);
        final Button btnSave = dialogView.findViewById(R.id.btn_save_budget);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel_budget);
        final TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_budget_title);

        spinnerCategory.setAdapter(categorySpinnerAdapter);

        if (existingBudget != null) {
            // Chế độ Sửa
            tvTitle.setText("Sửa Ngân Sách");
            edtAmount.setText(String.valueOf(existingBudget.getLimitAmount()));

            // Chọn danh mục đúng trong Spinner
            for (int i = 0; i < expenseCategoryList.size(); i++) {
                if (expenseCategoryList.get(i).getId() == existingBudget.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            // Không cho phép sửa danh mục khi sửa ngân sách đã tạo
            spinnerCategory.setEnabled(false);
        } else {
            // Chế độ Thêm mới
            tvTitle.setText("Thêm Ngân Sách Mới");
            spinnerCategory.setEnabled(true);
        }

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
            String amountStr = edtAmount.getText().toString().trim();

            if (selectedCategory == null) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập hạn mức", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount < 0) { // Cho phép hạn mức bằng 0 nếu người dùng muốn
                    Toast.makeText(this, "Hạn mức không thể âm", Toast.LENGTH_SHORT).show();
                    return;
                }

                Budget budgetToSave;
                if (existingBudget != null) {
                    // Cập nhật budget hiện có
                    budgetToSave = existingBudget;
                    budgetToSave.setLimitAmount(amount);
                } else {
                    // Tạo budget mới
                    budgetToSave = new Budget(selectedCategory.getId(), amount, currentSelectedYear, currentSelectedMonth);
                }

                // Lưu budget vào DB bằng AsyncTask
                new SaveBudgetTask().execute(budgetToSave);
                dialog.dismiss(); // Đóng dialog sau khi lưu
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    // --- Kết thúc Dialog showAddEditBudgetDialog ---


    // --- AsyncTask SaveBudgetTask ---
    private class SaveBudgetTask extends AsyncTask<Budget, Void, Void> {
        @Override
        protected Void doInBackground(Budget... budgets) {
            if (budgets.length > 0 && budgets[0] != null) {
                try {
                    // Sử dụng insertOrUpdate để vừa thêm mới hoặc cập nhật (dựa vào unique index)
                    db.budgetDAO().insertOrUpdate(budgets[0]);
                    Log.d(TAG, "SaveBudgetTask: Budget saved/updated successfully.");
                } catch (Exception e) {
                    Log.e(TAG, "SaveBudgetTask: Error saving/updating budget", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Tải lại dữ liệu sau khi lưu thành công để cập nhật UI
            loadRequiredData();
            Toast.makeText(BudgetActivity.this, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
        }
    }
    // --- Kết thúc AsyncTask SaveBudgetTask ---


    // --- AsyncTask DeleteBudgetTask ---
    private class DeleteBudgetTask extends AsyncTask<Budget, Void, Void> {
        @Override
        protected Void doInBackground(Budget... budgets) {
            if (budgets.length > 0 && budgets[0] != null) {
                try {
                    db.budgetDAO().delete(budgets[0]);
                    Log.d(TAG, "DeleteBudgetTask: Budget deleted successfully.");
                } catch (Exception e) {
                    Log.e(TAG, "DeleteBudgetTask: Error deleting budget", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Tải lại dữ liệu sau khi xóa thành công để cập nhật UI
            loadRequiredData();
            Toast.makeText(BudgetActivity.this, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
        }
    }
    // --- Kết thúc AsyncTask DeleteBudgetTask ---


    // --- Xử lý sự kiện Adapter (IMPLEMENTATION CỦA OnBudgetActionListener - LỖI 1) ---
    @Override
    public void onEditClick(Budget budget) {
        Log.d(TAG, "onEditClick: Editing budget ID " + budget.getId());
        // Phương thức showAddEditBudgetDialog được gọi ở đây
        showAddEditBudgetDialog(budget);
    }

    @Override
    public void onDeleteClick(Budget budget) {
        Log.d(TAG, "onDeleteClick: Deleting budget ID " + budget.getId());
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ngân sách cho danh mục '" + categoryNameMap.getOrDefault(budget.getCategoryId(), "N/A") + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new DeleteBudgetTask().execute(budget);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    // --- Kết thúc Xử lý sự kiện Adapter ---


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