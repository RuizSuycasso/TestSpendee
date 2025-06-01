package com.example.spendee;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDAO {

    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY date ASC")
    List<Transaction> getAllTransactionsOrderedByDateAsc();

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date < :endTime ORDER BY date ASC")
    List<Transaction> getTransactionsForPeriod(long startTime, long endTime);

    @Query("SELECT SUM(amount) FROM transactions WHERE category_id = :categoryId AND isIncome = 0 AND date >= :startTime AND date < :endTime")
    Double getSumOfExpensesForCategoryInPeriod(int categoryId, long startTime, long endTime);

}