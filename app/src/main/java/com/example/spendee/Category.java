package com.example.spendee;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories",
        indices = {@Index(value = {"name", "is_income"}, unique = true)}) // Đảm bảo tên + loại là duy nhất
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "is_income") // true cho danh mục Thu, false cho danh mục Chi
    private boolean isIncomeCategory;

    // Constructor cần thiết cho Room
    public Category() {}

    // Constructor để tạo đối tượng (ví dụ khi thêm category mặc định)
    public Category(String name, boolean isIncomeCategory) {
        this.name = name;
        this.isIncomeCategory = isIncomeCategory;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isIncomeCategory() {
        return isIncomeCategory;
    }

    // Setters (cần thiết cho Room)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIncomeCategory(boolean incomeCategory) {
        isIncomeCategory = incomeCategory;
    }

    // Ghi đè toString() để hiển thị tên trên Spinner
    @Override
    public String toString() {
        return name; // Spinner sẽ hiển thị trường name
    }
}