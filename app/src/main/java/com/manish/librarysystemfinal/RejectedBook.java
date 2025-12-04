package com.manish.librarysystemfinal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rejected_books")
public class RejectedBook {

    @PrimaryKey(autoGenerate = true)
    public int id;                // Local Room ID

    public String bookId;         // Firestore book document ID
    public String vendorId;       // Vendor UID
    public String title;          // Book title
    public String author;         // Book author
    public double price;          // Book price
    public int stock;             // Available stock
    public String imageBase64;    // Book image
    public String reason;         // Rejection reason (local only)
    public long rejectedAt;       // Timestamp (System.currentTimeMillis())

    public RejectedBook(String bookId, String vendorId, String title, String author,
                        double price, int stock, String imageBase64,
                        String reason, long rejectedAt) {

        this.bookId = bookId;
        this.vendorId = vendorId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.imageBase64 = imageBase64;
        this.reason = reason;
        this.rejectedAt = rejectedAt;
    }
}
