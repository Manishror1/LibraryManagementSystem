package com.manish.librarysystemfinal;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                BorrowedBook.class,
                RejectedBook.class,
                ReceiptEntity.class      // ← ADDED NEW ENTITY
        },
        version = 4,                      // ← VERSION UPDATED
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract BorrowedBookDao borrowedBookDao();
    public abstract RejectedBookDao rejectedBookDao();
    public abstract ReceiptDao receiptDao();   // ← ADDED NEW DAO

    public static synchronized AppDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "library_local_db"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }
}
