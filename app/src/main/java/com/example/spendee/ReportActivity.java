package com.example.spendee;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
// Bỏ import không cần thiết
// import java.util.Comparator;
import java.util.HashMap;
// import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// import java.util.stream.Collectors;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG_DEBUG = "ReportActivityDebug";

    private Toolbar toolbar;
    private TextView tvSelectedMonthYear, tvTotalIncome, tvTotalExpense;
    private TextView tvNoIncomeDetails, tvNoExpenseDetails;
    private Button btnChangeMonthYear;
    private RecyclerView rvIncomeDetails, rvExpenseDetails;

    private ReportDetailAdapter incomeDetailAdapter;
    private ReportDetailAdapter expenseDetailAdapter;

    private List<Transaction> currentMonthTransactions;
    private List<Transaction> incomeTransactions;
    private List<Transaction> expenseTransactions;

    // --- THÊM MAP ĐỂ LƯU TÊN CATEGORY ---
    private Map<Integer, String> categoryNameMap;

    private AppDatabase db;
    private int currentSelectedYear;
    private int currentSelectedMonth;

    private final SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MM/yyyy", new Locale("vi", "VN"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG_DEBUG, "onCreate: Starting ReportActivity - Before setContentView");

        // Khởi tạo map rỗng trước
        categoryNameMap = new HashMap<>();

        try {
            setContentView(R.layout.activity_report); // Đảm bảo layout này đã xóa phần Top 3
            Log.d(TAG_DEBUG, "onCreate: setContentView OK");

            db = AppDatabase.getInstance(this);
            Log.d(TAG_DEBUG, "onCreate: AppDatabase.getInstance OK");

            // --- Phần findViewById và setup Toolbar ---
            toolbar = findViewById(R.id.toolbar_report);
            // ... (phần còn lại của findViewById và setup Toolbar giữ nguyên) ...
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Báo Cáo Chi Tiêu");
            }
            // --- Kết thúc findViewById và setup Toolbar ---

            // --- Phần findViewById cho các View khác ---
            tvSelectedMonthYear = findViewById(R.id.tv_selected_month_year);
            btnChangeMonthYear = findViewById(R.id.btn_change_month_year);
            tvTotalIncome = findViewById(R.id.tv_total_income);
            tvTotalExpense = findViewById(R.id.tv_total_expense);
            rvIncomeDetails = findViewById(R.id.rv_income_details);
            rvExpenseDetails = findViewById(R.id.rv_expense_details);
            tvNoIncomeDetails = findViewById(R.id.tv_no_income_details);
            tvNoExpenseDetails = findViewById(R.id.tv_no_expense_details);
            // --- Kết thúc findViewById ---

            // Kiểm tra null cho các View cần thiết
            if (tvSelectedMonthYear == null || btnChangeMonthYear == null || tvTotalIncome == null ||
                    tvTotalExpense == null || rvIncomeDetails == null || rvExpenseDetails == null ||
                    tvNoIncomeDetails == null || tvNoExpenseDetails == null) {
                Log.e(TAG_DEBUG, "onCreate: One or more essential views are null after findViewById.");
                Toast.makeText(this, "Lỗi: Không thể tải giao diện báo cáo.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Log.d(TAG_DEBUG, "onCreate: Essential views initialized via findViewById OK.");

            currentMonthTransactions = new ArrayList<>();
            incomeTransactions = new ArrayList<>();
            expenseTransactions = new ArrayList<>();
            Log.d(TAG_DEBUG, "onCreate: Lists initialized OK");

            setupRecyclerViews(); // Khởi tạo adapter ở đây
            Log.d(TAG_DEBUG, "onCreate: setupRecyclerViews() OK");

            // --- TẢI CATEGORY MAP TRƯỚC ---
            loadCategoryMapAndInitialTransactions(); // Gọi hàm tải cả hai

            initCurrentMonthYear(); // Vẫn khởi tạo tháng/năm hiện tại
            updateSelectedMonthYearDisplay(); // Hiển thị tháng/năm ban đầu

            btnChangeMonthYear.setOnClickListener(v -> {
                Log.d(TAG_DEBUG, "btnChangeMonthYear clicked");
                showMonthYearPickerDialog();
            });
            Log.d(TAG_DEBUG, "onCreate: btnChangeMonthYear listener set OK");

            // Không gọi loadReportDataForSelectedMonth() trực tiếp ở đây nữa
            // Nó sẽ được gọi sau khi Category Map được tải

        } catch (Exception e) {
            Log.e(TAG_DEBUG, "onCreate: CRASHED with Exception!", e);
            Toast.makeText(this, "Lỗi nghiêm trọng khi mở báo cáo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // --- THÊM PHƯƠNG THỨC TẢI MAP VÀ GIAO DỊCH BAN ĐẦU ---
    private void loadCategoryMapAndInitialTransactions() {
        Log.d(TAG_DEBUG, "loadCategoryMapAndInitialTransactions: Starting to load category map...");
        new LoadCategoriesMapTask().execute();
        // Việc tải giao dịch ban đầu sẽ được gọi từ onPostExecute của LoadCategoriesMapTask
    }
    // --- KẾT THÚC PHƯƠNG THỨC MỚI ---


    private void setupRecyclerViews() {
        Log.d(TAG_DEBUG, "setupRecyclerViews: Called");

        // Income Details (Khởi tạo Adapter với list rỗng ban đầu)
        incomeDetailAdapter = new ReportDetailAdapter(new ArrayList<>(), true);
        rvIncomeDetails.setLayoutManager(new LinearLayoutManager(this));
        rvIncomeDetails.setAdapter(incomeDetailAdapter);
        rvIncomeDetails.setNestedScrollingEnabled(false);
        Log.d(TAG_DEBUG, "setupRecyclerViews: rvIncomeDetails setup OK");

        // Expense Details (Khởi tạo Adapter với list rỗng ban đầu)
        expenseDetailAdapter = new ReportDetailAdapter(new ArrayList<>(), false);
        rvExpenseDetails.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseDetails.setAdapter(expenseDetailAdapter);
        rvExpenseDetails.setNestedScrollingEnabled(false);
        Log.d(TAG_DEBUG, "setupRecyclerViews: rvExpenseDetails setup OK");

        // Quan trọng: Truyền map (ban đầu có thể rỗng) vào adapter ngay sau khi tạo
        // Nó sẽ được cập nhật lại khi map thực sự được tải xong
        incomeDetailAdapter.setCategoryMap(this.categoryNameMap);
        expenseDetailAdapter.setCategoryMap(this.categoryNameMap);
        Log.d(TAG_DEBUG, "setupRecyclerViews: Initial empty category map passed to adapters.");
    }

    // initCurrentMonthYear, updateSelectedMonthYearDisplay, showMonthYearPickerDialog giữ nguyên
    private void initCurrentMonthYear() {
        Log.d(TAG_DEBUG, "initCurrentMonthYear: Called");
        Calendar calendar = Calendar.getInstance();
        currentSelectedYear = calendar.get(Calendar.YEAR);
        currentSelectedMonth = calendar.get(Calendar.MONTH);
        Log.d(TAG_DEBUG, "initCurrentMonthYear: currentSelectedYear=" + currentSelectedYear + ", currentSelectedMonth=" + currentSelectedMonth);
    }

    private void updateSelectedMonthYearDisplay() {
        Log.d(TAG_DEBUG, "updateSelectedMonthYearDisplay: Called");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentSelectedYear);
        cal.set(Calendar.MONTH, currentSelectedMonth);
        if (tvSelectedMonthYear != null) {
            tvSelectedMonthYear.setText(monthYearDisplayFormat.format(cal.getTime()));
            Log.d(TAG_DEBUG, "updateSelectedMonthYearDisplay: Displayed " + monthYearDisplayFormat.format(cal.getTime()));
        } else {
            Log.e(TAG_DEBUG, "updateSelectedMonthYearDisplay: tvSelectedMonthYear is null!");
        }
    }

    private void showMonthYearPickerDialog() {
        // ... (Nội dung giữ nguyên) ...
        Log.d(TAG_DEBUG, "showMonthYearPickerDialog: Called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        // ... (Setup picker giữ nguyên) ...
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        String[] displayMonths = {"Thg 1", "Thg 2", "Thg 3", "Thg 4", "Thg 5", "Thg 6", "Thg 7", "Thg 8", "Thg 9", "Thg 10", "Thg 11", "Thg 12"};
        monthPicker.setDisplayedValues(displayMonths);
        monthPicker.setValue(currentSelectedMonth + 1);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 10);
        yearPicker.setMaxValue(currentYear + 10);
        yearPicker.setValue(currentSelectedYear);

        builder.setTitle("Chọn Tháng/Năm");
        builder.setPositiveButton("Chọn", (dialog, which) -> {
            Log.d(TAG_DEBUG, "showMonthYearPickerDialog: 'Chọn' clicked");
            currentSelectedMonth = monthPicker.getValue() - 1;
            currentSelectedYear = yearPicker.getValue();
            updateSelectedMonthYearDisplay();
            loadReportDataForSelectedMonth(); // Gọi tải dữ liệu cho tháng mới
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            Log.d(TAG_DEBUG, "showMonthYearPickerDialog: 'Hủy' clicked");
            dialog.dismiss();
        });
        builder.create().show();
        Log.d(TAG_DEBUG, "showMonthYearPickerDialog: Dialog shown");
    }

    // Đổi tên một chút để rõ ràng hơn
    private void loadReportDataForSelectedMonth() {
        Log.d(TAG_DEBUG, "loadReportDataForSelectedMonth: Triggered for Year: " + currentSelectedYear + ", Month: " + currentSelectedMonth);
        // Đảm bảo category map đã được tải (hoặc đang tải), nếu chưa thì không nên gọi load transactions?
        // Trong trường hợp này, chúng ta giả định map đã tải xong từ lần đầu.
        new LoadTransactionsTask(currentSelectedYear, currentSelectedMonth).execute();
    }

    // --- LoadTransactionsTask giờ chỉ tải giao dịch ---
    private class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        private final int year;
        private final int month;

        LoadTransactionsTask(int year, int month) {
            this.year = year;
            this.month = month;
            Log.d(TAG_DEBUG, "LoadTransactionsTask: Initialized for Year: " + year + ", Month: " + month);
        }

        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            // ... (Logic query giữ nguyên) ...
            Log.d(TAG_DEBUG, "LoadTransactionsTask: doInBackground - Started");
            Calendar calStart = Calendar.getInstance();
            calStart.set(year, month, 1, 0, 0, 0);
            calStart.set(Calendar.MILLISECOND, 0);
            long startTime = calStart.getTimeInMillis();
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(startTime);
            calEnd.add(Calendar.MONTH, 1);
            long endTime = calEnd.getTimeInMillis();
            Log.d(TAG_DEBUG, "LoadTransactionsTask: doInBackground - Querying transactions from " + startTime + " to " + endTime);
            try {
                List<Transaction> result = db.transactionDAO().getTransactionsForPeriod(startTime, endTime);
                Log.d(TAG_DEBUG, "LoadTransactionsTask: doInBackground - Transactions found: " + (result != null ? result.size() : "null"));
                return result;
            } catch (Exception e) {
                Log.e(TAG_DEBUG, "LoadTransactionsTask: doInBackground - Error querying database", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            Log.d(TAG_DEBUG, "LoadTransactionsTask: onPostExecute - Received " + (transactions != null ? transactions.size() : "null") + " transactions.");
            currentMonthTransactions.clear();
            if (transactions != null) {
                currentMonthTransactions.addAll(transactions);
            }
            // --- QUAN TRỌNG: Đảm bảo categoryMap đã được cập nhật cho adapter TRƯỚC KHI xử lý và update UI ---
            // (Việc này đã được làm trong onPostExecute của LoadCategoriesMapTask và setupRecyclerViews)
            processTransactions();
            updateUI();
            Log.d(TAG_DEBUG, "LoadTransactionsTask: onPostExecute - Finished processing and UI update.");
        }
    }

    // --- ASYNCTASK MỚI ĐỂ TẢI VÀ TẠO CATEGORY MAP ---
    private class LoadCategoriesMapTask extends AsyncTask<Void, Void, Map<Integer, String>> {

        @Override
        protected Map<Integer, String> doInBackground(Void... voids) {
            Log.d(TAG_DEBUG, "LoadCategoriesMapTask: doInBackground - Started");
            Map<Integer, String> map = new HashMap<>();
            try {
                List<Category> categories = db.categoryDAO().getAllCategories();
                if (categories != null) {
                    Log.d(TAG_DEBUG, "LoadCategoriesMapTask: doInBackground - Found " + categories.size() + " categories.");
                    for (Category category : categories) {
                        map.put(category.getId(), category.getName());
                    }
                } else {
                    Log.w(TAG_DEBUG, "LoadCategoriesMapTask: doInBackground - getAllCategories returned null.");
                }
            } catch (Exception e) {
                Log.e(TAG_DEBUG, "LoadCategoriesMapTask: doInBackground - Error querying categories", e);
            }
            return map;
        }

        @Override
        protected void onPostExecute(Map<Integer, String> loadedMap) {
            Log.d(TAG_DEBUG, "LoadCategoriesMapTask: onPostExecute - Category map loaded. Size: " + loadedMap.size());
            categoryNameMap = loadedMap; // Cập nhật map thành viên của Activity

            // Cập nhật map cho các adapter
            if (incomeDetailAdapter != null) {
                incomeDetailAdapter.setCategoryMap(categoryNameMap);
                Log.d(TAG_DEBUG, "LoadCategoriesMapTask: Updated incomeDetailAdapter's map.");
            }
            if (expenseDetailAdapter != null) {
                expenseDetailAdapter.setCategoryMap(categoryNameMap);
                Log.d(TAG_DEBUG, "LoadCategoriesMapTask: Updated expenseDetailAdapter's map.");
            }

            // Sau khi có map, bắt đầu tải giao dịch cho tháng hiện tại
            Log.d(TAG_DEBUG, "LoadCategoriesMapTask: Triggering initial transaction load.");
            loadReportDataForSelectedMonth(); // Gọi tải giao dịch lần đầu
        }
    }
    // --- KẾT THÚC ASYNCTASK MỚI ---


    // processTransactions (đã xóa logic Top 3)
    private void processTransactions() {
        // ... (Giữ nguyên phần tính totalIncome, totalExpense, tách list income/expense) ...
        Log.d(TAG_DEBUG, "processTransactions: Started. currentMonthTransactions size: " + currentMonthTransactions.size());
        double totalIncome = 0;
        double totalExpense = 0;
        incomeTransactions.clear();
        expenseTransactions.clear();
        for (Transaction t : currentMonthTransactions) {
            if (t.isIncome()) {
                totalIncome += t.getAmount();
                incomeTransactions.add(t);
            } else {
                totalExpense += t.getAmount();
                expenseTransactions.add(t);
            }
        }
        Log.d(TAG_DEBUG, "processTransactions: TotalIncome: " + totalIncome + ", TotalExpense: " + totalExpense);
        Log.d(TAG_DEBUG, "processTransactions: incomeTransactions size: " + incomeTransactions.size());
        Log.d(TAG_DEBUG, "processTransactions: expenseTransactions size: " + expenseTransactions.size());
        tvTotalIncome.setText(currencyFormatter.format(totalIncome));
        tvTotalExpense.setText(currencyFormatter.format(totalExpense));
        Collections.sort(incomeTransactions, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
        Collections.sort(expenseTransactions, (t1, t2) -> Long.compare(t2.getDate(), t1.getDate()));
        Log.d(TAG_DEBUG, "processTransactions: Finished (Top 3 logic removed).");
    }

    // updateUI (đã xóa logic Top 3)
    private void updateUI() {
        Log.d(TAG_DEBUG, "updateUI: Started. income: " + incomeTransactions.size() +
                ", expense: " + expenseTransactions.size());

        // Quan trọng: Đảm bảo map đã được set cho adapter trước khi gọi updateData
        // (Việc này đã được đảm bảo bởi luồng gọi từ LoadCategoriesMapTask)
        if (incomeDetailAdapter != null) incomeDetailAdapter.updateData(incomeTransactions);
        if (expenseDetailAdapter != null) expenseDetailAdapter.updateData(expenseTransactions);

        tvNoIncomeDetails.setVisibility(incomeTransactions.isEmpty() ? View.VISIBLE : View.GONE);
        rvIncomeDetails.setVisibility(incomeTransactions.isEmpty() ? View.GONE : View.VISIBLE);

        tvNoExpenseDetails.setVisibility(expenseTransactions.isEmpty() ? View.VISIBLE : View.GONE);
        rvExpenseDetails.setVisibility(expenseTransactions.isEmpty() ? View.GONE : View.VISIBLE);

        if (currentMonthTransactions.isEmpty()){
            Toast.makeText(this, "Không có giao dịch nào trong tháng này.", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DEBUG, "updateUI: No transactions for this month toast shown.");
        }
        Log.d(TAG_DEBUG, "updateUI: Finished (Top 3 UI logic removed).");
    }

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