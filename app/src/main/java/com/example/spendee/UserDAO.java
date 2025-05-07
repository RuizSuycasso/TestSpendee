package com.example.spendee; // Thay đổi package nếu cần

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;

@Dao
public interface UserDAO {

    // Thêm user mới. Mặc định, nếu trùng username (unique index) sẽ throw SQLiteConstraintException.
    // Cân nhắc dùng onConflict = OnConflictStrategy.IGNORE hoặc .REPLACE tùy yêu cầu.
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    // Tìm user theo username (dùng cho đăng nhập và kiểm tra tồn tại)
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    // Tìm user theo ID (dùng sau khi đã đăng nhập và chỉ lưu ID)
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(int userId);

    // Có thể thêm các query khác nếu cần, ví dụ:
    // @Delete
    // void delete(User user);
}