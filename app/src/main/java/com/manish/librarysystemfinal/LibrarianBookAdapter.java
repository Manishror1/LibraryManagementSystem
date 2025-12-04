package com.manish.librarysystemfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LibrarianBookAdapter extends RecyclerView.Adapter<LibrarianBookAdapter.ViewHolder> {

    Context context;
    ArrayList<Book> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LibrarianBookAdapter(Context context, ArrayList<Book> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_librarian_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Book b = list.get(pos);

        h.tvTitle.setText(b.title);
        h.tvAuthor.setText("by " + b.author);
        h.tvPrice.setText("Price: ₹" + b.price);
        h.tvStock.setText("Stock: " + b.stock);

        String status = (b.status == null || b.status.isEmpty()) ? "approved" : b.status;
        h.tvStatus.setText("Status: " + status);

        if ("pending".equalsIgnoreCase(status)) {
            h.btnApprove.setVisibility(View.VISIBLE);
            h.btnReject.setVisibility(View.VISIBLE);
            h.btnEdit.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.GONE);
        } else {
            h.btnApprove.setVisibility(View.GONE);
            h.btnReject.setVisibility(View.GONE);
            h.btnEdit.setVisibility(View.VISIBLE);
            h.btnDelete.setVisibility(View.VISIBLE);
        }

        // ✔ APPROVE BOOK
        h.btnApprove.setOnClickListener(v -> {
            db.collection("books")
                    .document(b.id)
                    .update("status", "approved")
                    .addOnSuccessListener(a -> {
                        Toast.makeText(context, "Book approved", Toast.LENGTH_SHORT).show();
                        list.remove(pos);
                        notifyItemRemoved(pos);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // ❗✔ REJECT BOOK → with reason dialog
        h.btnReject.setOnClickListener(v -> showReasonDialog(b, pos));

        // ✔ EDIT
        h.btnEdit.setOnClickListener(v -> showEditDialog(b, pos));

        // ✔ DELETE
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.collection("books")
                                .document(b.id)
                                .delete()
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                                    list.remove(pos);
                                    notifyItemRemoved(pos);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ------------------------------------------------------------
    // ⭐ REJECT BOOK WITH REASON SELECTION
    // ------------------------------------------------------------
    private void showReasonDialog(Book b, int pos) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reason_select, null);
        Spinner spinner = dialogView.findViewById(R.id.spinnerReasons);

        // Predefined reasons
        String[] reasons = {
                "Incorrect book details",
                "Low quality cover image",
                "Duplicate submission",
                "Price too high",
                "Not suitable for library",
                "Incomplete information",
                "Others"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_dropdown_item, reasons);
        spinner.setAdapter(adapter);

        new AlertDialog.Builder(context)
                .setTitle("Select Rejection Reason")
                .setView(dialogView)
                .setPositiveButton("Reject", (d, w) -> {

                    String reason = spinner.getSelectedItem().toString();

                    // 1️⃣ UPDATE FIRESTORE
                    db.collection("books")
                            .document(b.id)
                            .update("status", "rejected")
                            .addOnSuccessListener(a -> {

                                // 2️⃣ SAVE TO ROOM DB
                                saveRejectedToLocalDB(b, reason);

                                Toast.makeText(context, "Book rejected", Toast.LENGTH_SHORT).show();

                                list.remove(pos);
                                notifyItemRemoved(pos);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------------------------------------------
    // ⭐ SAVE REJECTED BOOK LOCALLY WITH REASON
    // ------------------------------------------------------------
    private void saveRejectedToLocalDB(Book b, String reason) {

        RejectedBook rejectedBook = new RejectedBook(
                b.id,
                b.vendorId,
                b.title,
                b.author,
                b.price,
                b.stock,
                b.imageBase64,
                reason,
                System.currentTimeMillis()
        );

        new Thread(() -> {
            AppDatabase.getInstance(context)
                    .rejectedBookDao()
                    .insert(rejectedBook);
        }).start();
    }

    // ------------------------------------------------------------
    // ✔ EDIT BOOK (already existing)
    // ------------------------------------------------------------
    private void showEditDialog(Book b, int pos) {

        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_book, null);

        TextView etPrice = dialogView.findViewById(R.id.etEditPrice);
        TextView etStock = dialogView.findViewById(R.id.etEditStock);

        etPrice.setText(String.valueOf(b.price));
        etStock.setText(String.valueOf(b.stock));

        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    try {
                        double newPrice = Double.parseDouble(etPrice.getText().toString().trim());
                        int newStock = Integer.parseInt(etStock.getText().toString().trim());

                        db.collection("books")
                                .document(b.id)
                                .update("price", newPrice, "stock", newStock)
                                .addOnSuccessListener(a -> {
                                    b.price = newPrice;
                                    b.stock = newStock;
                                    notifyItemChanged(pos);
                                    Toast.makeText(context, "Book updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    } catch (Exception ex) {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvAuthor, tvPrice, tvStock, tvStatus;
        Button btnApprove, btnReject, btnEdit, btnDelete;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvTitle = v.findViewById(R.id.tvLibBookTitle);
            tvAuthor = v.findViewById(R.id.tvLibBookAuthor);
            tvPrice = v.findViewById(R.id.tvLibBookPrice);
            tvStock = v.findViewById(R.id.tvLibBookStock);
            tvStatus = v.findViewById(R.id.tvLibBookStatus);

            btnApprove = v.findViewById(R.id.btnLibApprove);
            btnReject = v.findViewById(R.id.btnLibReject);
            btnEdit = v.findViewById(R.id.btnLibEdit);
            btnDelete = v.findViewById(R.id.btnLibDelete);
        }
    }
}
