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

import com.example.booksapp.adapters.ReviewAdapter;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.ReviewLikeModel;
import com.example.booksapp.dataModels.UserDetailsModel;
import com.example.booksapp.helpers.AppreciateBookStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.booksapp.AppConstants.ID_LIKE_FOR_CURRENT_USER;
import static com.example.booksapp.AppConstants.NUMBER_OF_DISLIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_LIKES;
import static com.example.booksapp.AppConstants.REVIEW_APPRECIATION_FROM_CURRENT_USER;
import static com.example.booksapp.AppConstants.REVIEW_EXISTS_IN_LIKE_LIST;
import static com.example.booksapp.AppConstants.USER_GMAIL_LIST;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mLikesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class BookReviewsActivity extends AppCompatActivity {

    private TextView title, author;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<AppreciateBookModel> reviews_list = new ArrayList<AppreciateBookModel>();
    FloatingActionButton fab;

    AppreciateBookStorageHelper appreciateBookStorageHelper = AppreciateBookStorageHelper.getInstance();

    String ratingMeanScore="0";
    ArrayList<AppreciateBookModel> book_rating_list = new ArrayList<AppreciateBookModel>();

    TextView mean_rating_tv;
    ImageView star_colored, star_discolored;

    private List<ReviewLikeModel> like_list = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        title.setText(appreciateBookStorageHelper.getBook_title());
        author.setText(appreciateBookStorageHelper.getAuthor_name());

        mean_rating_tv = findViewById(R.id.tv_mean_rating_see_reviews);
        star_colored = findViewById(R.id.iv_mean_rating__see_reviews_colored);
        star_discolored = findViewById(R.id.iv_mean_rating_see_reviews_discolored);

        getUsersEmail();
        getRatings();
        getLikeList();

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

    private List<ReviewLikeModel> like_score_list = new ArrayList<>();
    private void getData(DataSnapshot dataSnapshot) {
        reviews_list.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String user_id = String.valueOf(ds.child("user_id").getValue());
            String book_id = String.valueOf(ds.child("book_id").getValue());
            String text = String.valueOf(ds.child("content").getValue());
            if(book_id.equals(appreciateBookStorageHelper.getBook_id())){
                AppreciateBookModel newReview = new AppreciateBookModel(user_id, book_id, text);
                newReview.setId(String.valueOf(ds.getKey()));

                ReviewLikeModel reviewLikeModel = new ReviewLikeModel();
                reviewLikeModel.setReview_id(String.valueOf(ds.getKey()));
                int number_of_likes = Integer.parseInt(getNumberOfAppreciations(ds.getKey(), "like"));
                int number_of_dislikes = Integer.parseInt(getNumberOfAppreciations(ds.getKey(), "dislike"));
                reviewLikeModel.setLike_score(String.valueOf(number_of_likes-number_of_dislikes));
                like_score_list.add(reviewLikeModel);

                reviews_list.add(newReview);
            }
        }
        if(!reviews_list.isEmpty()){
            USER_GMAIL_LIST.clear();
            NUMBER_OF_LIKES.clear();
            NUMBER_OF_DISLIKES.clear();
            REVIEW_APPRECIATION_FROM_CURRENT_USER.clear();
            REVIEW_EXISTS_IN_LIKE_LIST.clear();
            ID_LIKE_FOR_CURRENT_USER.clear();

            List<AppreciateBookModel> aux_list = new ArrayList<>();
            aux_list.addAll(reviews_list);
            reviews_list.clear();
            Collections.sort(like_score_list, Collections.reverseOrder());
            for(ReviewLikeModel reviewLikeModel : like_score_list){
                for(AppreciateBookModel appreciateBookModel : aux_list){
                    if(reviewLikeModel.getReview_id().equals(appreciateBookModel.getId())){

                        USER_GMAIL_LIST.add(getUserEmail(appreciateBookModel.getUser_id()));
                        NUMBER_OF_LIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "like"));
                        NUMBER_OF_DISLIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "dislike"));
                        REVIEW_APPRECIATION_FROM_CURRENT_USER.add(getAppreciationFromCurrentUser(appreciateBookModel.getId(), currentUser.getUid()));

                        if(likeListContainsReview(appreciateBookModel.getId(), currentUser.getUid(), like_list)){
                            ID_LIKE_FOR_CURRENT_USER.add(getLikeId(appreciateBookModel.getId(), currentUser.getUid()));
                            REVIEW_EXISTS_IN_LIKE_LIST.add("Yes");
                        }
                        else {
                            REVIEW_EXISTS_IN_LIKE_LIST.add("No");
                            ID_LIKE_FOR_CURRENT_USER.add("none");
                        }

                        reviews_list.add(appreciateBookModel);
                    }
                }
            }

            setRecyclerView();
        }
    }

    public void setRecyclerView(){
        reviewAdapter = new ReviewAdapter((ArrayList<AppreciateBookModel>) reviews_list);
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

                AppreciateBookModel newReview = new AppreciateBookModel(appreciateBookStorageHelper.getUser_id(), appreciateBookStorageHelper.getBook_id(), inputEditText.getText().toString());
                String review_id = mBooksReadDatabase.push().getKey();
                mReviewsDatabase.child(review_id).setValue(newReview);
                Toast.makeText(getApplicationContext(), "Review added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton){
            }
        });
        alert.show();
    }

    private List<UserDetailsModel> user_email_list = new ArrayList<>();
    public void getUsersEmail()
    {
        user_email_list.clear();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String uid = String.valueOf(ds.getKey());
                    String email = String.valueOf(ds.child("email").getValue());
                    UserDetailsModel user_model = new UserDetailsModel();
                    user_model.setUid(uid);
                    user_model.setEmail(email);
                    user_email_list.add(user_model);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getUserEmail(String user_uid){
        for(UserDetailsModel user_model : user_email_list){
            if(user_model.getUid().equals(user_uid))
                return user_model.getEmail();
        }
        return null;
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
                    AppreciateBookModel ratingData = new AppreciateBookModel();
                    ratingData.setUser_id(user_id);
                    ratingData.setBook_id(book_id);
                    ratingData.setRating(rating);
                    book_rating_list.add(ratingData);
                }

                computeMeanRating(appreciateBookStorageHelper.getBook_id());
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
            int number_of_ratings=0;
            for(AppreciateBookModel model : book_rating_list){
                if(model.getBook_id().equals(book_id)){
                    rating_sum+=Integer.parseInt(model.getRating());
                    number_of_ratings++;
                }

            }
            if(rating_sum!=0){
                double meanScore= (double)rating_sum/number_of_ratings;
                ratingMeanScore=String.format("%.1f", meanScore);
            }
        }
    }

    public boolean likeListContainsReview(String review_id, String current_user_id, List<ReviewLikeModel> likeList){
        for(ReviewLikeModel reviewLikeModel : likeList){
            if(reviewLikeModel.getReview_id().equals(review_id) && reviewLikeModel.getUser_id().equals(current_user_id))
                return true;
        }
        return false;
    }

    public String getNumberOfAppreciations(String review_id, String variable){
        int sum=0;
        for(ReviewLikeModel reviewLikeModel:like_list){
            if(reviewLikeModel.getReview_id().equals(review_id)) {
                String appreciation = reviewLikeModel.getAppreciation();
                if(appreciation.equals(variable))
                    sum+=1;
            }
        }
        return String.valueOf(sum);
    }

    public String getAppreciationFromCurrentUser(String review_id, String current_user_id){
        for(ReviewLikeModel reviewLikeModel : like_list){
            if(reviewLikeModel.getReview_id().equals(review_id) && reviewLikeModel.getUser_id().equals(current_user_id)){
                return reviewLikeModel.getAppreciation();
            }
        }
        return "None";
    }

    public String getLikeId(String review_id, String current_user_id){
        for(ReviewLikeModel reviewLikeModel : like_list){
            if(reviewLikeModel.getReview_id().equals(review_id) && reviewLikeModel.getUser_id().equals(current_user_id))
                return reviewLikeModel.getId();
        }
        return null;
    }

    public void getLikeList(){
        mLikesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    String review_id= String.valueOf(ds.child("review_id").getValue());
                    String appreciation= String.valueOf(ds.child("appreciation").getValue());
                    String user_id= String.valueOf(ds.child("user_id").getValue());
                    ReviewLikeModel newReviewLikeModel = new ReviewLikeModel(review_id, appreciation);
                    newReviewLikeModel.setId(ds.getKey());
                    newReviewLikeModel.setUser_id(user_id);
                    like_list.add(newReviewLikeModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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