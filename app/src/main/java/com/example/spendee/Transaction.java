package com.example.spendee;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;


@Entity(tableName = "transactions")
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


    // Required by Room
    public Transaction() {}


    public Transaction(double amount, boolean isIncome, String description) { // Sửa constructor
        this.amount = amount;
        this.isIncome = isIncome;
        this.date = System.currentTimeMillis();
        this.description = description;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }


    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }


    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }


    public String getDescription() { return description; } // Thêm getter
    public void setDescription(String description) { this.description = description; } // Thêm setter
}