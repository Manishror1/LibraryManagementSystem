package com.manish.librarysystemfinal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class VendorAddBookActivity extends AppCompatActivity {

    EditText etTitle, etAuthor, etPrice, etStock;
    ImageView imgPreview;
    Button btnPickImage, btnAddBook;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String base64Image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_add_book);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        imgPreview = findViewById(R.id.imgPreview);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnAddBook = findViewById(R.id.btnAddBook);

        btnPickImage.setOnClickListener(v -> pickImage.launch("image/*"));
        btnAddBook.setOnClickListener(v -> saveBook());
    }

    // Pick image
    ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imgPreview.setImageURI(uri);
                    convertImageToBase64(uri);
                }
            });

    // Convert image to Base64
    private void convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] bytes = stream.toByteArray();

            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(this, "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Save to Firestore
    private void saveBook() {

        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (base64Image.isEmpty()) {
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;

        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or stock", Toast.LENGTH_SHORT).show();
            return;
        }

        String vendorId = auth.getCurrentUser().getUid();

        String id = db.collection("books").document().getId();

        // status = "pending" (for librarian approval later)
        Book book = new Book(
                id,
                title,
                author,
                price,
                stock,
                base64Image,
                vendorId,
                "pending"
        );

        db.collection("books").document(id).set(book)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Book Added (Pending Approval)", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
