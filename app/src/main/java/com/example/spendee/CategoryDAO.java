package com.example.spendee;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDAO {

    // Thêm một Category mới, nếu trùng tên + loại thì bỏ qua
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Category category);

    // Lấy tất cả Categories sắp xếp theo tên
    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategories();

    // Lấy Categories theo loại (Thu hoặc Chi) sắp xếp theo tên
    @Query("SELECT * FROM categories WHERE is_income = :isIncome ORDER BY name ASC")
    List<Category> getCategoriesByType(boolean isIncome);

    // (Tùy chọn) Thêm các phương thức khác nếu cần (update, delete...)
}