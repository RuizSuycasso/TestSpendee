package com.example.spendee; // Giữ nguyên package của bạn

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log; // Thêm import cho Log
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Thêm import cho Toolbar
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.spendee.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity"; // Tag cho logging
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView nameTextView, usernameTextView; // TextView trong header
    private DrawerLayout drawer;
    private AppDatabase db;
    private SharedPreferences loginPrefs; // SharedPreferences để lấy ID và logout
    private User currentUser; // Lưu trữ thông tin user đang đăng nhập

    // Constants cho SharedPreferences (phải khớp với Main_Screen nếu Main_Screen cũng dùng)
    // Hiện tại Main_Screen chỉ truyền USER_ID qua Intent, chưa lưu vào SharedPreferences.
    private static final String LOGIN_PREFS_NAME = "LoginStatus"; // Nên dùng cùng tên với Main_Screen nếu có
    private static final String LOGGED_IN_USER_ID_KEY = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập Toolbar làm ActionBar - SỬA LỖI
        Toolbar toolbar = binding.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        db = AppDatabase.getInstance(this);
        loginPrefs = getSharedPreferences(LOGIN_PREFS_NAME, MODE_PRIVATE);

        // --- SỬA LỖI: LƯU USER_ID TỪ INTENT VÀO SHAREDPREFERENCES ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_ID")) {
            int userIdFromIntent = intent.getIntExtra("USER_ID", -1); // -1 là giá trị mặc định nếu không tìm thấy
            if (userIdFromIntent != -1) {
                SharedPreferences.Editor editor = loginPrefs.edit();
                editor.putInt(LOGGED_IN_USER_ID_KEY, userIdFromIntent);
                editor.apply(); // Lưu thay đổi
                Log.d(TAG, "onCreate: Saved USER_ID (" + userIdFromIntent + ") from Intent to SharedPreferences.");
            } else {
                Log.w(TAG, "onCreate: USER_ID from Intent was -1. Not saving to SharedPreferences.");
            }
        } else {
            Log.d(TAG, "onCreate: No USER_ID found in Intent extras.");
        }
        // --- KẾT THÚC SỬA LỖI ---

        binding.appBarMain.fab.setVisibility(View.GONE);
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Lấy headerView và ánh xạ TextViews trong header
        View headerView = navigationView.getHeaderView(0);
        nameTextView = headerView.findViewById(R.id.textName);
        usernameTextView = headerView.findViewById(R.id.textUsername);

        // Tải thông tin người dùng từ DB dựa trên ID đã lưu và hiển thị lên header
        // Bây giờ, loadUserInfoFromDb() sẽ có thể đọc ID từ SharedPreferences một cách chính xác
        loadUserInfoFromDb();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow) // Các top-level destinations
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void loadUserInfoFromDb() {
        int loggedInUserId = loginPrefs.getInt(LOGGED_IN_USER_ID_KEY, -1);
        Log.d(TAG, "loadUserInfoFromDb: Attempting to load user with ID from SharedPreferences: " + loggedInUserId);

        if (loggedInUserId != -1) {
            new LoadUserTask().execute(loggedInUserId);
        } else {
            Log.e(TAG, "loadUserInfoFromDb: Invalid user ID (-1) from SharedPreferences. Logging out.");
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            logout();
        }
    }

    private class LoadUserTask extends AsyncTask<Integer, Void, User> {
        @Override
        protected User doInBackground(Integer... userIds) {
            if (userIds.length > 0 && userIds[0] != -1) {
                try {
                    Log.d(TAG, "LoadUserTask: Loading user with ID: " + userIds[0]);
                    return db.userDAO().findById(userIds[0]);
                } catch (Exception e) {
                    Log.e(TAG, "LoadUserTask: Database error while loading user.", e);
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            currentUser = user;
            if (currentUser != null) {
                Log.d(TAG, "LoadUserTask: User loaded successfully: " + currentUser.getUsername());
                nameTextView.setText(currentUser.getName());
                usernameTextView.setText(currentUser.getUsername());
            } else {
                Log.e(TAG, "LoadUserTask: User not found in DB with stored ID, or error occurred. Logging out.");
                Toast.makeText(MainActivity.this, "Lỗi: Không tìm thấy dữ liệu người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                logout();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        Log.d(TAG, "onNavigationItemSelected: Item selected: " + item.getTitle());

        if (item.getItemId() == R.id.nav_logout) {
            Log.d(TAG, "onNavigationItemSelected: Logout item selected.");
            logout();
            return true;
        }

        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
        if (handled && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return handled;
    }

    private void logout() {
        Log.d(TAG, "logout: Logging out user.");
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.remove(LOGGED_IN_USER_ID_KEY); // Xóa ID người dùng đã lưu
        // Bạn cũng có thể muốn xóa các thông tin khác liên quan đến phiên đăng nhập
        // editor.clear(); // Nếu muốn xóa tất cả SharedPreferences của LOGIN_PREFS_NAME
        editor.apply();

        currentUser = null;

        Intent intent = new Intent(this, Main_Screen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Đảm bảo Main_Screen là task mới và xóa task cũ
        startActivity(intent);
        finishAffinity(); // Đóng tất cả activity liên quan đến task hiện tại
        // finish(); // Có thể dùng finish() nếu chỉ muốn đóng MainActivity
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void updateNavHeader() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName());
            usernameTextView.setText(currentUser.getUsername());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MainActivity destroyed");
    }
}