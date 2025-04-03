package com.example.spendee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Main_Screen2 extends AppCompatActivity {

    private EditText edtName, edtUser, edtPhone, edtPassword, edtAddress;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen2);

        // Điều chỉnh padding cho Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các thành phần giao diện
        edtName = findViewById(R.id.edtName);
        edtUser = findViewById(R.id.edtUser);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtAddress = findViewById(R.id.edtAddress);
        btnRegister = findViewById(R.id.btnRegister);

        // Khởi tạo last_user_index nếu chưa tồn tại
        SharedPreferences prefs = getSharedPreferences("UserSpendee", MODE_PRIVATE);
        if (!prefs.contains("last_user_index")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("last_user_index", 0);
            editor.apply();
        }

        // Xử lý sự kiện click nút đăng ký
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu thông tin vào SharedPreferences với index
        SharedPreferences prefs = getSharedPreferences("UserSpendee", MODE_PRIVATE);
        int nextIndex = prefs.getInt("last_user_index", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name_" + nextIndex, name);
        editor.putString("username_" + nextIndex, username);
        editor.putString("phone_" + nextIndex, phone);
        editor.putString("password_" + nextIndex, password);
        editor.putString("address_" + nextIndex, address);
        editor.putInt("last_user_index", nextIndex + 1);
        editor.apply();

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

        // Chuyển đến trang đăng nhập
        startActivity(new Intent(this, Main_Screen.class));
        finish();
    }
}
