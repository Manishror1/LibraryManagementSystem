package com.manish.librarysystemfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class VendorBooksAdapter extends RecyclerView.Adapter<VendorBooksAdapter.ViewHolder> {

    Context context;
    ArrayList<Book> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public VendorBooksAdapter(Context context, ArrayList<Book> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_vendor_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        Book b = list.get(pos);

        h.tvTitle.setText(b.title);
        h.tvAuthor.setText("Author: " + b.author);
        h.tvPrice.setText("Price: $" + b.price);
        h.tvStock.setText("Stock: " + b.stock);
        h.tvStatus.setText("Status: " + (b.status == null ? "approved" : b.status));

        // Load image if available
        try {
            if (b.imageBase64 != null && !b.imageBase64.isEmpty()) {
                byte[] bytes = android.util.Base64.decode(b.imageBase64, android.util.Base64.DEFAULT);
                h.imgBook.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        } catch (Exception ignored) {}

        // DELETE
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Book")
                    .setMessage("Are you sure you want to delete this book?")
                    .setPositiveButton("Yes", (d, w) -> deleteBook(b, pos))
                    .setNegativeButton("No", null)
                    .show();
        });

        // UPDATE STOCK
        h.btnStock.setOnClickListener(v -> showStockDialog(b, pos));

        // EDIT basic details (title, author, price)
        h.btnEdit.setOnClickListener(v -> showEditDialog(b, pos));
    }

    private void deleteBook(Book b, int pos) {
        db.collection("books").document(b.id)
                .delete()
                .addOnSuccessListener(a -> {
                    list.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, "Book deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showStockDialog(Book b, int pos) {
        EditText input = new EditText(context);
        input.setHint("New stock");
        input.setText(String.valueOf(b.stock));

        new AlertDialog.Builder(context)
                .setTitle("Update Stock")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (val.isEmpty()) {
                        Toast.makeText(context, "Stock required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int newStock = Integer.parseInt(val);
                        db.collection("books").document(b.id)
                                .update("stock", newStock)
                                .addOnSuccessListener(a -> {
                                    b.stock = newStock;
                                    notifyItemChanged(pos);
                                    Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } catch (NumberFormatException ex) {
                        Toast.makeText(context, "Invalid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Book b, int pos) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_book, null);

        EditText etTitle = dialogView.findViewById(R.id.etEditTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etEditAuthor);
        EditText etPrice = dialogView.findViewById(R.id.etEditPrice);

        etTitle.setText(b.title);
        etAuthor.setText(b.author);
        etPrice.setText(String.valueOf(b.price));

        new AlertDialog.Builder(context)
                .setTitle("Edit Book")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newAuthor = etAuthor.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (newTitle.isEmpty() || newAuthor.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double newPrice;
                    try {
                        newPrice = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid price", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("books").document(b.id)
                            .update(
                                    "title", newTitle,
                                    "author", newAuthor,
                                    "price", newPrice
                            )
                            .addOnSuccessListener(a -> {
                                b.title = newTitle;
                                b.author = newAuthor;
                                b.price = newPrice;
                                notifyItemChanged(pos);
                                Toast.makeText(context, "Book updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvAuthor, tvPrice, tvStock, tvStatus;
        ImageView imgBook;
        Button btnEdit, btnDelete, btnStock;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvTitle = v.findViewById(R.id.tvVendorTitle);
            tvAuthor = v.findViewById(R.id.tvVendorAuthor);
            tvPrice = v.findViewById(R.id.tvVendorPrice);
            tvStock = v.findViewById(R.id.tvVendorStock);
            tvStatus = v.findViewById(R.id.tvVendorStatus);
            imgBook = v.findViewById(R.id.imgVendorBook);

            btnEdit = v.findViewById(R.id.btnVendorEdit);
            btnDelete = v.findViewById(R.id.btnVendorDelete);
            btnStock = v.findViewById(R.id.btnVendorStock);
        }
    }
}
