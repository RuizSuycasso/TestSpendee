package com.example.spendee; // Thay đổi package nếu cần

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

    private EditText usernameInput; // Đổi tên ID R.id.password thành R.id.usernameInput trong layout
    private EditText passwordInput; // Giữ nguyên R.id.password2 hoặc đổi thành R.id.passwordInput
    private Button loginButton;
    private Button registerButton;
    private AppDatabase db;
    private SharedPreferences loginPrefs; // Dùng SharedPreferences chỉ để lưu ID người dùng đã đăng nhập

    // Constants cho SharedPreferences
    private static final String LOGIN_PREFS_NAME = "LoginStatus";
    private static final String LOGGED_IN_USER_ID_KEY = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        db = AppDatabase.getInstance(this);
        loginPrefs = getSharedPreferences(LOGIN_PREFS_NAME, MODE_PRIVATE);

        // Kiểm tra xem người dùng đã đăng nhập trước đó chưa
        checkLoginStatus();

        // Ánh xạ Views (Đảm bảo ID trong layout khớp)
        usernameInput = findViewById(R.id.password); // !! Sửa ID trong R.layout.activity_main_screen thành usernameInput !!
        passwordInput = findViewById(R.id.password2);
        loginButton = findViewById(R.id.button);
        registerButton = findViewById(R.id.button3);

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

        // Xử lý sự kiện click nút đăng nhập
        loginButton.setOnClickListener(p -> {
            String inputUser = usernameInput.getText().toString().trim();
            String inputPass = passwordInput.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(Main_Screen.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            // Thực hiện đăng nhập trên background thread
            new LoginTask().execute(inputUser, inputPass);
        });
    }

    private void checkLoginStatus() {
        // Lấy ID người dùng đã lưu, nếu có (khác -1) thì vào thẳng MainActivity
        int loggedInUserId = loginPrefs.getInt(LOGGED_IN_USER_ID_KEY, -1);
        if (loggedInUserId != -1) {
            Intent intent = new Intent(Main_Screen.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng màn hình đăng nhập để người dùng không back lại được
        }
        // Nếu không có ID hoặc là -1, thì ở lại màn hình này để đăng nhập
    }

    // AsyncTask để kiểm tra đăng nhập trên background thread
    private class LoginTask extends AsyncTask<String, Void, User> {
        private String plainPassword; // Lưu mật khẩu người dùng nhập để kiểm tra

        @Override
        protected User doInBackground(String... credentials) {
            String username = credentials[0];
            plainPassword = credentials[1];
            // Tìm user trong DB bằng username
            try {
                return db.userDAO().findByUsername(username);
            } catch (Exception e) {
                e.printStackTrace(); // Log lỗi DB
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                // Tìm thấy user, bây giờ kiểm tra mật khẩu
                if (PasswordHasher.verify(plainPassword, user.getPasswordHash())) {
                    // Mật khẩu chính xác -> Đăng nhập thành công
                    Toast.makeText(Main_Screen.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Lưu ID của người dùng vào SharedPreferences
                    SharedPreferences.Editor editor = loginPrefs.edit();
                    editor.putInt(LOGGED_IN_USER_ID_KEY, user.getId());
                    editor.apply(); // Sử dụng apply() cho hiệu năng tốt hơn commit()

                    // Chuyển sang MainActivity
                    Intent intent = new Intent(Main_Screen.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Đóng màn hình đăng nhập
                } else {
                    // Sai mật khẩu
                    Toast.makeText(Main_Screen.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Không tìm thấy user với username đã nhập
                Toast.makeText(Main_Screen.this, "Tên đăng nhập không tồn tại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}