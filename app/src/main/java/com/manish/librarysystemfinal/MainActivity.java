package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    RecyclerView recyclerRandomBooks;
    ArrayList<Book> randomBooks = new ArrayList<>();
    BookAdapter bookAdapter;

    LinearLayout layoutGuestButtons;
    CardView layoutLoggedIn;

    TextView tvWelcome, tvRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerRandomBooks = findViewById(R.id.recyclerRandomBooks);
        recyclerRandomBooks.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        bookAdapter = new BookAdapter(MainActivity.this, randomBooks, false);
        recyclerRandomBooks.setAdapter(bookAdapter);

        layoutGuestButtons = findViewById(R.id.layoutGuestButtons);
        layoutLoggedIn = findViewById(R.id.layoutLoggedIn);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);

        loadRandomBooks();
        checkLoginState();

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.btnVisitor).setOnClickListener(v ->
                Toast.makeText(this, "Browsing as guest", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnGoDashboard).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));

        findViewById(R.id.navBooks).setOnClickListener(v ->
                startActivity(new Intent(this, AllBooksActivity.class)));

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            if (auth.getCurrentUser() == null)
                startActivity(new Intent(this, LoginActivity.class));
            else
                startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.navHome).setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show());
    }

    private void checkLoginState() {
        if (auth.getCurrentUser() == null) {
            layoutGuestButtons.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
        } else {
            layoutGuestButtons.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);

            String uid = auth.getCurrentUser().getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                tvWelcome.setText("Welcome, " + doc.getString("name") + " 👋");
                tvRole.setText("Role: " + doc.getString("role"));
            });
        }
    }

    private void loadRandomBooks() {
        db.collection("books")
                .limit(3)
                .get()
                .addOnSuccessListener(q -> {
                    randomBooks.clear();
                    for (DocumentSnapshot d : q) {
                        Book b = d.toObject(Book.class);
                        if (b != null) {
                            b.id = d.getId();
                            randomBooks.add(b);
                        }
                    }
                    bookAdapter.notifyDataSetChanged();
                });
    }
}
