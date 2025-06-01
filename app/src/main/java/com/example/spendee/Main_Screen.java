package com.example.spendee;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Main_Screen extends AppCompatActivity {
    private static final String TAG = "Main_Screen";

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting Main_Screen");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);

        db = AppDatabase.getInstance(this);

        // Ánh xạ Views
        usernameInput = findViewById(R.id.password);
        passwordInput = findViewById(R.id.password2);
        loginButton = findViewById(R.id.button);
        registerButton = findViewById(R.id.button3);
        registerButton.setOnClickListener(p -> {
            Log.d(TAG, "Register button clicked");
            Intent intent = new Intent(Main_Screen.this, Main_Screen2.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton.setOnClickListener(p -> {
            Log.d(TAG, "Login button clicked");
            String inputUser = usernameInput.getText().toString().trim();
            String inputPass = passwordInput.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(Main_Screen.this, "Vui lòng nhập tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            new LoginTask().execute(inputUser, inputPass);
        });
    }
    private class LoginTask extends AsyncTask<String, Void, User> {
        private String plainPassword;

        @Override
        protected User doInBackground(String... credentials) {
            String username = credentials[0];
            plainPassword = credentials[1];

            Log.d(TAG, "doInBackground: Checking login for user: " + username);

            try {
                return db.userDAO().findByUsername(username);
            } catch (Exception e) {
                Log.e(TAG, "Database error: ", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                Log.d(TAG, "onPostExecute: User found, checking password");

                // Tìm thấy user, bây giờ kiểm tra mật khẩu
                if (PasswordHasher.verify(plainPassword, user.getPasswordHash())) {
                    // Mật khẩu chính xác -> Đăng nhập thành công
                    Log.d(TAG, "onPostExecute: Password verified, login successful");
                    Toast.makeText(Main_Screen.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    try {
                        Toast.makeText(Main_Screen.this, "Chuyển đến màn hình chính...", Toast.LENGTH_SHORT).show();

                        Thread.sleep(500);

                        Intent intent = new Intent(Main_Screen.this, MainActivity.class);
                        intent.putExtra("USER_ID", user.getId());

                        Log.d(TAG, "Starting MainActivity with USER_ID: " + user.getId());

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        startActivity(intent);

                        Log.d(TAG, "Finishing Main_Screen");
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during activity transition: ", e);
                        Toast.makeText(Main_Screen.this, "Lỗi khi chuyển màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "onPostExecute: Incorrect password");
                    Toast.makeText(Main_Screen.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "onPostExecute: User not found");
                Toast.makeText(Main_Screen.this, "Tên đăng nhập không tồn tại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Main_Screen destroyed");
    }
}