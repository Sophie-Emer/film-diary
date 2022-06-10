package com.sonya.filmdiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{
    private ArrayList<Review> reviews = new ArrayList<>();
    private Context context;
    private static ReviewClickListener reviewClickListener;

    public ReviewAdapter(Context context, ArrayList<Review> reviews) {
        this.reviews = reviews;
        this.context = context;
    }


    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_row, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {

        holder.title.setText(reviews.get(position).getTitle());
        holder.text.setText(reviews.get(position).getText());
        holder.genre.setText("Жанр: " + reviews.get(position).getGenre());
        holder.ratingBar.setRating(Float.parseFloat(String.valueOf(reviews.get(position).getMark())));

    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, text, genre;
        private RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
            genre = itemView.findViewById(R.id.genre);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            reviewClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ReviewClickListener clickListener) {
        reviewClickListener = clickListener;
    }

    public interface ReviewClickListener {
        void onItemClick(int position, View v);
    }

    public void updateList(List<Review> list) {
        reviews = (ArrayList<Review>) list;
        notifyDataSetChanged();
    }

}
