package com.manish.librarysystemfinal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "borrowed_books")
public class BorrowedBook {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String bookId;
    public String title;
    public String author;
    public String imageBase64;

    public long borrowedAt;   // when student borrowed
    public long dueDate;      // 7 days after borrowed

    public BorrowedBook() {
        // Empty constructor required by Room
    }

    // ⭐ UPDATED constructor with due date auto-calculated
    public BorrowedBook(String bookId, String title, String author, String imageBase64, long borrowedAt) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.imageBase64 = imageBase64;
        this.borrowedAt = borrowedAt;

        // Due date = borrowedAt + 7 days
        this.dueDate = borrowedAt + (7L * 24 * 60 * 60 * 1000);
    }
}
