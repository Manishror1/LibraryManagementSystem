package com.manish.librarysystemfinal;

public class Book {
    public String id;
    public String title;
    public String author;
    public double price;
    public int stock;
    public String imageBase64;

    // New fields for vendor feature
    public String vendorId;   // Which vendor uploaded this
    public String status;     // "pending", "approved", "rejected"

    public Book() {
        // Needed for Firestore
    }

    // Old-style constructor (kept for compatibility if used anywhere)
    public Book(String id, String title, String author, double price, int stock, String imageBase64) {
        this(id, title, author, price, stock, imageBase64, null, "approved");
    }

    // New constructor with vendor + status
    public Book(String id,
                String title,
                String author,
                double price,
                int stock,
                String imageBase64,
                String vendorId,
                String status) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.imageBase64 = imageBase64;
        this.vendorId = vendorId;
        this.status = status;
    }
}
