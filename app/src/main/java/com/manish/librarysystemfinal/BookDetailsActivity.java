package com.manish.librarysystemfinal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class BookDetailsActivity extends AppCompatActivity {

    TextView txtTitle, txtAuthor, txtPrice, txtStock, txtAvgRating, txtReviewCount;
    ImageView imgBook;
    Button btnBorrow, btnWriteReview;

    RecyclerView recyclerReviews;
    ReviewAdapter reviewAdapter;
    ArrayList<Review> reviewsList = new ArrayList<>();

    FirebaseFirestore db;
    FirebaseAuth auth;

    String bookId;
    Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        txtTitle = findViewById(R.id.txtTitle);
        txtAuthor = findViewById(R.id.txtAuthor);
        txtPrice = findViewById(R.id.txtPrice);
        txtStock = findViewById(R.id.txtStock);
        imgBook = findViewById(R.id.imgBook);
        btnBorrow = findViewById(R.id.btnBorrow);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        txtAvgRating = findViewById(R.id.txtAvgRating);
        txtReviewCount = findViewById(R.id.txtReviewCount);

        recyclerReviews = findViewById(R.id.recyclerReviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewsList);
        recyclerReviews.setAdapter(reviewAdapter);

        bookId = getIntent().getStringExtra("bookId");

        loadBookDetails();

        btnBorrow.setOnClickListener(v -> {

            if (auth.getCurrentUser() == null) {
                showLoginDialog();
                return;
            }

            if (book == null) {
                showAlert("Book details not loaded yet.");
                return;
            }

            if (book.stock <= 0) {
                showAlert("This book is out of stock.");
                return;
            }

            // ⭐ FIXED INTENT — NOW PASSES CORRECT VALUES
            Intent intent = new Intent(BookDetailsActivity.this, CheckoutActivity.class);
            intent.putExtra("type", "borrow");
            intent.putExtra("amount", book.price);
            intent.putExtra("bookId", book.id);
            intent.putExtra("vendorId", book.vendorId);
            intent.putExtra("bookTitle", book.title);
            intent.putExtra("bookAuthor", book.author);  // ⭐ IMPORTANT
            startActivity(intent);
        });

        btnWriteReview.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                showLoginDialog();
                return;
            }
            showReviewDialog();
        });
    }

    private void loadBookDetails() {
        db.collection("books").document(bookId)
                .get()
                .addOnSuccessListener(doc -> {

                    book = doc.toObject(Book.class);

                    if (book == null) {
                        Toast.makeText(this, "Error loading book", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    book.id = doc.getId();

                    txtTitle.setText(book.title);
                    txtAuthor.setText(book.author);
                    txtPrice.setText("$" + book.price);
                    txtStock.setText("Stock: " + book.stock);

                    if (book.imageBase64 != null) {
                        byte[] bytes = Base64.decode(book.imageBase64, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgBook.setImageBitmap(bmp);
                    }

                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showReviewDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);

        new AlertDialog.Builder(this)
                .setTitle("Write Review")
                .setView(view)
                .setPositiveButton("Submit", (d, w) -> {

                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveReviewToFirestore(rating, comment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveReviewToFirestore(float rating, String comment) {

        String userId = auth.getCurrentUser().getUid();
        String userName = auth.getCurrentUser().getDisplayName();
        if (userName == null) userName = "Student";

        String reviewId = db.collection("reviews").document().getId();

        Review review = new Review(
                reviewId,
                book.id,
                userId,
                userName,
                rating,
                comment,
                System.currentTimeMillis()
        );

        db.collection("reviews")
                .document(reviewId)
                .set(review)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                    loadReviews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadReviews() {

        if (book == null || book.id == null) return;

        db.collection("reviews")
                .whereEqualTo("bookId", book.id)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {

                    reviewsList.clear();

                    float totalRating = 0;
                    int count = 0;

                    for (DocumentSnapshot d : snaps) {
                        Review r = d.toObject(Review.class);
                        if (r != null) {
                            reviewsList.add(r);
                            totalRating += r.rating;
                            count++;
                        }
                    }

                    reviewAdapter.notifyDataSetChanged();

                    if (count > 0) {
                        float avg = totalRating / count;
                        txtAvgRating.setText(String.format("Rating: %.1f ★", avg));
                        txtReviewCount.setText(count + " Reviews");
                    } else {
                        txtAvgRating.setText("Rating: 0.0 ★");
                        txtReviewCount.setText("No reviews yet");
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load reviews: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void showAlert(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("Please login to borrow books.")
                .setPositiveButton("Login", (d, w) ->
                        startActivity(new Intent(this, LoginActivity.class)))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
