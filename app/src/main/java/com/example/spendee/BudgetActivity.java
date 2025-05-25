package com.example.spendee;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.util.regex.Pattern;


public class BudgetActivity extends AppCompatActivity implements BudgetAdapter.OnBudgetActionListener {

    private static final String TAG = "BudgetActivity";

    private Toolbar toolbar;
    private RecyclerView rvBudgets;
    private TextView tvNoBudgets, tvSelectedMonthYearBudget;
    private Button btnChangeMonthYearBudget;
    private FloatingActionButton fabAddBudget;

    private View warningBudgetExceededBox;
    private TextView tvWarningMessage;


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

        warningBudgetExceededBox = findViewById(R.id.warning_budget_box);
        if (warningBudgetExceededBox != null) {
            tvWarningMessage = warningBudgetExceededBox.findViewById(R.id.tv_warning_message);
            if (tvWarningMessage == null) {
                Log.e(TAG, "TextView tv_warning_message not found inside included layout!");
            }
        } else {
            Log.e(TAG, "Included layout with ID warning_budget_box not found!");
        }


        budgetAdapter = new BudgetAdapter(this, currentBudgetList, this);
        rvBudgets.setAdapter(budgetAdapter);

        categorySpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, expenseCategoryList);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Calendar calendar = Calendar.getInstance();
        currentSelectedYear = calendar.get(Calendar.YEAR);
        currentSelectedMonth = calendar.get(Calendar.MONTH);
        updateSelectedMonthYearDisplay();

        loadRequiredData();

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
            Log.e(TAG, "showMonthYearPickerDialog: monthPicker or yearPicker is null.");
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
            loadRequiredData();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void loadRequiredData() {
        Log.d(TAG, "loadRequiredData: Loading categories and budgets for " + (currentSelectedMonth+1) + "/" + currentSelectedYear);
        new LoadBudgetDataTask(currentSelectedYear, currentSelectedMonth).execute();
    }

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
        }

        @Override
        protected List<Budget> doInBackground(Void... voids) {
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
                } else {
                    Log.w(TAG, "LoadBudgetDataTask: No categories found.");
                }

                budgetsForMonth = db.budgetDAO().getBudgetsForMonth(year, month);
                Log.d(TAG, "LoadBudgetDataTask: Found " + (budgetsForMonth != null ? budgetsForMonth.size() : 0) + " budgets for " + (month+1) + "/" + year);

                Calendar calStart = Calendar.getInstance();
                calStart.set(year, month, 1, 0, 0, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                long startTime = calStart.getTimeInMillis();

                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(startTime);
                calEnd.add(Calendar.MONTH, 1);
                calEnd.add(Calendar.MILLISECOND, -1);
                long endTime = calEnd.getTimeInMillis();

                Log.d(TAG, "LoadBudgetDataTask: Time range for transactions: " + new Date(startTime) + " to " + new Date(calEnd.getTimeInMillis()));


                if (budgetsForMonth != null) {
                    for (Budget budget : budgetsForMonth) {
                        budget.setCategoryName(localCategoryMap.getOrDefault(budget.getCategoryId(), "Không rõ"));
                        Double spent = db.transactionDAO().getSumOfExpensesForCategoryInPeriod(budget.getCategoryId(), startTime, endTime);
                        budget.setSpentAmount(spent != null ? spent : 0.0);
                        Log.d(TAG, "LoadBudgetDataTask: Category ID " + budget.getCategoryId() + " spent: " + budget.getSpentAmount() + ", Limit: " + budget.getLimitAmount());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "LoadBudgetDataTask: Error loading data", e);
                return null;
            }
            return budgetsForMonth;
        }


        @Override
        protected void onPostExecute(List<Budget> loadedBudgets) {
            Log.d(TAG, "LoadBudgetDataTask: onPostExecute");

            expenseCategoryList.clear();
            expenseCategoryList.addAll(localExpenseCategories);
            categorySpinnerAdapter.notifyDataSetChanged();

            categoryNameMap = localCategoryMap;

            if (loadedBudgets != null) {
                currentBudgetList.clear();
                currentBudgetList.addAll(loadedBudgets);

                boolean anyBudgetExceeded = false;
                StringBuilder exceededCategoriesMessage = new StringBuilder();
                int exceededCount = 0;

                for (Budget budget : currentBudgetList) {
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

                if (currentBudgetList.isEmpty()) {
                    tvNoBudgets.setVisibility(View.VISIBLE);
                    rvBudgets.setVisibility(View.GONE);
                } else {
                    tvNoBudgets.setVisibility(View.GONE);
                    rvBudgets.setVisibility(View.VISIBLE);
                }

                if (warningBudgetExceededBox != null && tvWarningMessage != null) {
                    if (anyBudgetExceeded) {
                        tvWarningMessage.setText("⚠️ Đã vượt ngân sách ở các danh mục: " + exceededCategoriesMessage.toString());
                        warningBudgetExceededBox.setVisibility(View.VISIBLE);
                    } else {
                        warningBudgetExceededBox.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Warning box views are null, cannot show/hide warning.");
                }


            } else {
                Toast.makeText(BudgetActivity.this, "Lỗi khi tải dữ liệu ngân sách", Toast.LENGTH_SHORT).show();
                tvNoBudgets.setVisibility(View.VISIBLE);
                rvBudgets.setVisibility(View.GONE);
                currentBudgetList.clear();
                budgetAdapter.updateData(currentBudgetList);
                if (warningBudgetExceededBox != null) {
                    warningBudgetExceededBox.setVisibility(View.GONE);
                }
            }
        }
    }


    private void showAddEditBudgetDialog(final Budget existingBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_budget, null);
        builder.setView(dialogView);

        final Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_budget_category);
        final EditText edtAmount = dialogView.findViewById(R.id.edt_budget_amount);
        final Button btnSave = dialogView.findViewById(R.id.btn_save_budget);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel_budget);
        final TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_budget_title);

        // *** START Thêm TextWatcher để định dạng số tiền ***
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        final DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);

        edtAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(current)) {
                    return;
                }

                edtAmount.removeTextChangedListener(this);

                int originalSelectionStart = edtAmount.getSelectionStart();

                String cleanString = s.toString().replaceAll("[^0-9,]", "");
                // Log.d(TAG, "afterTextChanged: Clean string after removing non-digits/comma: " + cleanString);

                if (cleanString.isEmpty()) {
                    current = "";
                    edtAmount.setText("");
                    edtAmount.addTextChangedListener(this);
                    return;
                }

                if (cleanString.equals(",")) {
                    cleanString = "0,";
                } else if (cleanString.startsWith(",")) {
                    cleanString = "0" + cleanString;
                }

                int firstComma = cleanString.indexOf(',');
                if (firstComma != -1) {
                    String integerPart = cleanString.substring(0, firstComma);
                    String decimalPart = cleanString.substring(firstComma + 1).replace(",", "");
                    cleanString = integerPart + (decimalPart.isEmpty() ? "" : "," + decimalPart);
                }

                double parsed = 0;
                try {
                    parsed = formatter.parse(cleanString).doubleValue();
                    // Log.d(TAG, "afterTextChanged: Parsed double value: " + parsed);
                } catch (ParseException e) {
                    Log.e(TAG, "afterTextChanged: Failed to parse number: " + cleanString, e);
                    current = "";
                    edtAmount.setText("");
                    edtAmount.addTextChangedListener(this);
                    // Toast.makeText(BudgetActivity.this, "Định dạng số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                String formatted = formatter.format(parsed);
                // Log.d(TAG, "afterTextChanged: Formatted string: " + formatted);

                current = formatted;
                edtAmount.setText(formatted);

                try {
                    String originalString = s.toString();
                    String originalCleanBeforeCursor = "";
                    if (originalSelectionStart > 0) {
                        originalCleanBeforeCursor = originalString.substring(0, originalSelectionStart).replaceAll("[^0-9,]", "");
                    }

                    int newCursorPosition = 0;
                    int cleanCharsMatched = 0;

                    for (int i = 0; i < formatted.length(); i++) {
                        char c = formatted.charAt(i);
                        if (Character.isDigit(c) || c == symbols.getDecimalSeparator()) {
                            if (cleanCharsMatched < originalCleanBeforeCursor.length()) {
                                cleanCharsMatched++;
                                newCursorPosition = i + 1;
                            } else {
                                newCursorPosition = i;
                                break;
                            }
                        } else {
                            newCursorPosition = i + 1;
                        }
                        if (i == formatted.length() - 1 && cleanCharsMatched < originalCleanBeforeCursor.length()) {
                            newCursorPosition = formatted.length();
                        }
                    }

                    if (originalCleanBeforeCursor.isEmpty()) {
                        newCursorPosition = 0;
                    }
                    if (originalSelectionStart >= originalString.length()) {
                        newCursorPosition = formatted.length();
                    }

                    newCursorPosition = Math.max(0, newCursorPosition);
                    newCursorPosition = Math.min(newCursorPosition, formatted.length());

                    edtAmount.setSelection(newCursorPosition);

                } catch (Exception e) {
                    Log.e(TAG, "Error calculating cursor position", e);
                    edtAmount.setSelection(formatted.length());
                }

                edtAmount.addTextChangedListener(this);
            }
        });
        // *** END TextWatcher ***


        spinnerCategory.setAdapter(categorySpinnerAdapter);

        if (existingBudget != null) {
            tvTitle.setText("Sửa Ngân Sách");
            // *** Sửa đổi: Định dạng số tiền hạn mức khi hiển thị để sửa ***
            edtAmount.setText(formatter.format(existingBudget.getLimitAmount()));
            // *** End Sửa đổi ***

            for (int i = 0; i < expenseCategoryList.size(); i++) {
                if (expenseCategoryList.get(i).getId() == existingBudget.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            spinnerCategory.setEnabled(false);
        } else {
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

            // *** START Làm sạch chuỗi đã định dạng trước khi parse cho lưu DB ***
            final DecimalFormatSymbols confirmSymbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
            String groupingSeparator = String.valueOf(confirmSymbols.getGroupingSeparator());
            String groupingSeparatorRegex = Pattern.quote(groupingSeparator);
            String cleanAmountStrForParsing = amountStr.replaceAll(groupingSeparatorRegex, "");

            cleanAmountStrForParsing = cleanAmountStrForParsing.replace(confirmSymbols.getDecimalSeparator(), '.');
            // *** END Làm sạch chuỗi ***


            try {
                // *** START Parse chuỗi đã làm sạch thành double ***
                double amount = Double.parseDouble(cleanAmountStrForParsing);
                // *** END Parse ***

                if (amount < 0) {
                    Toast.makeText(this, "Hạn mức không thể âm", Toast.LENGTH_SHORT).show();
                    return;
                }

                Budget budgetToSave;
                if (existingBudget != null) {
                    budgetToSave = existingBudget;
                    budgetToSave.setLimitAmount(amount);
                } else {
                    budgetToSave = new Budget(selectedCategory.getId(), amount, currentSelectedYear, currentSelectedMonth);
                }

                new SaveBudgetTask().execute(budgetToSave);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Log.e(TAG, "showAddEditBudgetDialog: Invalid amount format during parse", e);
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "showAddEditBudgetDialog: Error saving budget", e);
                Toast.makeText(this, "Lỗi khi lưu ngân sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private class SaveBudgetTask extends AsyncTask<Budget, Void, Void> {
        @Override
        protected Void doInBackground(Budget... budgets) {
            if (budgets.length > 0 && budgets[0] != null) {
                try {
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
            loadRequiredData();
            Toast.makeText(BudgetActivity.this, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
        }
    }


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
            loadRequiredData();
            Toast.makeText(BudgetActivity.this, "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(Budget budget) {
        Log.d(TAG, "onEditClick: Editing budget ID " + budget.getId());
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}