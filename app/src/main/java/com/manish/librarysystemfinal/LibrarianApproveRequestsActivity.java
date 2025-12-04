package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class LibrarianApproveRequestsActivity extends AppCompatActivity {

    RecyclerView recyclerPendingBooks;
    ArrayList<BorrowRecord> pendingList = new ArrayList<>();
    LibrarianPendingBookAdapter adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_librarian_approve_requests);

        db = FirebaseFirestore.getInstance();

        recyclerPendingBooks = findViewById(R.id.recyclerPendingBooks);
        recyclerPendingBooks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LibrarianPendingBookAdapter(this, pendingList);
        recyclerPendingBooks.setAdapter(adapter);

        loadReturnRequests();
    }

    // ⭐ Load all pending return requests
    private void loadReturnRequests() {
        db.collection("return_requests")
                .whereEqualTo("status", "pending_return")
                .get()
                .addOnSuccessListener(snaps -> {

                    pendingList.clear();

                    for (DocumentSnapshot d : snaps) {
                        BorrowRecord r = d.toObject(BorrowRecord.class);

                        if (r != null) {
                            // We need the return_request doc id as well (for update)
                            r.id = d.getId();
                            pendingList.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (pendingList.isEmpty()) {
                        Toast.makeText(this, "No pending return requests.", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ⭐ Librarian approves book return
    public void approveReturn(BorrowRecord r) {

        long now = System.currentTimeMillis();

        // 1️⃣ Update return_requests → status = returned
        db.collection("return_requests")
                .document(r.id)   // r.id is the return_requests doc id
                .update(
                        "status", "returned",
                        "returnedAt", now
                )
                .addOnSuccessListener(a -> {

                    // 2️⃣ Increase stock in books collection
                    db.collection("books")
                            .document(r.bookId)
                            .get()
                            .addOnSuccessListener(bookDoc -> {

                                Long stockObj = bookDoc.getLong("stock");
                                int stock = stockObj == null ? 0 : stockObj.intValue();

                                db.collection("books")
                                        .document(r.bookId)
                                        .update("stock", stock + 1);
                            });

                    // 3️⃣ Update borrow_records status → returned (for reporting/history)
                    db.collection("borrow_records")
                            .whereEqualTo("userId", r.userId)
                            .whereEqualTo("bookId", r.bookId)
                            .whereEqualTo("status", "borrowed")
                            .get()
                            .addOnSuccessListener(snap -> {
                                for (DocumentSnapshot d : snap) {
                                    d.getReference().update("status", "returned");
                                }
                            });

                    // 4️⃣ Remove from local Room DB (so student dashboard no longer shows it)
                    new Thread(() -> {
                        AppDatabase.getInstance(getApplicationContext())
                                .borrowedBookDao()
                                .deleteByBookId(r.bookId);
                    }).start();

                    Toast.makeText(this,
                            "Return approved!",
                            Toast.LENGTH_SHORT).show();

                    // 5️⃣ Refresh pending list
                    loadReturnRequests();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // ⭐ Librarian adds fine manually if needed (unchanged)
    public void addFine(BorrowRecord r) {

        long now = System.currentTimeMillis();
        long delay = now - r.dueDate;

        if (delay <= 0) {
            Toast.makeText(this, "Book is not overdue!", Toast.LENGTH_SHORT).show();
            return;
        }

        long daysLate = delay / (24 * 60 * 60 * 1000);
        double fineAmount = daysLate * 2;  // 2 per day

        String fineId = db.collection("fines").document().getId();

        StudentFine fine = new StudentFine(
                fineId,
                r.userId,
                r.bookId,
                r.bookTitle,
                r.dueDate,
                now,
                fineAmount,
                false
        );

        db.collection("fines").document(fineId)
                .set(fine)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Fine added: " + fineAmount,
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
