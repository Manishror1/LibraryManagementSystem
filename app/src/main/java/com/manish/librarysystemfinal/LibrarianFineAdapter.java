package com.manish.librarysystemfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class LibrarianFineAdapter extends RecyclerView.Adapter<LibrarianFineAdapter.ViewHolder> {

    Context context;
    ArrayList<StudentFine> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public LibrarianFineAdapter(Context context, ArrayList<StudentFine> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_librarian_fine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        StudentFine f = list.get(pos);

        h.tvBook.setText("Book: " + f.bookTitle);
        h.tvUser.setText("User ID: " + f.userId);
        h.tvAmount.setText("Fine: ₹" + f.amount);

        String due = sdf.format(f.dueDate);
        String ret = sdf.format(f.returnedAt);

        h.tvDates.setText("Due: " + due + " | Returned: " + ret);

        // MARK PAID
        h.btnPaid.setOnClickListener(v -> {
            db.collection("fines").document(f.id)
                    .update("amount", 0)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(context, "Fine marked as paid", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // DELETE / WAIVE
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Fine")
                    .setMessage("Are you sure you want to delete/waive this fine?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.collection("fines").document(f.id)
                                .delete()
                                .addOnSuccessListener(a -> {
                                    list.remove(pos);
                                    notifyItemRemoved(pos);
                                    Toast.makeText(context, "Fine deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBook, tvUser, tvAmount, tvDates;
        Button btnPaid, btnDelete;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvBook = v.findViewById(R.id.tvLibFineBookTitle);
            tvUser = v.findViewById(R.id.tvLibFineUser);
            tvAmount = v.findViewById(R.id.tvLibFineAmount);
            tvDates = v.findViewById(R.id.tvLibFineDates);
            btnPaid = v.findViewById(R.id.btnLibFinePaid);
            btnDelete = v.findViewById(R.id.btnLibFineDelete);
        }
    }
}
