package com.example.spendee; // Thay đổi package nếu cần

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.spendee.databinding.ActivityMainBinding; // Giữ nguyên ViewBinding
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView nameTextView, usernameTextView; // TextView trong header
    private DrawerLayout drawer;
    private AppDatabase db;
    private SharedPreferences loginPrefs; // SharedPreferences để lấy ID và logout
    private User currentUser; // Lưu trữ thông tin user đang đăng nhập

    // Constants cho SharedPreferences (phải khớp với Main_Screen)
    private static final String LOGIN_PREFS_NAME = "LoginStatus";
    private static final String LOGGED_IN_USER_ID_KEY = "logged_in_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        loginPrefs = getSharedPreferences(LOGIN_PREFS_NAME, MODE_PRIVATE);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setAnchorView(R.id.fab).show());

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Lấy headerView và ánh xạ TextViews trong header
        View headerView = navigationView.getHeaderView(0);
        nameTextView = headerView.findViewById(R.id.textName);
        usernameTextView = headerView.findViewById(R.id.textUsername);

        // Tải thông tin người dùng từ DB dựa trên ID đã lưu và hiển thị lên header
        loadUserInfoFromDb();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow) // Các top-level destinations
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Set listener cho navigation view (quan trọng để bắt sự kiện logout)
        navigationView.setNavigationItemSelectedListener(this);

        // (Tùy chọn) Tự động đóng drawer khi chuyển fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Kiểm tra xem drawer có thực sự mở không trước khi đóng
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void loadUserInfoFromDb() {
        int loggedInUserId = loginPrefs.getInt(LOGGED_IN_USER_ID_KEY, -1);
        if (loggedInUserId != -1) {
            // Lấy thông tin user từ DB trên background thread
            new LoadUserTask().execute(loggedInUserId);
        } else {
            // Nếu không có ID user hợp lệ -> có lỗi, quay về màn hình đăng nhập
            Toast.makeText(this, "Phiên đăng nhập không hợp lệ.", Toast.LENGTH_SHORT).show();
            logout(); // Gọi logout để đảm bảo trạng thái được clear
        }
    }

    // AsyncTask để tải thông tin User từ DB
    private class LoadUserTask extends AsyncTask<Integer, Void, User> {
        @Override
        protected User doInBackground(Integer... userIds) {
            if (userIds.length > 0 && userIds[0] != -1) {
                try {
                    return db.userDAO().findById(userIds[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            currentUser = user; // Lưu lại thông tin user hiện tại
            if (currentUser != null) {
                // Hiển thị thông tin lên header
                nameTextView.setText(currentUser.getName());
                usernameTextView.setText(currentUser.getUsername());
            } else {
                // Không tìm thấy user trong DB với ID đã lưu? Dữ liệu không nhất quán
                Toast.makeText(MainActivity.this, "Lỗi: Không tìm thấy dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                logout(); // Quay về đăng nhập
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Xử lý riêng cho mục Logout
        if (item.getItemId() == R.id.nav_logout) {
            logout();
            // Không cần đóng drawer vì activity sẽ bị finish
            return true; // Đã xử lý xong sự kiện
        }

        // Đối với các mục khác, để NavigationUI tự xử lý
        // Đóng drawer trước khi điều hướng (nếu chưa có listener ở onCreate)
        // if (drawer.isDrawerOpen(GravityCompat.START)) {
        //     drawer.closeDrawer(GravityCompat.START);
        // }
        // return NavigationUI.onNavDestinationSelected(item, navController);
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
        // Đóng drawer nếu item được chọn không phải là logout và đã được xử lý bởi NavigationUI
        if (handled && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return handled;

    }

    private void logout() {
        // Xóa ID người dùng đã lưu trong SharedPreferences
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.remove(LOGGED_IN_USER_ID_KEY);
        editor.apply();

        currentUser = null; // Xóa thông tin user hiện tại trong MainActivity

        // Tạo Intent để quay lại màn hình đăng nhập
        Intent intent = new Intent(this, Main_Screen.class);
        // Xóa các activity phía trên trong stack và tạo task mới
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Đảm bảo đóng tất cả activity liên quan đến task hiện tại

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    // Hỗ trợ nút Up/Back trên ActionBar và mở/đóng Drawer
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    // (Optional) Cung cấp phương thức để các Fragment con có thể lấy thông tin user hiện tại
    public User getCurrentUser() {
        return currentUser;
    }

    // (Optional) Cung cấp phương thức để các Fragment con có thể cập nhật header nếu thông tin user thay đổi
    public void updateNavHeader() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName());
            usernameTextView.setText(currentUser.getUsername());
        }
    }

}