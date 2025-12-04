package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LibrarianApproveReturnsActivity extends AppCompatActivity {

    RecyclerView recyclerPendingReturns;
    ArrayList<BorrowRecord> pendingList = new ArrayList<>();
    LibrarianPendingReturnAdapter adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_librarian_approve_returns);

        db = FirebaseFirestore.getInstance();

        recyclerPendingReturns = findViewById(R.id.recyclerPendingReturns);
        recyclerPendingReturns.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LibrarianPendingReturnAdapter(this, pendingList);
        recyclerPendingReturns.setAdapter(adapter);

        loadPendingReturns();
    }

    // Load all pending return requests
    private void loadPendingReturns() {
        db.collection("return_requests")
                .whereEqualTo("status", "pending_return")
                .get()
                .addOnSuccessListener(snaps -> {
                    pendingList.clear();

                    for (DocumentSnapshot d : snaps) {
                        BorrowRecord r = d.toObject(BorrowRecord.class);
                        if (r != null) pendingList.add(r);
                    }

                    adapter.notifyDataSetChanged();

                    if (pendingList.isEmpty()) {
                        Toast.makeText(this, "No pending returns.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void approveReturn(BorrowRecord r) {

        long now = System.currentTimeMillis();

        // Update return request
        db.collection("return_requests")
                .document(r.id)
                .update("status", "returned", "returnedAt", now)
                .addOnSuccessListener(a -> {

                    // Increase book stock
                    db.collection("books")
                            .document(r.bookId)
                            .get()
                            .addOnSuccessListener(bookDoc -> {
                                Long stock = bookDoc.getLong("stock");
                                if (stock == null) stock = 0L;

                                db.collection("books")
                                        .document(r.bookId)
                                        .update("stock", stock + 1);
                            });

                    Toast.makeText(this, "Return approved!", Toast.LENGTH_SHORT).show();
                    loadPendingReturns();
                });
    }

    public void addFine(BorrowRecord r) {

        long now = System.currentTimeMillis();
        long delay = now - r.dueDate;

        if (delay <= 0) {
            Toast.makeText(this, "Not overdue!", Toast.LENGTH_SHORT).show();
            return;
        }

        long daysLate = delay / (24 * 60 * 60 * 1000);
        double fineAmount = daysLate * 2;

        StudentFine fine = new StudentFine(
                null,
                r.userId,
                r.bookId,
                r.bookTitle,
                r.dueDate,
                now,
                fineAmount,
                false
        );

        String id = db.collection("fines").document().getId();
        fine.id = id;

        db.collection("fines").document(id)
                .set(fine)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Fine added: " + fineAmount,
                                Toast.LENGTH_SHORT).show());
    }
}
