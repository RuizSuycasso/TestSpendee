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

public class Main_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        EditText usernameInput = findViewById(R.id.password);
        EditText passwordInput = findViewById(R.id.password2);
        Button loginButton = findViewById(R.id.button);
        Button registerButton = findViewById(R.id.button3);

        // Chuyển sang trang đăng ký
        registerButton.setOnClickListener(p -> {
            Intent intent = new Intent(Main_Screen.this, Main_Screen2.class);
            startActivity(intent);
        });

        // Điều chỉnh padding cho Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra thông tin đăng nhập
        loginButton.setOnClickListener(p -> {
            String inputUser = usernameInput.getText().toString();
            String inputPass = passwordInput.getText().toString();

            SharedPreferences sharedPreferences = getSharedPreferences("UserSpendee", MODE_PRIVATE);

            // Tìm tài khoản khớp với username và password
            boolean isLoggedIn = false;
            String currentName = "";
            String currentUsername = "";
            int currentUserIndex = -1; // Thêm biến để lưu index

            for (int i = 0; i < 100; i++) { // Kiểm tra tối đa 100 tài khoản
                String savedUser = sharedPreferences.getString("username_" + i, "");
                String savedPass = sharedPreferences.getString("password_" + i, "");
                String savedName = sharedPreferences.getString("name_" + i, "");

                if (inputUser.equals(savedUser) && inputPass.equals(savedPass)) {
                    isLoggedIn = true;
                    currentName = savedName;
                    currentUsername = savedUser;
                    currentUserIndex = i; // Lưu index khi tìm thấy tài khoản
                    break;
                }
            }

            if (isLoggedIn) {
                // Lưu tài khoản đang đăng nhập và index vào SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("current_name", currentName);
                editor.putString("current_username", currentUsername);
                editor.putInt("current_user_index", currentUserIndex); // Lưu index
                editor.apply();

                // Chuyển sang MainActivity
                Intent intent = new Intent(Main_Screen.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Main_Screen.this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}