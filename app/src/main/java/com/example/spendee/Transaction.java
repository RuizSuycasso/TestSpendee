package com.example.spendee;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Định nghĩa index CHỈ trong @Entity
@Entity(tableName = "transactions",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.SET_NULL),
        indices = {@Index(value = {"category_id"})}) // <-- Giữ lại định nghĩa index ở đây
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "isIncome")
    private boolean isIncome;

    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "description")
    private String description;

    // --- SỬA LỖI: Xóa index = true khỏi @ColumnInfo ---
    @ColumnInfo(name = "category_id") // <-- Đã xóa index = true
    private Integer categoryId;

    // Required by Room
    public Transaction() {}

    // Constructor giữ nguyên
    public Transaction(double amount, boolean isIncome, String description, Integer categoryId) {
        this.amount = amount;
        this.isIncome = isIncome;
        this.date = System.currentTimeMillis();
        this.description = description;
        this.categoryId = categoryId;
    }

    // Getters and Setters giữ nguyên
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}