package com.example.spendee;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BudgetDAO {

    // Chèn Budget mới, nếu đã có budget cho category/tháng/năm đó thì thay thế
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Budget budget);

    // Cập nhật Budget đã có
    @Update
    void update(Budget budget);

    // Xóa Budget
    @Delete
    void delete(Budget budget);

    // Lấy tất cả Budget cho một tháng/năm cụ thể
    @Query("SELECT * FROM budgets WHERE year = :year AND month = :month")
    List<Budget> getBudgetsForMonth(int year, int month);

    // Lấy một Budget cụ thể cho category và tháng/năm
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND year = :year AND month = :month LIMIT 1")
    Budget getBudgetForCategoryAndMonth(int categoryId, int year, int month);

    // (Tùy chọn) Lấy tất cả budgets
    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    List<Budget> getAllBudgets();
}