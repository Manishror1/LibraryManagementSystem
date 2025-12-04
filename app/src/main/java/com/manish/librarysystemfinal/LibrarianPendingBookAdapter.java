package com.manish.librarysystemfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LibrarianPendingBookAdapter extends RecyclerView.Adapter<LibrarianPendingBookAdapter.ViewHolder> {

    Context context;
    ArrayList<BorrowRecord> list;
    LibrarianApproveRequestsActivity activity;

    public LibrarianPendingBookAdapter(Context context, ArrayList<BorrowRecord> list) {
        this.context = context;
        this.list = list;
        this.activity = (LibrarianApproveRequestsActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pending_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        BorrowRecord r = list.get(pos);

        h.txtTitle.setText(r.bookTitle);
        h.txtAuthor.setText("Author: " + r.bookAuthor);
        h.txtStudent.setText("Student: " + r.userName);
        h.txtEmail.setText(r.userEmail);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        h.txtBorrowDate.setText("Borrowed: " + sdf.format(new Date(r.borrowedAt)));
        h.txtDueDate.setText("Due: " + sdf.format(new Date(r.dueDate)));

        if (r.bookImageBase64 != null) {
            byte[] bytes = android.util.Base64.decode(r.bookImageBase64, android.util.Base64.DEFAULT);
            h.imgBook.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        // APPROVE RETURN
        h.btnApprove.setOnClickListener(v -> activity.approveReturn(r));

        // ADD FINE WITH PREDEFINED AMOUNTS
        h.btnFine.setOnClickListener(v -> showFineAmountDialog(r));
    }

    // ⭐ Fine Selection Dialog (₹10, ₹20, ₹30)
    private void showFineAmountDialog(BorrowRecord r) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reason_select, null);
        Spinner spinner = dialogView.findViewById(R.id.spinnerReasons);

        // REUSE THIS LAYOUT, BUT WITH FINE OPTIONS
        String[] fineOptions = {"$10", "$20", "$30"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, fineOptions);

        spinner.setAdapter(adapter);

        new AlertDialog.Builder(context)
                .setTitle("Select Fine Amount")
                .setView(dialogView)
                .setPositiveButton("Confirm", (d, w) -> {

                    String choice = spinner.getSelectedItem().toString();
                    choice = choice.replace("$", "").trim();
                    double fineAmount = Double.parseDouble(choice);

                    saveFineToFirestore(r, fineAmount);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveFineToFirestore(BorrowRecord r, double fineAmount) {

        long now = System.currentTimeMillis();

        String fineId = FirebaseFirestore.getInstance().collection("fines").document().getId();

        StudentFine fine = new StudentFine(
                fineId,
                r.userId,
                r.bookId,
                r.bookTitle,
                r.dueDate,
                now,
                fineAmount,
                false
        );

        FirebaseFirestore.getInstance()
                .collection("fines")
                .document(fineId)
                .set(fine)
                .addOnSuccessListener(a ->
                        Toast.makeText(context,
                                "Fine added: ₹" + fineAmount,
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtAuthor, txtStudent, txtEmail, txtBorrowDate, txtDueDate;
        ImageView imgBook;
        Button btnApprove, btnFine;

        public ViewHolder(@NonNull View v) {
            super(v);

            txtTitle = v.findViewById(R.id.txtPendingTitle);
            txtAuthor = v.findViewById(R.id.txtPendingAuthor);
            txtStudent = v.findViewById(R.id.txtPendingStudent);
            txtEmail = v.findViewById(R.id.txtPendingEmail);
            txtBorrowDate = v.findViewById(R.id.txtPendingBorrowDate);
            txtDueDate = v.findViewById(R.id.txtPendingDueDate);
            imgBook = v.findViewById(R.id.imgPendingBook);

            btnApprove = v.findViewById(R.id.btnApproveBook);
            btnFine = v.findViewById(R.id.btnFineBook);
        }
    }
}
