package com.example.spendee;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transaction.class}, version = 2)
public abstract class TransactionDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "transactions.db";
    private static TransactionDatabase instance;

    public static synchronized TransactionDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            TransactionDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract TransactionDAO transactionDAO();
}