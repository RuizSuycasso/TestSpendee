package com.example.spendee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.spendee.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private TextView nameTextView, usernameTextView;
    private DrawerLayout drawer; // Thêm biến drawer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).setAnchorView(R.id.fab).show());

        drawer = binding.drawerLayout; // Ánh xạ drawer
        NavigationView navigationView = binding.navView;

        // Lấy headerView
        View headerView = navigationView.getHeaderView(0);
        nameTextView = headerView.findViewById(R.id.textName);
        usernameTextView = headerView.findViewById(R.id.textUsername);
        loadUserInfo();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        // Thêm DrawerListener để đóng drawer khi chuyển fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (drawer.isOpen()) {
                drawer.close();
            }
        });
    }

    private void loadUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSpendee", MODE_PRIVATE);
        String name = sharedPreferences.getString("current_name", "Tên mặc định");
        String username = sharedPreferences.getString("current_username", "Username mặc định");

        nameTextView.setText(name);
        usernameTextView.setText(username);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_logout) {
            logout();
            return true;
        }
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment_content_main));
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserSpendee", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("current_username");
        editor.remove("current_name");
        editor.remove("current_user_index");
        editor.apply();

        Intent intent = new Intent(this, Main_Screen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
