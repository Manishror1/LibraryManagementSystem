package com.manish.librarysystemfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BorrowedBooksListAdapter extends RecyclerView.Adapter<BorrowedBooksListAdapter.ViewHolder> {

    Context context;
    List<BorrowedBook> list;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public BorrowedBooksListAdapter(Context context, List<BorrowedBook> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_borrowed_book, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        BorrowedBook b = list.get(pos);

        h.tvTitle.setText(b.title);
        h.tvAuthor.setText("by " + b.author);
        h.tvDate.setText("Borrowed on: " +
                android.text.format.DateFormat.format("dd/MM/yyyy", b.borrowedAt));

        // Load image safely
        try {
            byte[] bytes = android.util.Base64.decode(b.imageBase64, android.util.Base64.DEFAULT);
            h.imgBook.setImageBitmap(
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length)
            );
        } catch (Exception ignored) {}

        // RETURN BUTTON CLICK
        h.btnReturn.setOnClickListener(v -> {

            // 🔥 Prevent double-tapping
            h.btnReturn.setEnabled(false);
            h.btnReturn.postDelayed(() -> h.btnReturn.setEnabled(true), 1200);

            checkIfAlreadyRequested(b, h);
        });
    }

    // ---------------------------
    // 🔥 CHECK IF ALREADY REQUESTED
    // ---------------------------
    private void checkIfAlreadyRequested(BorrowedBook b, ViewHolder h) {

        String userId = auth.getCurrentUser().getUid();

        db.collection("return_requests")
                .whereEqualTo("userId", userId)
                .whereEqualTo("bookId", b.bookId)
                .whereEqualTo("status", "pending_return")
                .get()
                .addOnSuccessListener(snaps -> {

                    if (!snaps.isEmpty()) {
                        Toast.makeText(context,
                                "Return already requested!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // If not requested → create request
                    createReturnRequest(b);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------------------
    // 🔥 CREATE RETURN REQUEST
    // ---------------------------
    private void createReturnRequest(BorrowedBook b) {

        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();
        String userName = auth.getCurrentUser().getDisplayName();

        if (userName == null) userName = "Student";

        String requestId = db.collection("return_requests").document().getId();

        BorrowRecord record = new BorrowRecord(
                requestId,
                userId,
                userName,
                userEmail,
                b.bookId,
                b.title,
                b.imageBase64,
                b.borrowedAt,
                b.dueDate,
                "pending_return"
        );

        db.collection("return_requests")
                .document(requestId)
                .set(record)
                .addOnSuccessListener(a -> {
                    Toast.makeText(context,
                            "Return request sent to librarian!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvAuthor, tvDate;
        ImageView imgBook;
        Button btnReturn;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvTitle = v.findViewById(R.id.txtBorrowedTitle);
            tvAuthor = v.findViewById(R.id.txtBorrowedAuthor);
            tvDate = v.findViewById(R.id.txtBorrowedDate);
            imgBook = v.findViewById(R.id.imgBorrowedBook);
            btnReturn = v.findViewById(R.id.btnReturnBook);
        }
    }
}
