package com.manish.librarysystemfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    List<Review> list;

    public ReviewAdapter(List<Review> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Review r = list.get(pos);

        h.tvName.setText(r.userName);
        h.tvComment.setText(r.comment);
        h.ratingBar.setRating(r.rating);

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(r.timestamp);
        h.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvComment, tvDate;
        RatingBar ratingBar;

        public VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvReviewUser);
            tvComment = v.findViewById(R.id.tvReviewComment);
            tvDate = v.findViewById(R.id.tvReviewDate);
            ratingBar = v.findViewById(R.id.ratingBarReview);
        }
    }
}
