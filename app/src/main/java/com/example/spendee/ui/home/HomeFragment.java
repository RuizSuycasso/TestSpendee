package com.example.spendee.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // << THÊM IMPORT CHO Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // << THÊM IMPORT CHO Toast (tùy chọn, nếu muốn hiển thị lỗi cho người dùng)

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spendee.BudgetActivity;
import com.example.spendee.databinding.FragmentHomeBinding;
import com.example.spendee.TransactionActivity;
import com.example.spendee.ReportActivity;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment"; // << THÊM TAG CHO LOGGING
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        homeViewModel.getText().observe(getViewLifecycleOwner(), binding.textHome::setText);


        if (binding.btn1 != null) {
            binding.btn1.setOnClickListener(v -> {
                Log.d(TAG, "btn1 (Thêm giao dịch) clicked");
                Intent intent = new Intent(requireContext(), TransactionActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "binding.btn1 is null!");
        }

        if (binding.btn2 != null) {
            binding.btn2.setOnClickListener(v -> {
                Log.d(TAG, "btn2 (Xem báo cáo) clicked");
                Intent intentToReport = new Intent(requireContext(), ReportActivity.class);
                startActivity(intentToReport);
            });
        } else {
            Log.e(TAG, "binding.btn2 is null! Cannot start ReportActivity.");
        }

        if (binding.btn4 != null) {
            binding.btn4.setOnClickListener(v -> {
                Log.d(TAG, "btn4 (Quản lý chi tiêu) clicked");
                Intent intentToReport = new Intent(requireContext(), BudgetActivity.class);
                startActivity(intentToReport);
            });
        } else {
            Log.e(TAG, "binding.btn4 is null! Cannot start ReportActivity.");

        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        binding = null;
    }
}