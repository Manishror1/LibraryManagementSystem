package com.manish.librarysystemfinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BorrowedBookDao {

    @Insert
    void insert(BorrowedBook book);

    @Query("SELECT * FROM borrowed_books")
    List<BorrowedBook> getAll();

    @Query("DELETE FROM borrowed_books WHERE bookId = :bookId")
    void deleteByBookId(String bookId);

    @Query("DELETE FROM borrowed_books")
    void clearAll();
}
