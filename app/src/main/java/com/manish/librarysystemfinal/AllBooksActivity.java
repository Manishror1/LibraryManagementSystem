package com.manish.librarysystemfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AllBooksActivity extends AppCompatActivity {

    RecyclerView recyclerBooks;
    EditText etSearch;

    ArrayList<Book> books = new ArrayList<>();
    BookAdapter adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_books);

        db = FirebaseFirestore.getInstance();

        recyclerBooks = findViewById(R.id.recyclerBooks);
        etSearch = findViewById(R.id.etSearch);

        recyclerBooks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(this, books, false);
        recyclerBooks.setAdapter(adapter);

        loadBooks(); // only approved books

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ⭐ Only show approved books
    private void loadBooks() {
        db.collection("books")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(snaps -> {
                    books.clear();
                    for (DocumentSnapshot d : snaps) {
                        Book b = d.toObject(Book.class);
                        if (b != null) {
                            b.id = d.getId();
                            books.add(b);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void filterBooks(String text) {
        ArrayList<Book> filtered = new ArrayList<>();

        for (Book b : books) {
            if (b.title.toLowerCase().contains(text.toLowerCase()) ||
                    b.author.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(b);
            }
        }

        adapter = new BookAdapter(this, filtered, false);
        recyclerBooks.setAdapter(adapter);
    }
}
