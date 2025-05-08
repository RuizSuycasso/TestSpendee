package com.example.spendee;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Định nghĩa Entity cho Bảng Ngân Sách
@Entity(tableName = "budgets",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.CASCADE), // Nếu xóa Category thì xóa luôn Budget liên quan
        // Đảm bảo mỗi category chỉ có 1 budget cho một tháng/năm cụ thể
        indices = {@Index(value = {"category_id", "year", "month"}, unique = true),
                @Index(value = {"category_id"})}) // Index cho category_id để join nhanh hơn
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "category_id")
    private int categoryId; // Khóa ngoại đến Category

    @ColumnInfo(name = "limit_amount")
    private double limitAmount; // Số tiền hạn mức

    @ColumnInfo(name = "year")
    private int year; // Năm áp dụng ngân sách

    @ColumnInfo(name = "month")
    private int month; // Tháng áp dụng ngân sách (0-11)

    // --- Các trường này không lưu vào DB, dùng để hiển thị ---
    @Ignore // Không lưu vào database
    private String categoryName; // Tên danh mục (lấy từ join hoặc map)

    @Ignore // Không lưu vào database
    private double spentAmount; // Số tiền đã chi (tính toán từ transactions)
    // --- Kết thúc các trường @Ignore ---


    // Constructor cho Room
    public Budget() {}

    // Constructor tiện lợi để tạo đối tượng
    public Budget(int categoryId, double limitAmount, int year, int month) {
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.year = year;
        this.month = month;
        this.spentAmount = 0; // Mặc định là 0 khi mới tạo
    }

    // Getters
    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public double getLimitAmount() { return limitAmount; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public String getCategoryName() { return categoryName; }
    public double getSpentAmount() { return spentAmount; }

    // Setters (Room cần)
    public void setId(int id) { this.id = id; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
    // Setters cho các trường Ignore (để cập nhật sau khi tính toán)
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
}