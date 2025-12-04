package com.manish.librarysystemfinal;
public class Review {

    public String id;
    public String bookId;
    public String userId;
    public String userName;
    public float rating;
    public String comment;
    public long timestamp;

    public Review() {}  // required for Firestore

    public Review(String id, String bookId, String userId, String userName,
                  float rating, String comment, long timestamp) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }
}
