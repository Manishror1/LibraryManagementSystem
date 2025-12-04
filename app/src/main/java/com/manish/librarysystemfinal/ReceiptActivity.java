package com.manish.librarysystemfinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class ReceiptActivity extends AppCompatActivity {

    TextView tvSuccess, tvPath;
    Button btnOpen;
    String pdfPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        pdfPath = getIntent().getStringExtra("pdfPath");

        tvSuccess = findViewById(R.id.tvSuccess);
        tvPath = findViewById(R.id.tvPath);
        btnOpen = findViewById(R.id.btnOpen);

        tvPath.setText(pdfPath);

        btnOpen.setOnClickListener(v -> openPdf());
    }

    private void openPdf() {
        File file = new File(pdfPath);

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        startActivity(intent);
    }
}
