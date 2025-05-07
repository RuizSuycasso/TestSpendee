package com.example.spendee; // Thay đổi package nếu cần

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
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
    private AppDatabase db; // Sử dụng AppDatabase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen2);

        db = AppDatabase.getInstance(this); // Khởi tạo DB instance

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

        // Xử lý sự kiện click nút đăng ký
        btnRegister.setOnClickListener(v -> attemptRegisterUser());
    }

    private void attemptRegisterUser() {
        String name = edtName.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Băm mật khẩu trước khi tạo User object
        String hashedPassword = PasswordHasher.hash(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Lỗi xử lý mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, username, phone, address, hashedPassword);
        new RegisterUserTask().execute(newUser);
    }

    // Sử dụng AsyncTask để thực hiện thao tác DB trên background thread
    private class RegisterUserTask extends AsyncTask<User, Void, Boolean> {
        private boolean usernameExists = false;

        @Override
        protected Boolean doInBackground(User... users) {
            User userToRegister = users[0];
            try {
                // 1. Kiểm tra username đã tồn tại chưa
                if (db.userDAO().findByUsername(userToRegister.getUsername()) != null) {
                    usernameExists = true;
                    return false; // Đăng ký thất bại do trùng username
                }
                // 2. Nếu chưa tồn tại, thêm user mới
                db.userDAO().insert(userToRegister);
                return true; // Đăng ký thành công
            } catch (Exception e) {
                // Log lỗi cụ thể, ví dụ: SQLiteConstraintException nếu bỏ qua kiểm tra trên
                e.printStackTrace();
                return false; // Đăng ký thất bại do lỗi DB
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(Main_Screen2.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                // Chuyển về màn hình đăng nhập sau khi đăng ký thành công
                startActivity(new Intent(Main_Screen2.this, Main_Screen.class));
                finish(); // Đóng màn hình đăng ký
            } else {
                if (usernameExists) {
                    Toast.makeText(Main_Screen2.this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Main_Screen2.this, "Đăng ký thất bại. Có lỗi xảy ra.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}