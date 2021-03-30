package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.adapters.QuoteAdapter;
import com.example.booksapp.adapters.ReviewAdapter;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.dataModels.ReviewModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.helpers.ReviewStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;

public class SeeReviewsActivity extends AppCompatActivity {

    private TextView title, author;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviews_list = new ArrayList<ReviewModel>();
    FloatingActionButton fab;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ReviewStorageHelper reviewStorageHelper = ReviewStorageHelper.getInstance();

    String ratingMeanScore="0";
    ArrayList<ReviewModel> book_rating_list = new ArrayList<ReviewModel>();

    TextView mean_rating_tv;
    ImageView star_colored, star_discolored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_reviews);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        title.setText(reviewStorageHelper.getBook_title());
        author.setText(reviewStorageHelper.getAuthor_name());

        mean_rating_tv = findViewById(R.id.tv_mean_rating_see_reviews);
        star_colored = findViewById(R.id.iv_mean_rating__see_reviews_colored);
        star_discolored = findViewById(R.id.iv_mean_rating_see_reviews_discolored);

        getRatings();

        mReviewsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDatabase(view);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void getData(DataSnapshot dataSnapshot) {
        reviews_list.removeAll(reviews_list);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String user_id = String.valueOf(ds.child("user_id").getValue());
            String book_id = String.valueOf(ds.child("book_id").getValue());
            String text = String.valueOf(ds.child("content").getValue());
            if(book_id.equals(reviewStorageHelper.getBook_id())){
                ReviewModel newReview = new ReviewModel(user_id, book_id, text);
                newReview.setId(String.valueOf(ds.getKey()));
                reviews_list.add(newReview);
            }
        }
        if(!reviews_list.isEmpty()){
            Collections.reverse(reviews_list);
            setRecyclerView();
        }
    }

    public void setRecyclerView(){
        reviewAdapter = new ReviewAdapter((ArrayList<ReviewModel>) reviews_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reviewAdapter);
    }

    public void addToDatabase(View view) {
        final TextInputEditText inputEditText;
        TextInputLayout textInputLayout;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        new AlertDialog.Builder(this, R.style.InputDialogTheme);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.view_input_dialog, (ViewGroup) findViewById(R.id.et_input_dialog) , false);
        inputEditText = viewInflated.findViewById(R.id.et_input_dialog);
        textInputLayout = viewInflated.findViewById(R.id.til_input_dialog);
        alert.setView(viewInflated);
        alert.setTitle("Add review");
        textInputLayout.setHint("Review");
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (inputEditText.getText() == null || inputEditText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Review field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                ReviewModel newReview = new ReviewModel(reviewStorageHelper.getUser_id(), reviewStorageHelper.getBook_id(), inputEditText.getText().toString());
                String review_id = mBooksReadDatabase.push().getKey();
                mReviewsDatabase.child(review_id).setValue(newReview);
                Toast.makeText(getApplicationContext(), "Review added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }


    public void getRatings(){
        mRatingsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                book_rating_list.removeAll(book_rating_list);
                for(DataSnapshot ds: snapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("book_id").getValue());
                    String user_id = String.valueOf(ds.child("user_id").getValue());
                    String rating = String.valueOf(ds.child("rating").getValue());
                    ReviewModel ratingData = new ReviewModel();
                    ratingData.setUser_id(user_id);
                    ratingData.setBook_id(book_id);
                    ratingData.setRating(rating);
                    book_rating_list.add(ratingData);
                }

                computeMeanRating(reviewStorageHelper.getBook_id());
                if(!ratingMeanScore.equals("0")){
                    mean_rating_tv.setText(ratingMeanScore + "/5");
                    star_discolored.setVisibility(View.GONE);
                    star_colored.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void computeMeanRating(String book_id){
        if(book_rating_list.size()!=0){
            int rating_sum=0;
            for(ReviewModel model : book_rating_list){
                if(model.getBook_id().equals(book_id))
                    rating_sum+=Integer.parseInt(model.getRating());
            }
            if(rating_sum!=0){
                double meanScore= (double)rating_sum/(book_rating_list.size());
                ratingMeanScore=String.format("%.1f", meanScore);
            }
        }
    }


    public void initializeViews(){
        title = findViewById(R.id.tv_title_in_reviews);
        author = findViewById(R.id.tv_author_in_reviews);
        recyclerView = findViewById(R.id.rv_reviews);
        fab = findViewById(R.id.fab_add_review);

        Intent intent = getIntent();
        String add_review_enabled = intent.getStringExtra(AppConstants.ADD_REVIEW_ENABLED);
        if(add_review_enabled.equals("NO"))
            fab.setVisibility(View.GONE);
    }

}