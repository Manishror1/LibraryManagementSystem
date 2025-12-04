package com.manish.librarysystemfinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentInfoActivity extends AppCompatActivity {

    EditText etName, etCard;
    Button btnConfirm;

    String paymentType, bookId, bookTitle, bookAuthor, vendorId, fineId;
    double amount;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_info);

        etName = findViewById(R.id.etName);
        etCard = findViewById(R.id.etCard);
        btnConfirm = findViewById(R.id.btnConfirm);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Read incoming data
        paymentType = getIntent().getStringExtra("type");
        bookId = getIntent().getStringExtra("bookId");
        bookTitle = getIntent().getStringExtra("bookTitle");
        bookAuthor = getIntent().getStringExtra("bookAuthor");
        vendorId = getIntent().getStringExtra("vendorId");
        fineId = getIntent().getStringExtra("fineId");
        amount = getIntent().getDoubleExtra("amount", 0);

        if (bookAuthor == null) bookAuthor = "Unknown";

        btnConfirm.setOnClickListener(v -> process());
    }

    private void process() {
        String name = etName.getText().toString();
        String card = etCard.getText().toString();

        if (name.isEmpty() || card.length() < 12) {
            Toast.makeText(this, "Enter valid details", Toast.LENGTH_SHORT).show();
            return;
        }

        String masked = maskCard(card);

        String userId = auth.getUid();
        long now = System.currentTimeMillis();

        if ("borrow".equals(paymentType)) {
            saveBorrowPayment(userId, now);
        } else {
            saveFinePayment(userId, now);
        }

        generateReceipt(name, masked);
    }

    private String maskCard(String card) {
        String last4 = card.substring(card.length() - 4);
        return "**** **** **** " + last4;
    }

    // ---------------------------------------------------------
    // ⭐ BORROW PAYMENT
    // ---------------------------------------------------------
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

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------------------------------------------------
    // ⭐ FIXED — Save borrow record correctly in Firestore
    // ---------------------------------------------------------
    private void saveBorrowRecord(String userId) {

        long now = System.currentTimeMillis();
        long dueDate = now + (7L * 24 * 60 * 60 * 1000);

        Map<String, Object> record = new HashMap<>();
        record.put("userId", userId);
        record.put("userEmail", auth.getCurrentUser().getEmail());
        record.put("userName", auth.getCurrentUser().getEmail());  // displayName is always null
        record.put("bookId", bookId);
        record.put("bookTitle", bookTitle);
        record.put("bookAuthor", bookAuthor);
        record.put("borrowedAt", now);
        record.put("dueDate", dueDate);
        record.put("status", "borrowed");
        record.put("bookImageBase64", null);

        db.collection("borrow_records").add(record);
    }

    // ---------------------------------------------------------
    // ⭐ SAVE LOCAL ROOM COPY
    // ---------------------------------------------------------
    private void fetchImageAndSaveToRoom() {

        db.collection("books")
                .document(bookId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {

                    String imageBase64 = doc.exists() ? doc.getString("imageBase64") : null;
                    saveBorrowToRoom(imageBase64);
                })
                .addOnFailureListener(e -> saveBorrowToRoom(null));
    }

    private void saveBorrowToRoom(String imageBase64) {

        new Thread(() -> {

            BorrowedBook localBook = new BorrowedBook(
                    bookId,
                    bookTitle,
                    bookAuthor == null ? "Unknown" : bookAuthor,
                    imageBase64,
                    System.currentTimeMillis()
            );

            AppDatabase.getInstance(getApplicationContext())
                    .borrowedBookDao()
                    .insert(localBook);

        }).start();
    }

    // ---------------------------------------------------------
    // EARNINGS + COMMISSION + STOCK
    // ---------------------------------------------------------
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

    private void reduceStock() {
        if (bookId == null || bookId.isEmpty()) return;

        db.collection("books")
                .document(bookId)
                .update("stock", com.google.firebase.firestore.FieldValue.increment(-1));
    }

    // ---------------------------------------------------------
    // ⭐ FINE PAYMENT
    // ---------------------------------------------------------
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

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------------------------------------------------
    // ⭐ PDF RECEIPT
    // ---------------------------------------------------------
    private void generateReceipt(String name, String maskedCard) {

        String receiptId = "R" + System.currentTimeMillis();

        String pdfPath = PdfReceiptGenerator.createPDF(
                this,
                receiptId,
                name,
                maskedCard,
                bookTitle,
                amount,
                paymentType
        );

        saveReceiptToRoom(receiptId, pdfPath);

        Intent i = new Intent(this, ReceiptActivity.class);
        i.putExtra("receiptId", receiptId);
        i.putExtra("pdfPath", pdfPath);
        startActivity(i);
        finish();
    }

    private void saveReceiptToRoom(String receiptId, String pdfPath) {

        new Thread(() -> {

            ReceiptEntity r = new ReceiptEntity();
            r.receiptId = receiptId;
            r.pdfPath = pdfPath;
            r.userId = auth.getUid();
            r.paymentType = paymentType;
            r.bookId = bookId;
            r.bookTitle = bookTitle;
            r.amount = amount;
            r.timestamp = System.currentTimeMillis();

            AppDatabase
                    .getInstance(getApplicationContext())
                    .receiptDao()
                    .insert(r);

        }).start();
    }
}
