package com.manish.librarysystemfinal;

public class BorrowRecord {

    public String id;
    public String userId;
    public String userName;
    public String userEmail;

    public String bookId;
    public String bookTitle;
    public String bookImageBase64;

    public long borrowedAt;
    public long dueDate;
    public String status;   // borrowed / pending_return / returned
    public String bookAuthor;

    public BorrowRecord() { }

    public BorrowRecord(String id,
                        String userId,
                        String userName,
                        String userEmail,
                        String bookId,
                        String bookTitle,
                        String bookImageBase64,
                        long borrowedAt,
                        long dueDate,
                        String status) {

        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;

        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookImageBase64 = bookImageBase64;

        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
        this.status = status;
    }
}
