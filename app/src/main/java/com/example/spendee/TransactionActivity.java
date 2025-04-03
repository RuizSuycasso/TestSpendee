package com.example.spendee;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {
    private BarChart barChart;
    private RecyclerView rcvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_giao_dich);

        initViews();
        setupRecyclerView();
        setupBarChart();
        loadTransactions();
        setupFabClick();
    }

    private void initViews() {
        barChart = findViewById(R.id.barChart);
        rcvTransactions = findViewById(R.id.rcv_user);
        fabAdd = findViewById(R.id.btnAddTransaction);
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);
        rcvTransactions.setAdapter(transactionAdapter);
        rcvTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        updateBarChart();
    }

    private void updateBarChart() {
        new AsyncTask<Void, Void, double[]>() {
            @Override
            protected double[] doInBackground(Void... voids) {
                TransactionDAO dao = TransactionDatabase.getInstance(TransactionActivity.this).transactionDAO();
                return new double[]{dao.getTotalIncome(), dao.getTotalExpense()};
            }

            @Override
            protected void onPostExecute(double[] totals) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                float income = 0f;
                float expense = 0f;

                if (totals != null) {
                    if (totals.length > 0) {
                        income = (float) totals [0];
                    }
                    if (totals.length > 1) {
                        expense = (float) totals [1];
                    }
                }

                entries.add(new BarEntry(0, income)); // Income
                entries.add(new BarEntry(1, expense)); // Expense

                BarDataSet dataSet = new BarDataSet(entries, "Thu - Chi");
                dataSet.setColors(
                        getColor(android.R.color.holo_green_light),
                        getColor(android.R.color.holo_red_light)
                );

                BarData data = new BarData(dataSet);
                barChart.setData(data);
                barChart.invalidate();
            }
        }.execute();
    }

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
        EditText edtDescription = dialog.findViewById(R.id.edtDescription); // Tìm EditText description
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            String description = edtDescription.getText().toString().trim(); // Lấy description

            if (!amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    Transaction transaction = new Transaction(amount, isIncome, description); // Gọi constructor mới
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

    private void loadTransactions() {
        new LoadTransactionsTask().execute();
    }

    private class InsertTransactionTask extends AsyncTask<Transaction, Void, Void> {
        @Override
        protected Void doInBackground(Transaction... transactions) {
            TransactionDatabase.getInstance(TransactionActivity.this)
                    .transactionDAO().insert(transactions [0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            loadTransactions();
            updateBarChart();
        }
    }

    private class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            return TransactionDatabase.getInstance(TransactionActivity.this)
                    .transactionDAO().getAllTransactions();
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            transactionList.clear();
            transactionList.addAll(transactions);
            transactionAdapter.notifyDataSetChanged();
        }
    }
}