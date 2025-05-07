package com.example.spendee; // Thay đổi package nếu cần

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;

@Entity(tableName = "users", indices = {@Index(value = "username", unique = true)}) // Đảm bảo username là duy nhất
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "username")
    private String username; // Sẽ dùng để đăng nhập

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "password_hash") // Lưu trữ mật khẩu đã băm
    private String passwordHash;

    // Constructor rỗng bắt buộc cho Room
    public User() {}

    // Constructor để tạo user mới (chú ý nhận password hash)
    public User(String name, String username, String phone, String address, String passwordHash) {
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.address = address;
        this.passwordHash = passwordHash;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPasswordHash() { return passwordHash; }

    // Setters (Cần thiết cho Room và cập nhật)
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}