package com.manish.librarysystemfinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    Context context;
    List<Book> list;
    boolean allowBorrow;

    public BookAdapter(Context context, List<Book> list, boolean allowBorrow) {
        this.context = context;
        this.list = list;
        this.allowBorrow = allowBorrow;
    }

    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BookViewHolder h, int position) {
        Book b = list.get(position);

        h.txtTitle.setText(b.title);
        h.txtAuthor.setText(b.author);
        h.txtPrice.setText("$" + b.price);

        // SET BOOK IMAGE
        if (b.imageBase64 != null && !b.imageBase64.isEmpty()) {
            Bitmap bmp = decodeBase64(b.imageBase64);
            if (bmp != null) h.imgBook.setImageBitmap(bmp);
        }

        h.cardRoot.setOnClickListener(v -> {
            Intent i = new Intent(context, BookDetailsActivity.class);
            i.putExtra("bookId", b.id);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtAuthor, txtPrice;
        ImageView imgBook;
        CardView cardRoot;

        BookViewHolder(View v) {
            super(v);
            cardRoot = v.findViewById(R.id.cardRoot);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtAuthor = v.findViewById(R.id.txtAuthor);
            txtPrice = v.findViewById(R.id.txtPrice);
            imgBook = v.findViewById(R.id.imgBook); // ADD THIS IN item_book.xml
        }
    }
}
