package com.manish.librarysystemfinal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StudentFineAdapter extends RecyclerView.Adapter<StudentFineAdapter.ViewHolder> {

    Context context;
    ArrayList<StudentFine> list;

    public StudentFineAdapter(Context context, ArrayList<StudentFine> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_student_fine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        StudentFine f = list.get(pos);

        h.tvBook.setText(f.bookTitle);
        h.tvAmount.setText("Fine: $" + f.amount);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        h.tvDate.setText("Due Date: " + sdf.format(f.dueDate));

        // ⭐ OPEN CHECKOUT PAGE ⭐
        h.btnPayFine.setOnClickListener(v -> {

            Intent i = new Intent(context, CheckoutActivity.class);
            i.putExtra("type", "fine");
            i.putExtra("amount", f.amount);
            i.putExtra("bookId", f.bookId);
            i.putExtra("bookTitle", f.bookTitle);
            i.putExtra("fineId", f.id);

            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvBook, tvAmount, tvDate;
        Button btnPayFine;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvBook = v.findViewById(R.id.tvFineBookTitle);
            tvAmount = v.findViewById(R.id.tvFineAmount);
            tvDate = v.findViewById(R.id.tvFineDate);
            btnPayFine = v.findViewById(R.id.btnPayFine);
        }
    }
}
