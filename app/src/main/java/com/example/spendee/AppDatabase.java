package com.example.spendee;

import android.content.Context;
import android.util.Log; // << THÊM IMPORT Log
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;


// --- SỬA ĐỔI @Database: Thêm Budget.class và TĂNG VERSION lên 5 ---
@Database(entities = {Transaction.class, User.class, Category.class, Budget.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "spendee_app.db";
    private static volatile AppDatabase instance;
    // << THÊM TAG CHO CALLBACK LOGGING >>
    private static final String CALLBACK_TAG = "AppDatabaseCallback";

    public abstract TransactionDAO transactionDAO();
    public abstract UserDAO userDAO();
    public abstract CategoryDAO categoryDAO();
    // --- THÊM BudgetDAO ---
    public abstract BudgetDAO budgetDAO();


    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    Log.d(CALLBACK_TAG, "getInstance: Creating new database instance..."); // Log khi tạo instance mới
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration() // Giữ lại
                            .addCallback(roomCallback) // Giữ lại Callback
                            .build();
                    Log.d(CALLBACK_TAG, "getInstance: Database instance created.");
                }
            }
        }
        return instance;
    }

    // --- CẬP NHẬT Callback VỚI LOGGING ---
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Log khi callback onCreate được gọi (chỉ chạy khi DB tạo lần đầu)
            Log.d(CALLBACK_TAG, "onCreate: Database being created for the first time. Seeding initial data...");
            Executors.newSingleThreadExecutor().execute(() -> {
                // Log khi background thread bắt đầu
                Log.d(CALLBACK_TAG, "onCreate: Background thread started for seeding categories.");
                // Lấy instance DAO (chắc chắn instance đã được tạo)
                CategoryDAO categoryDao = instance.categoryDAO();
                try {
                    Log.d(CALLBACK_TAG, "onCreate: Inserting default categories...");
                    // Thêm các danh mục Chi mặc định
                    categoryDao.insert(new Category("Ăn uống", false));
                    categoryDao.insert(new Category("Đi lại", false));
                    categoryDao.insert(new Category("Mua sắm", false));
                    categoryDao.insert(new Category("Hóa đơn", false));
                    categoryDao.insert(new Category("Giải trí", false));
                    categoryDao.insert(new Category("Sức khỏe", false));
                    categoryDao.insert(new Category("Giáo dục", false));
                    categoryDao.insert(new Category("Khác (Chi)", false));

                    // Thêm các danh mục Thu mặc định
                    categoryDao.insert(new Category("Lương", true));
                    categoryDao.insert(new Category("Thưởng", true));
                    categoryDao.insert(new Category("Thu nhập phụ", true));
                    categoryDao.insert(new Category("Quà tặng", true));
                    categoryDao.insert(new Category("Khác (Thu)", true));

                    // Log khi insert thành công
                    Log.d(CALLBACK_TAG, "onCreate: Default categories inserted successfully.");

                } catch (Exception e) {
                    // Log nếu có lỗi xảy ra trong quá trình insert
                    Log.e(CALLBACK_TAG, "onCreate: Error inserting default categories!", e);
                }
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Log mỗi khi database được mở (không chỉ lần đầu)
            Log.d(CALLBACK_TAG, "onOpen: Database opened. Version: " + db.getVersion());
        }
    };
    // --- KẾT THÚC CẬP NHẬT CALLBACK ---
}