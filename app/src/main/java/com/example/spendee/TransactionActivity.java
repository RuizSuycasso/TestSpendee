package com.example.spendee;

// Import cần thiết
import android.app.DatePickerDialog; // Cần cho chọn tháng/năm
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker; // Cần cho DatePickerDialog
import android.widget.EditText;
import android.widget.TextView; // Cần cho tvSelectedMonth
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Các import khác
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TransactionActivity extends AppCompatActivity {
    private BarChart barChart;
    private RecyclerView rcvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FloatingActionButton fabAdd;
    private AppDatabase db;
    private TextView tvSelectedMonth; // TextView hiển thị/chọn tháng/năm

    // Biến lưu tháng/năm đang chọn
    private int currentYear;
    private int currentMonth; // Tháng bắt đầu từ 0 (Calendar.JANUARY)

    // Lưu trữ nhãn ngày cho trục X của biểu đồ
    private final List<String> dateLabels = new ArrayList<>();
    // Format ngày cho nhãn trục X
    private final SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_giao_dich); // Dùng layout có tvSelectedMonth

        db = AppDatabase.getInstance(this);

        // Khởi tạo tháng/năm hiện tại
        Calendar cal = Calendar.getInstance();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);

        initViews();
        setupRecyclerView();
        setupBarChart();           // Cấu hình biểu đồ
        setupMonthSelector();      // Cài đặt click cho chọn tháng
        updateSelectedMonthText(); // Hiển thị tháng/năm ban đầu
        loadTransactionsForSelectedMonth(); // Tải dữ liệu cho tháng/năm ban đầu
        setupFabClick();
    }

    private void initViews() {
        barChart = findViewById(R.id.barChart);
        rcvTransactions = findViewById(R.id.rcv_user);
        fabAdd = findViewById(R.id.btnAddTransaction);
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth); // Ánh xạ TextView chọn tháng
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        rcvTransactions.setAdapter(transactionAdapter);
        rcvTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    // ---- Phần xử lý chọn tháng/năm ----
    private void setupMonthSelector() {
        tvSelectedMonth.setOnClickListener(v -> showMonthYearPicker());
    }

    private void updateSelectedMonthText() {
        // Định dạng hiển thị, ví dụ: "Tháng 07/2024"
        SimpleDateFormat monthYearDisplayFormat = new SimpleDateFormat("MM/yyyy", new Locale("vi", "VN"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, currentMonth);
        tvSelectedMonth.setText("Tháng " + monthYearDisplayFormat.format(cal.getTime()));
    }

    private void showMonthYearPicker() {
        // Sử dụng DatePickerDialog, nhưng chỉ quan tâm đến năm và tháng được chọn
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                // Listener được gọi khi người dùng nhấn OK
                (view, year, month, dayOfMonth) -> {
                    // Cập nhật năm và tháng đã chọn
                    currentYear = year;
                    currentMonth = month; // month trả về từ 0-11
                    // Cập nhật text hiển thị
                    updateSelectedMonthText();
                    // Tải lại dữ liệu cho tháng mới được chọn
                    loadTransactionsForSelectedMonth();
                },
                // Năm, tháng, ngày ban đầu hiển thị trên dialog
                currentYear, currentMonth, 1); // Ngày 1 chỉ là giá trị mặc định

        // Tùy chọn: Giới hạn chỉ chọn tháng/năm (có thể không hoạt động trên mọi thiết bị/theme)
        // Bạn có thể cần thư viện bên ngoài hoặc custom dialog để chỉ chọn tháng/năm tốt hơn
        /*
        try {
            Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
            for (Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);
                    Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (Field datePickerField : datePickerFields) {
                       if ("mDaySpinner".equals(datePickerField.getName()) || "mDayPicker".equals(datePickerField.getName())) {
                          datePickerField.setAccessible(true);
                          Object dayPicker = datePickerField.get(datePicker);
                          ((View) dayPicker).setVisibility(View.GONE);
                       }
                    }
                }
            }
        } catch (Exception ex) {
            // Handle exception if reflection fails
        }
        */

        datePickerDialog.show();
    }
    // ---- Kết thúc phần xử lý chọn tháng/năm ----


    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);

        // --- Trục X (Ngày trong tháng) ---
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-45);
        // Dùng IndexAxisValueFormatter cho nhãn ngày "dd/MM"
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));

        // --- Trục Y (Số tiền - không format) ---
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisMinimum(0f); // Bắt đầu từ 0
        leftAxis.setValueFormatter(null); // Không format tiền tệ

        barChart.getAxisRight().setEnabled(false);

        // --- Legend (Thu/Chi) ---
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP);
        barChart.getLegend().setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        barChart.getLegend().setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        barChart.getLegend().setDrawInside(false);

        // --- Tương tác ---
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
    }

    // Lớp nội bộ để lưu tổng Thu/Chi cho một ngày
    private static class DailySummary {
        double income = 0.0;
        double expense = 0.0;
    }

    // Phương thức xử lý và vẽ Grouped Bar Chart THEO NGÀY của tháng đã chọn
    private void processAndDrawDailyGroupedChart(List<Transaction> transactions) {
        // TreeMap để nhóm theo ngày và tự sắp xếp
        Map<Long, DailySummary> dailySummaryMap = new TreeMap<>();
        Calendar cal = Calendar.getInstance();

        // 1. Tính tổng Thu và Chi riêng cho mỗi ngày
        for (Transaction t : transactions) {
            cal.setTimeInMillis(t.getDate());
            cal.set(Calendar.HOUR_OF_DAY, 0); // Reset giờ phút giây
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

        // 2. Chuẩn bị dữ liệu cho biểu đồ nhóm
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        dateLabels.clear(); // Xóa nhãn ngày cũ

        int index = 0;
        for (Map.Entry<Long, DailySummary> entry : dailySummaryMap.entrySet()) {
            long dayTimestamp = entry.getKey();
            DailySummary summary = entry.getValue();

            incomeEntries.add(new BarEntry(index, (float) summary.income));
            expenseEntries.add(new BarEntry(index, (float) summary.expense));

            // Tạo nhãn ngày "dd/MM"
            dateLabels.add(dayMonthFormat.format(new Date(dayTimestamp)));
            index++;
        }

        // 3. Cập nhật và vẽ biểu đồ
        if (incomeEntries.isEmpty() && expenseEntries.isEmpty()) {
            barChart.clear(); // Xóa nếu không có dữ liệu cho tháng
            barChart.setNoDataText("Không có giao dịch cho tháng này"); // Hiển thị thông báo
            barChart.invalidate();
            return;
        }

        // Tạo DataSet Thu (Màu xanh)
        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Thu");
        incomeDataSet.setColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        incomeDataSet.setDrawValues(false);

        // Tạo DataSet Chi (Màu đỏ)
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Chi");
        expenseDataSet.setColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        expenseDataSet.setDrawValues(false);

        // Cấu hình cho Grouped Bar Chart
        float groupSpace = 0.1f;
        float barSpace = 0.05f;
        float barWidth = 0.4f; // (groupSpace + (barSpace + barWidth) * 2 = 1.0)

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        data.setBarWidth(barWidth);

        // Cập nhật formatter trục X với nhãn ngày mới
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        barChart.getXAxis().setLabelCount(dateLabels.size()); // Đặt số lượng nhãn khớp với số ngày

        // Tính toán lại giới hạn trục X
        if (!dateLabels.isEmpty()) {
            barChart.getXAxis().setAxisMinimum(-0.5f);
            barChart.getXAxis().setAxisMaximum(dateLabels.size() - 0.5f);
        } else {
            barChart.getXAxis().setAxisMinimum(0f);
            barChart.getXAxis().setAxisMaximum(0f);
        }

        barChart.setData(data);

        // Group các cột lại với nhau
        if (!dateLabels.isEmpty()) {
            barChart.groupBars(-0.5f, groupSpace, barSpace);
        }

        barChart.invalidate();
        barChart.animateY(1000);

        // Điều chỉnh view nếu nhiều ngày
        if (dateLabels.size() > 7) { // Ví dụ: Hiển thị 7 ngày, cho phép cuộn xem thêm
            barChart.setVisibleXRangeMaximum(7.5f);
            // barChart.moveViewToX(dateLabels.size() - 1); // Có thể không cần di chuyển đến cuối
            barChart.moveViewToX(0); // Hiển thị từ đầu tháng
        } else {
            barChart.fitScreen(); // Hiển thị hết nếu ít ngày
        }
    }

    // --- Các phương thức tải dữ liệu và thêm giao dịch ---

    private void loadTransactionsForSelectedMonth() {
        // Gọi AsyncTask tải dữ liệu cho tháng đang chọn
        new LoadTransactionsTask(currentYear, currentMonth).execute();
    }

    // AsyncTask để tải giao dịch cho tháng được chọn
    private class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        private final int year;
        private final int month; // 0-11

        LoadTransactionsTask(int year, int month) {
            this.year = year;
            this.month = month;
        }

        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            Calendar calStart = Calendar.getInstance();
            // Đặt ngày về đầu tháng được chọn
            calStart.set(year, month, 1, 0, 0, 0);
            calStart.set(Calendar.MILLISECOND, 0);
            long startTime = calStart.getTimeInMillis();

            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(startTime); // Bắt đầu từ đầu tháng
            // Đặt ngày về đầu tháng sau -> endTime là đầu tháng sau (không bao gồm)
            calEnd.add(Calendar.MONTH, 1);
            long endTime = calEnd.getTimeInMillis();

            // Gọi DAO để lấy dữ liệu trong khoảng thời gian
            return db.transactionDAO().getTransactionsForPeriod(startTime, endTime);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            // Cập nhật RecyclerView và Biểu đồ
            if (transactions != null) {
                transactionList.clear();
                List<Transaction> displayList = new ArrayList<>(transactions);
                Collections.reverse(displayList); // Hiển thị mới nhất trước trong list
                transactionList.addAll(displayList);
                transactionAdapter.notifyDataSetChanged();

                // Xử lý và vẽ biểu đồ nhóm THEO NGÀY
                processAndDrawDailyGroupedChart(transactions);
            }
            // Không cần hiển thị Toast nếu không có giao dịch, biểu đồ sẽ tự hiện text
        }
    }

    // InsertTransactionTask gọi loadTransactionsForSelectedMonth()
    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            if (transactions.length > 0 && transactions[0] != null) {
                try {
                    db.transactionDAO().insert(transactions[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            // Tải lại dữ liệu cho tháng hiện tại đang xem sau khi thêm
            loadTransactionsForSelectedMonth();
        }
    }

    // --- Các hàm thêm giao dịch giữ nguyên ---
    private void setupFabClick() {
        fabAdd.setOnClickListener(v -> showTransactionDialog());
    }
    private void showTransactionDialog() {
        Dialog dialog = new Dialog(this);
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
    private void showAmountDialog(boolean isIncome) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_amount);
        EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        EditText edtDescription = dialog.findViewById(R.id.edtDescription);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            String description = edtDescription.getText().toString().trim();
            if (description.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(this, "Vui lòng nhập số tiền lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Thêm giao dịch mới với thời gian hiện tại
                    Transaction transaction = new Transaction(amount, isIncome, description);
                    new InsertTransactionTask().execute(transaction);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    // --- Kết thúc phần giữ nguyên ---
}