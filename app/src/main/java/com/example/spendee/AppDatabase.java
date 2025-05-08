package com.example.spendee;

import android.content.Context;
import androidx.annotation.NonNull; // << THÊM IMPORT NÀY (Cần cho Callback nếu dùng)
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase; // << THÊM IMPORT NÀY (Cần cho Callback)
import java.util.concurrent.Executors; // << THÊM IMPORT NÀY (Cần cho Callback)


// --- SỬA ĐỔI @Database: Thêm Category.class và TĂNG VERSION lên 4 ---
@Database(entities = {Transaction.class, User.class, Category.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "spendee_app.db";
    private static volatile AppDatabase instance;

    public abstract TransactionDAO transactionDAO();
    public abstract UserDAO userDAO();
    // --- THÊM LẠI CategoryDAO ---
    public abstract CategoryDAO categoryDAO();


    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration() // Giữ lại, sẽ xóa DB cũ khi version tăng từ 3 lên 4
                            .addCallback(roomCallback) // << THÊM LẠI CALLBACK ĐỂ THÊM CATEGORY MẪU
                            .build();
                }
            }
        }
        return instance;
    }

    // --- THÊM LẠI roomCallback ĐỂ TẠO CATEGORY MẶC ĐỊNH ---
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Chạy trên background thread để thêm dữ liệu mẫu khi DB được tạo lần đầu
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDAO dao = instance.categoryDAO(); // Lấy CategoryDAO

                // Thêm các danh mục Chi mặc định
                dao.insert(new Category("Ăn uống", false));
                dao.insert(new Category("Đi lại", false));
                dao.insert(new Category("Mua sắm", false));
                dao.insert(new Category("Hóa đơn", false)); // Ví dụ: Điện, nước, internet
                dao.insert(new Category("Giải trí", false));
                dao.insert(new Category("Sức khỏe", false));
                dao.insert(new Category("Giáo dục", false));
                dao.insert(new Category("Khác (Chi)", false));

                // Thêm các danh mục Thu mặc định
                dao.insert(new Category("Lương", true));
                dao.insert(new Category("Thưởng", true));
                dao.insert(new Category("Thu nhập phụ", true));
                dao.insert(new Category("Quà tặng", true));
                dao.insert(new Category("Khác (Thu)", true));
            });
        }
    };
    // --- KẾT THÚC PHẦN THÊM CALLBACK ---
}