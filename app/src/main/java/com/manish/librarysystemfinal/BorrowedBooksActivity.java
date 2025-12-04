package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BorrowedBooksActivity extends AppCompatActivity {

    RecyclerView recycler;
    BorrowedBooksListAdapter adapter;
    List<BorrowedBook> list = new ArrayList<>();
    TextView txtNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);

        recycler = findViewById(R.id.recyclerBorrowed);
        txtNoData = findViewById(R.id.txtNoBorrowed);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        loadBorrowedBooks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBorrowedBooks(); // auto-refresh when returning
    }

    private void loadBorrowedBooks() {

        AppDatabase db = AppDatabase.getInstance(this);
        BorrowedBookDao dao = db.borrowedBookDao();

        list = dao.getAll();

        if (list == null || list.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);

            adapter = new BorrowedBooksListAdapter(this, list);
            recycler.setAdapter(adapter);
        }
    }
}
