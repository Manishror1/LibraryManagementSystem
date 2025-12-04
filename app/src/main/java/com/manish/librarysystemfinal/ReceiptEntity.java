package com.manish.librarysystemfinal;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipts")
public class ReceiptEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String receiptId;
    public String userId;

    public String bookId;
    public String bookTitle;
    public double amount;

    public String paymentType;  // "borrow" or "fine"
    public long timestamp;

    public String pdfPath;  // internal storage file path
}
