package com.example.spendee; // Thay đổi package nếu cần

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Thêm User.class vào entities và TĂNG VERSION lên 3 (hoặc số tiếp theo)
@Database(entities = {Transaction.class, User.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    // Nên đổi tên file DB để tránh xung đột với file DB cũ chỉ có Transaction
    private static final String DATABASE_NAME = "spendee_app.db";
    private static volatile AppDatabase instance; // Thêm volatile cho thread safety tốt hơn

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Dùng double-checked locking cho singleton
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            // CẢNH BÁO: Sẽ xóa toàn bộ dữ liệu cũ (cả User và Transaction)
                            // khi nâng cấp version! Thay bằng Migration nếu cần giữ dữ liệu.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    // DAO cho Transactions (giữ nguyên từ TransactionDatabase)
    public abstract TransactionDAO transactionDAO();

    // DAO cho Users (mới)
    public abstract UserDAO userDAO();
}