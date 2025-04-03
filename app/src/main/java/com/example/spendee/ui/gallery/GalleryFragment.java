package com.example.spendee.ui.gallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spendee.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private EditText edtName, edtPhone, edtAddress;
    private TextView tvUsername;
    private Button btnSave, btnChangePassword;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Ánh xạ các EditText và TextView
        edtName = binding.edtName;
        edtPhone = binding.edtPhone;
        edtAddress = binding.edtAddress;
        tvUsername = binding.tvUsername;
        btnSave = binding.btnSave;
        btnChangePassword = binding.btnChangePassword;

        // Lấy thông tin người dùng từ SharedPreferences
        loadUserInfo();

        // Xử lý sự kiện click nút Lưu thay đổi
        btnSave.setOnClickListener(v -> saveUserInfo());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void loadUserInfo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSpendee", android.content.Context.MODE_PRIVATE);

        // Giả sử bạn đã lưu index của người dùng đang đăng nhập
        int currentUserIndex = prefs.getInt("current_user_index", -1); // -1 là giá trị mặc định nếu không tìm thấy

        if (currentUserIndex != -1) {
            String name = prefs.getString("name_" + currentUserIndex, "");
            String username = prefs.getString("username_" + currentUserIndex, "");
            String phone = prefs.getString("phone_" + currentUserIndex, "");
            String address = prefs.getString("address_" + currentUserIndex, "");

            edtName.setText(name);
            tvUsername.setText(username);
            edtPhone.setText(phone);
            edtAddress.setText(address);
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            // Có thể điều hướng người dùng trở lại màn hình đăng nhập hoặc thực hiện hành động khác
        }
    }

    private void saveUserInfo() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSpendee", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int currentUserIndex = prefs.getInt("current_user_index", -1);

        if (currentUserIndex != -1) {
            editor.putString("name_" + currentUserIndex, name);
            editor.putString("phone_" + currentUserIndex, phone);
            editor.putString("address_" + currentUserIndex, address);
            editor.apply();
            Toast.makeText(requireContext(), "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Không thể cập nhật thông tin", Toast.LENGTH_SHORT).show();
        }
    }
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi mật khẩu");

        // Tạo layout cho dialog
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);


        final EditText oldPasswordInput = new EditText(requireContext());
        oldPasswordInput.setHint("Mật khẩu cũ");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);

        final EditText newPasswordInput = new EditText(requireContext());
        newPasswordInput.setHint("Mật khẩu mới");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmNewPasswordInput = new EditText(requireContext());
        confirmNewPasswordInput.setHint("Xác nhận mật khẩu mới");
        confirmNewPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmNewPasswordInput);


        builder.setView(layout);

        builder.setPositiveButton("Đổi", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

            // Kiểm tra mật khẩu cũ và lưu mật khẩu mới
            changePassword(oldPassword, newPassword,confirmNewPassword);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserSpendee", android.content.Context.MODE_PRIVATE);
        int currentUserIndex = prefs.getInt("current_user_index", -1);

        if (currentUserIndex != -1) {
            String savedPassword = prefs.getString("password_" + currentUserIndex, "");

            if (oldPassword.equals(savedPassword)) {
                if(!newPassword.equals(confirmPassword)){
                    Toast.makeText(requireContext(), "Xác nhận mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
                } else if(newPassword.isEmpty() || confirmPassword.isEmpty() ){
                    Toast.makeText(requireContext(), "Không được bỏ trống các trường mật khẩu!", Toast.LENGTH_SHORT).show();
                }
                else{

                    // Lưu mật khẩu mới
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("password_" + currentUserIndex, newPassword);
                    editor.apply();
                    Toast.makeText(requireContext(), "Mật khẩu đã được thay đổi!", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(requireContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}