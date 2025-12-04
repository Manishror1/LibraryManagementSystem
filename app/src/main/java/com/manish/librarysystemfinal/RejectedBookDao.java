package com.manish.librarysystemfinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RejectedBookDao {

    @Insert
    void insert(RejectedBook book);

    @Query("SELECT * FROM rejected_books WHERE vendorId = :vendorId ORDER BY rejectedAt DESC")
    List<RejectedBook> getRejectedBooksByVendor(String vendorId);

    @Query("DELETE FROM rejected_books WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM rejected_books")
    void clearAll();
}
