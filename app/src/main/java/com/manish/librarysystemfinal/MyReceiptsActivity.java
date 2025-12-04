package com.manish.librarysystemfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyReceiptsActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<ReceiptEntity> list = new ArrayList<>();
    ReceiptAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_receipts);

        recycler = findViewById(R.id.recyclerReceipts);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceiptAdapter(this, list);
        recycler.setAdapter(adapter);

        loadReceipts();
    }

    private void loadReceipts() {
        new Thread(() -> {
            List<ReceiptEntity> data =
                    AppDatabase.getInstance(getApplicationContext())
                            .receiptDao()
                            .getReceiptsByUser(FirebaseAuth.getInstance().getUid());

            runOnUiThread(() -> {
                list.clear();
                list.addAll(data);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
