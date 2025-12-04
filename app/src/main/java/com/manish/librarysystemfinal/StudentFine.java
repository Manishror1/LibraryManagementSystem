package com.manish.librarysystemfinal;

public class StudentFine {

    public String id;        // Firestore document ID
    public String userId;    // student id
    public String bookId;    // which book caused fine
    public String bookTitle; // title
    public long dueDate;     // date when return was due
    public long returnedAt;  // actual return time
    public double amount;    // fine amount
    public boolean paid;     // NEW: whether librarian marked as paid

    // Empty constructor for Firestore
    public StudentFine() { }

    public StudentFine(String id,
                       String userId,
                       String bookId,
                       String bookTitle,
                       long dueDate,
                       long returnedAt,
                       double amount,
                       boolean paid) {

        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.dueDate = dueDate;
        this.returnedAt = returnedAt;
        this.amount = amount;
        this.paid = paid;
    }


}
