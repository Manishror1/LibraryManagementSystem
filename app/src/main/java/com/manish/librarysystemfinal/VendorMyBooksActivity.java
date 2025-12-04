package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class VendorMyBooksActivity extends AppCompatActivity {

    RecyclerView recyclerVendorBooks;
    ArrayList<Book> bookList = new ArrayList<>();
    VendorBooksAdapter adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_my_books);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerVendorBooks = findViewById(R.id.recyclerVendorBooks);
        recyclerVendorBooks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VendorBooksAdapter(this, bookList);
        recyclerVendorBooks.setAdapter(adapter);

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMyBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when user returns after resubmitting a rejected book
        loadMyBooks();
    }

    private void loadMyBooks() {

        String vendorId = auth.getCurrentUser().getUid();

        db.collection("books")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnSuccessListener(snaps -> {

                    bookList.clear();

                    for (DocumentSnapshot d : snaps) {
                        Book b = d.toObject(Book.class);
                        if (b != null) {
                            b.id = d.getId();
                            bookList.add(b);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        Toast.makeText(this,
                                "You have not added any books yet.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load books: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
