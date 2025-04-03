package com.example.spendee;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


import java.util.List;


@Dao
public interface TransactionDAO {
    @Insert
    void insert(Transaction transaction);


    @Query("SELECT * FROM transactions ORDER BY date DESC") // Sử dụng 'date' (tên field)
    List<Transaction> getAllTransactions();


    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 1") // Sử dụng 'amount' và 'isIncome' (tên field)
    double getTotalIncome();


    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE isIncome = 0") // Sử dụng 'amount' và 'isIncome' (tên field)
    double getTotalExpense();
}