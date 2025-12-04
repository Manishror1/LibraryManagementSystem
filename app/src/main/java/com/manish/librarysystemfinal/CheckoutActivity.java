package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    TextView tvTitle, tvAmount, tvDesc;
    Button btnConfirmPay, btnCancel;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String type = "";
    double amount = 0;
    String bookId = "";
    String bookTitle = "";
    String bookAuthor = "";
    String vendorId = "";
    String fineId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvTitle = findViewById(R.id.tvCheckoutTitle);
        tvAmount = findViewById(R.id.tvCheckoutAmount);
        tvDesc = findViewById(R.id.tvCheckoutDesc);

        btnConfirmPay = findViewById(R.id.btnCheckoutConfirm);
        btnCancel = findViewById(R.id.btnCheckoutCancel);

        // -------- READ DATA FROM INTENT --------
        type = getIntent().getStringExtra("type");
        amount = getIntent().getDoubleExtra("amount", 0);
        bookId = getIntent().getStringExtra("bookId");
        bookTitle = getIntent().getStringExtra("bookTitle");

        // ⭐ NEW — FIXED AUTHOR
        bookAuthor = getIntent().getStringExtra("bookAuthor");
        if (bookAuthor == null) bookAuthor = "Unknown";

        vendorId = getIntent().getStringExtra("vendorId");
        fineId = getIntent().getStringExtra("fineId");

        tvAmount.setText("$" + amount);

        if ("fine".equals(type)) {
            tvTitle.setText("Pay Fine");
            tvDesc.setText("You are paying a fine for: " + bookTitle);
        } else {
            tvTitle.setText("Borrow Book");
            tvDesc.setText("You are purchasing access to: " + bookTitle);
        }

        btnCancel.setOnClickListener(v -> finish());

        // ⭐ REDIRECT TO PAYMENT INFO ACTIVITY
        btnConfirmPay.setOnClickListener(v -> openPaymentInfo());
    }

    // ⭐ NEW METHOD: Redirect to PaymentInfoActivity
    private void openPaymentInfo() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CheckoutActivity.this, PaymentInfoActivity.class);

        // Send all necessary data:
        intent.putExtra("type", type);
        intent.putExtra("amount", amount);
        intent.putExtra("bookId", bookId);
        intent.putExtra("bookTitle", bookTitle);
        intent.putExtra("bookAuthor", bookAuthor);    // ⭐ FIXED
        intent.putExtra("vendorId", vendorId);
        intent.putExtra("fineId", fineId);

        startActivity(intent);
    }


    // ---------------- FINE PAYMENT --------------------
    private void saveFinePayment(String userId, long timestamp) {

        Map<String, Object> map = new HashMap<>();
        map.put("studentId", userId);
        map.put("amount", amount);
        map.put("bookTitle", bookTitle);
        map.put("timestamp", timestamp);

        db.collection("payments_fines")
                .add(map)
                .addOnSuccessListener(a -> {

                    if (fineId != null && !fineId.isEmpty()) {
                        db.collection("fines").document(fineId).delete();
                    }

                    Toast.makeText(this, "Fine Paid Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------- BORROW PAYMENT --------------------
    private void saveBorrowPayment(String userId, long timestamp) {

        Map<String, Object> map = new HashMap<>();
        map.put("studentId", userId);
        map.put("vendorId", vendorId);
        map.put("bookId", bookId);
        map.put("bookTitle", bookTitle);
        map.put("amount", amount);
        map.put("timestamp", timestamp);

        db.collection("payments_borrow")
                .add(map)
                .addOnSuccessListener(a -> {

                    saveBorrowRecord(userId);
                    updateVendorEarnings();
                    updateAdminCommission();
                    reduceStock();
                    fetchImageAndSaveToRoom();

                    Toast.makeText(this, "Book Borrowed Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------- Save borrow record in Firestore ----------
    private void saveBorrowRecord(String userId) {

        long now = System.currentTimeMillis();
        long dueDate = now + (7L * 24 * 60 * 60 * 1000);

        Map<String, Object> record = new HashMap<>();
        record.put("userId", userId);
        record.put("bookId", bookId);
        record.put("bookTitle", bookTitle);   // ⭐ FIXED
        record.put("userEmail", auth.getCurrentUser().getEmail());
        record.put("borrowedAt", now);
        record.put("dueDate", dueDate);
        record.put("status", "borrowed");

        db.collection("borrow_records")
                .add(record);
    }

    // ---------- Load image from Firestore and save to Room ----------
    private void fetchImageAndSaveToRoom() {

        db.collection("books")
                .document(bookId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {

                    String imageBase64 = doc.exists() ? doc.getString("imageBase64") : null;
                    saveToLocalRoom(imageBase64);
                })
                .addOnFailureListener(e -> saveToLocalRoom(null));
    }

    private void saveToLocalRoom(String imageBase64) {

        new Thread(() -> {

            // ⭐ FIXED — ALWAYS USE VALID TITLE & AUTHOR
            BorrowedBook localBook = new BorrowedBook(
                    bookId,
                    bookTitle,
                    bookAuthor,
                    imageBase64,
                    System.currentTimeMillis()
            );

            AppDatabase.getInstance(getApplicationContext())
                    .borrowedBookDao()
                    .insert(localBook);

        }).start();
    }

    // ---------- Vendor earnings ----------
    private void updateVendorEarnings() {

        db.collection("vendor_earnings")
                .document(vendorId)
                .get()
                .addOnSuccessListener(doc -> {

                    double old = 0;

                    if (doc.exists() && doc.getDouble("total") != null) {
                        old = doc.getDouble("total");
                    }

                    double newAmount = old + amount;

                    Map<String, Object> data = new HashMap<>();
                    data.put("total", newAmount);

                    db.collection("vendor_earnings")
                            .document(vendorId)
                            .set(data);
                });
    }

    // ---------- Admin 10% commission ----------
    private void updateAdminCommission() {

        db.collection("commission")
                .document("admin")
                .get()
                .addOnSuccessListener(doc -> {

                    double old = 0;

                    if (doc.exists() && doc.getDouble("total") != null) {
                        old = doc.getDouble("total");
                    }

                    double finalAmount = old + (amount * 0.10);

                    Map<String, Object> data = new HashMap<>();
                    data.put("total", finalAmount);

                    db.collection("commission")
                            .document("admin")
                            .set(data);
                });
    }

    // ---------- Reduce stock ----------
    private void reduceStock() {
        if (bookId == null || bookId.isEmpty()) return;

        db.collection("books")
                .document(bookId)
                .update("stock", com.google.firebase.firestore.FieldValue.increment(-1));
    }
}
