package com.manish.librarysystemfinal;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LibrarianPendingReturnAdapter
        extends RecyclerView.Adapter<LibrarianPendingReturnAdapter.ViewHolder> {

    Context context;
    List<BorrowRecord> list;
    LibrarianApproveReturnsActivity activity;

    public LibrarianPendingReturnAdapter(Context context, List<BorrowRecord> list) {
        this.context = context;
        this.list = list;
        this.activity = (LibrarianApproveReturnsActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pending_return, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        BorrowRecord r = list.get(pos);

        h.txtTitle.setText(r.bookTitle);
        h.txtStudent.setText("Student: " + r.userName);
        h.txtEmail.setText("Email: " + r.userEmail);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

        h.txtBorrowDate.setText("Borrow: " + sdf.format(new Date(r.borrowedAt)));
        h.txtDueDate.setText("Due: " + sdf.format(new Date(r.dueDate)));

        try {
            if (r.bookImageBase64 != null) {
                byte[] bytes = Base64.decode(r.bookImageBase64, Base64.DEFAULT);
                h.imgBook.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
        } catch (Exception ignored) {}

        h.btnApprove.setOnClickListener(v -> activity.approveReturn(r));
        h.btnFine.setOnClickListener(v -> activity.addFine(r));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtStudent, txtEmail, txtBorrowDate, txtDueDate;
        ImageView imgBook;
        Button btnApprove, btnFine;

        public ViewHolder(@NonNull View v) {
            super(v);

            txtTitle = v.findViewById(R.id.txtPendingTitle);
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
