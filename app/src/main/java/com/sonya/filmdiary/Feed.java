package com.sonya.filmdiary;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.sonya.filmdiary.databinding.ActivityFeedBinding;
import com.sonya.filmdiary.databinding.ActivityProfileBinding;

import java.util.ArrayList;
import java.util.Locale;

public class Feed extends AppCompatActivity {

    private ActivityFeedBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ReviewAdapter reviewAdapter;
    ArrayList<Review> reviews;
    String myImage, genre = "Комедия";
    String g = "Комедия";
    String r = "5";
    ArrayAdapter ratingadapter, genreadapter;
    String[] genres = {"Жанр", "Исторический", "Боевик", "Комедия", "Хоррор", "Научная фантастика", "Мистика", "Анимационный", "Романтика", "Триллер", "Война", "Приключение", "Комиксы"};
    String[] ratings = {"Рейтинг", "1", "2", "3", "4", "5"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getImage();
        binding.toolbar.setTitle("Лента");
        binding.toolbar.inflateMenu(R.menu.profile);
        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_item_profile) {

                    startActivity(new Intent(Feed.this, Profile.class).putExtra("myImage", myImage));

                }
                return false;
            }
        });

        genreadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genres);
        genreadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ratingadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ratings);
        ratingadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        getReviews();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getReviews();
                binding.swipe.setRefreshing(false);

            }
        });

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showCreateReviewDialog();

            }
        });

        binding.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getLayoutInflater().inflate(R.layout.filter_bottom, null);

                BottomSheetDialog dialog = new BottomSheetDialog(Feed.this);
                dialog.setContentView(view);
                dialog.show();

                final Spinner genre_spinner = view.findViewById(R.id.genre);
                genre_spinner.setAdapter(genreadapter);

                final Spinner rating_spinner = view.findViewById(R.id.rating);
                rating_spinner.setAdapter(ratingadapter);

                genre_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        g = genres[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                        g = "Жанр";

                    }
                });

                rating_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        r = ratings[position];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                        r = "Рейтинг";

                    }
                });

                final Button search = view.findViewById(R.id.button_search);
                search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!r.equals("Rating") && !g.equals("Жанр")) {
                            genre_and_rating_filter(r.toLowerCase(), g.toLowerCase());
                            dialog.cancel();
                        } else if (!r.equals("Rating")) {
                            filter(r, "Rating");
                            dialog.cancel();
                        } else if (!g.equals("Жанр")) {
                            filter(g.toLowerCase(), "Жанр");
                            dialog.cancel();
                        }

                    }
                });

            }
        });

        binding.searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filter(binding.searchView.getText().toString().toLowerCase(), "title");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void genre_and_rating_filter(String rating, String genre) {
        genre.toLowerCase();
        rating.toLowerCase();
        ArrayList<Review> temp = new ArrayList();
        for (Review review : reviews) {
            if (review.getGenre().toLowerCase().contains(genre) && String.valueOf(review.getMark()).toLowerCase().equals(rating)) {
                temp.add(review);
            }
        }

        reviewAdapter.updateList(temp);

    }

    private void filter(String text, String looking_for) {
        text.toLowerCase();
        ArrayList<Review> temp = new ArrayList();
        for (Review review : reviews) {
            if (looking_for.equals("title")) {
                if (review.getTitle().toLowerCase().contains(text)) {
                    temp.add(review);
                }
            } else if (looking_for.equals("Rating")) {
                if (String.valueOf(review.getMark()).toLowerCase().equals(text)) {
                    temp.add(review);
                }

            } else if (looking_for.equals("Жанр")) {
                if (review.getGenre().toLowerCase().equals(text)) {
                    temp.add(review);
                }
            }
        }

        reviewAdapter.updateList(temp);
    }

    private void getImage() {

        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {

                    if (ds.getValue(User.class).getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {

                        myImage = ds.getValue(User.class).getProfilePicture();
                    }

                }
            }


            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void getReviews() {
        reviews = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviews.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    Review review = ds.getValue(Review.class);

                    reviews.add(review);
                    Log.d("REV", review.getTitle());

                }

                binding.recycler.setHasFixedSize(false);
                binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                reviewAdapter = new ReviewAdapter(getApplication(), reviews);
                reviewAdapter.setOnItemClickListener(new ReviewAdapter.ReviewClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {

                        if (reviews.get(position).getAuthor().equals(FirebaseAuth.getInstance().getUid()))

                            showEditReviewDialog(reviews.get(position));

                    }
                });
                binding.recycler.setAdapter(reviewAdapter);
                reviewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showEditReviewDialog(Review review) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.edit_review_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Изменить");

        final EditText title = dialogView.findViewById(R.id.title);
        final EditText text = dialogView.findViewById(R.id.text);
        final Button close = dialogView.findViewById(R.id.close);
        final Button change = dialogView.findViewById(R.id.change);
        final Button delete = dialogView.findViewById(R.id.delete);
        final Spinner genre_spinner = dialogView.findViewById(R.id.genre_spinner);
        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        title.setText(review.getTitle());
        text.setText(review.getText());
        ratingBar.setRating(review.getMark());
        genre_spinner.setAdapter(genreadapter);


        final AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();

        genre_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                genre = genres[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b.dismiss();

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds : snapshot.getChildren()) {


                            if (ds.getValue(Review.class).getTitle().equals(review.getTitle()) && ds.getValue(Review.class).getAuthor().equals(review.getAuthor())) {

                                FirebaseDatabase.getInstance().getReference("review").child(ds.getKey()).removeValue();

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                b.dismiss();


            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String s = title.getText().toString();
                String z = text.getText().toString();

                if (!TextUtils.isEmpty(title.getText().toString().trim()) && !TextUtils.isEmpty(text.getText().toString().trim())) {

                    FirebaseDatabase.getInstance().getReference().child("review").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot ds : snapshot.getChildren()) {


                                if (ds.getValue(Review.class).getTitle().equals(review.getTitle()) && ds.getValue(Review.class).getAuthor().equals(review.getAuthor())) {

                                    FirebaseDatabase.getInstance().getReference("review").child(ds.getKey()).setValue(new Review(FirebaseAuth.getInstance().getUid(), s, z, (int) ratingBar.getRating(), genre));

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {

                    title.setError("Enter items");

                }

                b.dismiss();

            }
        });
    }

    private void showCreateReviewDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.create_review_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Создать");

        final EditText title = dialogView.findViewById(R.id.title);
        final EditText text = dialogView.findViewById(R.id.text);
        final Button close = dialogView.findViewById(R.id.close);
        final Button add = dialogView.findViewById(R.id.add);
        final Spinner genre_spinner = dialogView.findViewById(R.id.genre_spinner);
        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);

        genre_spinner.setAdapter(genreadapter);


        final AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();


        genre_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                genre = genres[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b.dismiss();

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                b.dismiss();

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String s = title.getText().toString();
                String z = text.getText().toString();

                if (!TextUtils.isEmpty(title.getText().toString().trim()) && !TextUtils.isEmpty(text.getText().toString().trim())) {

                    FirebaseDatabase.getInstance().getReference("review").push().setValue(new Review(FirebaseAuth.getInstance().getUid(), s, z, (int) ratingBar.getRating(), genre));

                } else {

                    title.setError("Enter items");

                }

                b.dismiss();

            }
        });
    }

}