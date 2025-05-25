package com.example.spendee;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";

    private BarChart barChart;
    private FloatingActionButton fabAdd;
    private AppDatabase db;
    private TextView tvSelectedMonth;

    private int currentYear;
    private int currentMonth;

    private final List<String> dateLabels = new ArrayList<>();
    private final SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

    private List<Category> currentCategoryList;
    private ArrayAdapter<Category> categoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_giao_dich);
        Log.d(TAG, "onCreate: Starting");

        db = AppDatabase.getInstance(this);

        Calendar cal = Calendar.getInstance();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);

        initViews();
        setupBarChart();
        setupMonthSelector();
        updateSelectedMonthText();
        loadTransactionsForSelectedMonth();
        setupFabClick();

        currentCategoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentCategoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Log.d(TAG, "onCreate: Category adapter initialized");
    }

    private void initViews() {
        barChart = findViewById(R.id.barChart);
        fabAdd = findViewById(R.id.btnAddTransaction);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth);
    }

    private void setupMonthSelector() {
        tvSelectedMonth.setOnClickListener(v -> showMonthYearPicker());
    }

    private void updateSelectedMonthText() {
        SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MM/yyyy", new Locale("vi", "VN"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        tvSelectedMonth.setText("Tháng " + monthYearDisplayFormat.format(cal.getTime()));
    }

    private void showMonthYearPicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    currentYear = year;
                    currentMonth = month;
                    updateSelectedMonthText();
                    loadTransactionsForSelectedMonth();
                },
                currentYear, currentMonth, 1);
        datePickerDialog.show();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(null);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP);
        barChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        barChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        barChart.getLegend().setDrawInside(false);

        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
    }

    private static class DailySummary {
        double income = 0.0;
        double expense = 0.0;
    }

    private void processAndDrawDailyGroupedChart(List<Transaction> transactions) {
        Map<Long, DailySummary> dailySummaryMap = new TreeMap<>();
        Calendar cal = Calendar.getInstance();

        if (transactions == null) return;

        for (Transaction t : transactions) {
            if (t == null) continue;
            cal.setTimeInMillis(t.getDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayTimestamp = cal.getTimeInMillis();

            DailySummary summary = dailySummaryMap.computeIfAbsent(dayTimestamp, k -> new DailySummary());
            if (t.isIncome()) {
                summary.income += t.getAmount();
            } else {
                summary.expense += t.getAmount();
            }
        }

        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        dateLabels.clear();

        int index = 0;
        for (Map.Entry<Long, DailySummary> entry : dailySummaryMap.entrySet()) {
            long dayTimestamp = entry.getKey();
            DailySummary summary = entry.getValue();

            incomeEntries.add(new BarEntry(index, (float) summary.income));
            expenseEntries.add(new BarEntry(index, (float) summary.expense));
            dateLabels.add(dayMonthFormat.format(new Date(dayTimestamp)));
            index++;
        }

        if (incomeEntries.isEmpty() && expenseEntries.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Không có giao dịch cho tháng này");
            barChart.invalidate();
            return;
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Thu");
        incomeDataSet.setColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        incomeDataSet.setDrawValues(false);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Chi");
        expenseDataSet.setColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        expenseDataSet.setDrawValues(false);

        float groupSpace = 0.1f;
        float barSpace = 0.05f;
        float barWidth = 0.4f;

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        data.setBarWidth(barWidth);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        barChart.getXAxis().setLabelCount(dateLabels.size());

        if (!dateLabels.isEmpty()) {
            barChart.getXAxis().setAxisMinimum(-0.5f);
            barChart.getXAxis().setAxisMaximum(dateLabels.size() - 0.5f);
        } else {
            barChart.getXAxis().setAxisMinimum(0f);
            barChart.getXAxis().setAxisMaximum(0f);
        }

        barChart.setData(data);
        if (!dateLabels.isEmpty()) {
            barChart.groupBars(-0.5f, groupSpace, barSpace);
        }
        barChart.invalidate();
        barChart.animateY(1000);

        if (dateLabels.size() > 7) {
            barChart.setVisibleXRangeMaximum(7.5f);
            barChart.moveViewToX(0);
        } else {
            barChart.fitScreen();
        }
    }


    private void loadTransactionsForSelectedMonth() {
        Log.d(TAG, "loadTransactionsForSelectedMonth: Called for Year: " + currentYear + ", Month: " + currentMonth);
        new LoadTransactionsTask(currentYear, currentMonth).execute();
    }

    private class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        private final int year;
        private final int month;

        LoadTransactionsTask(int year, int month) {
            this.year = year;
            this.month = month;
        }

        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            Log.d(TAG, "LoadTransactionsTask: doInBackground - Started");
            Calendar calStart = Calendar.getInstance();
            calStart.set(year, month, 1, 0, 0, 0);
            calStart.set(Calendar.MILLISECOND, 0);
            long startTime = calStart.getTimeInMillis();
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(startTime);
            calEnd.add(Calendar.MONTH, 1);
            long endTime = calEnd.getTimeInMillis();
            Log.d(TAG, "LoadTransactionsTask: doInBackground - Querying from " + startTime + " to " + endTime);
            try {
                return db.transactionDAO().getTransactionsForPeriod(startTime, endTime);
            } catch (Exception e) {
                Log.e(TAG, "LoadTransactionsTask: doInBackground - Error querying transactions", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            Log.d(TAG, "LoadTransactionsTask: onPostExecute - Received " + (transactions != null ? transactions.size() : "null") + " transactions.");
            if (transactions != null) {
                processAndDrawDailyGroupedChart(transactions);
            } else {
                barChart.clear();
                barChart.setNoDataText("Lỗi tải giao dịch");
                barChart.invalidate();
                Toast.makeText(TransactionActivity.this, "Lỗi khi tải giao dịch", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            if (transactions.length > 0 && transactions[0] != null) {
                try {
                    Log.d(TAG, "InsertTransactionTask: Inserting transaction with categoryId: " + transactions[0].getCategoryId());
                    db.transactionDAO().insert(transactions[0]);
                    Log.d(TAG, "InsertTransactionTask: Insertion successful.");
                } catch (Exception e) {
                    Log.e(TAG, "InsertTransactionTask: Error inserting transaction", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Log.d(TAG, "InsertTransactionTask: onPostExecute - Reloading transactions.");
            loadTransactionsForSelectedMonth();
        }
    }

    private void setupFabClick() {
        fabAdd.setOnClickListener(v -> showTransactionDialog());
    }

    private void showTransactionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_transaction_type);
        Button btnIncome = dialog.findViewById(R.id.btnIncome);
        Button btnExpense = dialog.findViewById(R.id.btnExpense);
        btnIncome.setOnClickListener(v -> {
            showAmountDialog(true);
            dialog.dismiss();
        });
        btnExpense.setOnClickListener(v -> {
            showAmountDialog(false);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showAmountDialog(final boolean isIncome) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_amount);

        final EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        final Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

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
                Log.d(TAG, "afterTextChanged: Clean string after removing non-digits/comma: " + cleanString);

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
                    Log.d(TAG, "afterTextChanged: Parsed double value: " + parsed);
                } catch (ParseException e) {
                    Log.e(TAG, "afterTextChanged: Failed to parse number: " + cleanString, e);
                    current = "";
                    edtAmount.setText("");
                    edtAmount.addTextChangedListener(this);
                    Toast.makeText(TransactionActivity.this, "Định dạng số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                String formatted = formatter.format(parsed);
                Log.d(TAG, "afterTextChanged: Formatted string: " + formatted);

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

        spinnerCategory.setAdapter(categoryAdapter);
        Log.d(TAG, "showAmountDialog: Spinner adapter set.");

        new LoadCategoriesTask(isIncome).execute();

        btnConfirm.setOnClickListener(v -> {
            Log.d(TAG, "showAmountDialog: Confirm button clicked.");
            String amountStr = edtAmount.getText().toString();

            final DecimalFormatSymbols confirmSymbols = new DecimalFormatSymbols(new Locale("vi", "VN"));

            String groupingSeparator = String.valueOf(confirmSymbols.getGroupingSeparator());
            String groupingSeparatorRegex = Pattern.quote(groupingSeparator);
            String cleanAmountStrForParsing = amountStr.replaceAll(groupingSeparatorRegex, "");
            Log.d(TAG, "showAmountDialog: After removing grouping separator: " + cleanAmountStrForParsing);

            cleanAmountStrForParsing = cleanAmountStrForParsing.replace(confirmSymbols.getDecimalSeparator(), '.');
            Log.d(TAG, "showAmountDialog: After replacing decimal separator: " + cleanAmountStrForParsing);

            Log.d(TAG, "showAmountDialog: Final cleaned amount string for parsing: " + cleanAmountStrForParsing);

            if (cleanAmountStrForParsing.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer selectedCategoryId = null; // Khai báo ở đây

            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

            if (selectedCategory == null) {
                Toast.makeText(this, "Vui lòng chọn một danh mục", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "showAmountDialog: No category selected.");
                return;
            } else {
                selectedCategoryId = selectedCategory.getId(); // Gán giá trị
                Log.d(TAG, "showAmountDialog: Selected Category: ID=" + selectedCategoryId + ", Name=" + selectedCategory.getName());
            }


            try {
                double amount = Double.parseDouble(cleanAmountStrForParsing);

                if (amount <= 0) {
                    Toast.makeText(this, "Vui lòng nhập số tiền lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Transaction transaction = new Transaction(amount, isIncome, null, selectedCategoryId);
                new InsertTransactionTask().execute(transaction);
                Log.d(TAG, "showAmountDialog: Transaction object created and InsertTask executed.");
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Log.e(TAG, "showAmountDialog: Invalid amount format during parse (after cleaning)", e);
                Toast.makeText(this, "Số tiền nhập vào không hợp lệ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "showAmountDialog: Error saving transaction", e);
                Toast.makeText(this, "Lỗi khi lưu giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
        Log.d(TAG, "showAmountDialog: Dialog shown for isIncome=" + isIncome);
    }


    private class LoadCategoriesTask extends AsyncTask<Void, Void, List<Category>> {
        private final boolean isIncomeType;

        LoadCategoriesTask(boolean isIncomeType) {
            this.isIncomeType = isIncomeType;
            Log.d(TAG, "LoadCategoriesTask: Initialized for isIncomeType=" + isIncomeType);
        }

        @Override
        protected List<Category> doInBackground(Void... voids) {
            Log.d(TAG, "LoadCategoriesTask: doInBackground - Started");
            try {
                return db.categoryDAO().getCategoriesByType(isIncomeType);
            } catch (Exception e) {
                Log.e(TAG, "LoadCategoriesTask: doInBackground - Error querying categories", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Category> categories) {
            Log.d(TAG, "LoadCategoriesTask: onPostExecute - Received " + (categories != null ? categories.size() : "null") + " categories.");
            if (categories != null) {
                currentCategoryList.clear();
                currentCategoryList.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
                Log.d(TAG, "LoadCategoriesTask: Category adapter updated and notified.");
                if (categories.isEmpty()) {
                    Toast.makeText(TransactionActivity.this, "Không có danh mục nào cho loại giao dịch này.", Toast.LENGTH_SHORT).show();
                }
            } else {
                currentCategoryList.clear();
                categoryAdapter.notifyDataSetChanged();
                Toast.makeText(TransactionActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "LoadCategoriesTask: Failed to load categories.");
            }
        }
    }
}