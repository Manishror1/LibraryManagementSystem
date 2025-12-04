package com.manish.librarysystemfinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReceiptDao {

    @Insert
    void insert(ReceiptEntity r);

    @Query("SELECT * FROM receipts WHERE userId = :uid ORDER BY timestamp DESC")
    List<ReceiptEntity> getReceiptsByUser(String uid);
}
