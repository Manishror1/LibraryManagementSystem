package com.manish.librarysystemfinal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RejectedBooksAdapter extends RecyclerView.Adapter<RejectedBooksAdapter.VH> {

    private final List<RejectedBook> list;
    private final Context context;

    public RejectedBooksAdapter(Context ctx, List<RejectedBook> list) {
        this.context = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_rejected_book, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        RejectedBook b = list.get(position);

        holder.title.setText(b.title);
        holder.author.setText(b.author);
        holder.reason.setText("Reason: " + b.reason);

        // Load Image
        try {
            if (b.imageBase64 != null && !b.imageBase64.isEmpty()) {
                byte[] bytes = Base64.decode(b.imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.img.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) {}

        // -----------------------------------------
        // RESUBMIT BOOK
        // -----------------------------------------
        holder.btnResubmit.setOnClickListener(v -> {

            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(b.bookId)
                    .update("status", "pending")
                    .addOnSuccessListener(a -> {

                        // Remove from Room DB
                        new Thread(() -> {
                            AppDatabase.getInstance(context)
                                    .rejectedBookDao()
                                    .deleteById(b.id);
                        }).start();

                        // Update UI
                        list.remove(currentPos);
                        notifyItemRemoved(currentPos);
                        notifyItemRangeChanged(currentPos, list.size());

                        Toast.makeText(context,
                                "Book resubmitted for approval.",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });

        // -----------------------------------------
        // DELETE REJECTED BOOK ENTRY
        // -----------------------------------------
        holder.btnDelete.setOnClickListener(v -> {

            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            new Thread(() -> {
                AppDatabase.getInstance(context)
                        .rejectedBookDao()
                        .deleteById(b.id);
            }).start();

            list.remove(currentPos);
            notifyItemRemoved(currentPos);
            notifyItemRangeChanged(currentPos, list.size());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, author, reason;
        Button btnResubmit, btnDelete;

        public VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgRejectedBook);
            title = v.findViewById(R.id.tvRejectedTitle);
            author = v.findViewById(R.id.tvRejectedAuthor);
            reason = v.findViewById(R.id.tvRejectedReason);
            btnResubmit = v.findViewById(R.id.btnResubmit);
            btnDelete = v.findViewById(R.id.btnDeleteRejected);
        }
    }
}
