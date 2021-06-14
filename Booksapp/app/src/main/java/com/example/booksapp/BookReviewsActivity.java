package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.adapters.ReviewAdapter;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.ReviewLikeModel;
import com.example.booksapp.dataModels.UserDetailsModel;
import com.example.booksapp.dataModels.WordModel;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.example.booksapp.AppConstants.ADD_REPLY_ENABLED;
import static com.example.booksapp.AppConstants.ADD_REVIEW_EN;
import static com.example.booksapp.AppConstants.ADD_REVIEW_ENABLED;
import static com.example.booksapp.AppConstants.ID_LIKE_FOR_CURRENT_USER;
import static com.example.booksapp.AppConstants.NUMBER_OF_DISLIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_LIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_REPLIES;
import static com.example.booksapp.AppConstants.REVIEW_APPRECIATION_FROM_CURRENT_USER;
import static com.example.booksapp.AppConstants.REVIEW_EXISTS_IN_LIKE_LIST;
import static com.example.booksapp.AppConstants.USER_GMAIL_LIST;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mLikesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRepliesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class BookReviewsActivity extends AppCompatActivity {

    private TextView title, author;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private MultiAutoCompleteTextView add_review_text;
    private Button add_review_btn, cancel_add_review_btn;
    private List<AppreciateBookModel> reviews_list = new ArrayList<AppreciateBookModel>();
    FloatingActionButton fab;

    AppreciateBookStorageHelper appreciateBookStorageHelper = AppreciateBookStorageHelper.getInstance();

    String ratingMeanScore="0";
    ArrayList<AppreciateBookModel> book_rating_list = new ArrayList<AppreciateBookModel>();

    TextView mean_rating_tv;
    ImageView star_colored, star_discolored;

    private List<ReviewLikeModel> like_list = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    ArrayList<String> texts_from_book_description = new ArrayList<>();

    String add_review_enabled;

    ArrayList<String> list_review_ids = new ArrayList<>();
    public void getReviewIds(){
        mReviewsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list_review_ids.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    list_review_ids.add(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reviews);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        add_review_enabled = intent.getStringExtra(AppConstants.ADD_REVIEW_ENABLED);
        ADD_REPLY_ENABLED.clear();
        AppConstants.ADD_REPLY_ENABLED.add(add_review_enabled);
        ADD_REVIEW_EN.clear();
        ADD_REVIEW_EN.add(add_review_enabled);
        AppConstants.enable_add_review = add_review_enabled;

        initializeViews();
        title.setText(appreciateBookStorageHelper.getBook_title());
        author.setText(appreciateBookStorageHelper.getAuthor_name());

        mean_rating_tv = findViewById(R.id.tv_mean_rating_see_reviews);
        star_colored = findViewById(R.id.iv_mean_rating__see_reviews_colored);
        star_discolored = findViewById(R.id.iv_mean_rating_see_reviews_discolored);

        getUsersEmail();
        getRatings();
        getLikeList();

        getReviewIds();


        mRepliesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getListNumberOfReplies();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    mRepliesDatabase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
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
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });


        final NestedScrollView nestedScrollview = findViewById(R.id.reviews_activ_nested_scroll_view);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.card_view_add_review).setVisibility(View.VISIBLE);

                nestedScrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollview.fullScroll(NestedScrollView.FOCUS_UP);
                    }
                });
            }
        });

        getTextFromAllBooks();
        final boolean[] lower_case = {false};
        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};
        add_review_text.setTokenizer(new SpaceTokenizer());
        ListView listView = findViewById(R.id.words_for_review_lv);
        add_review_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(add_review_text.getText().toString().length()>1){
                    String text_input = add_review_text.getText().toString();
                    List<String> text_splitted = Arrays.asList(text_input.split("\\s+"));
                    String last_word = text_splitted.get(text_splitted.size()-1);
                    if(last_word.length()>1 && !Arrays.asList(end_characters).contains(text_input.charAt(text_input.length()-1)))
                        listView.setVisibility(View.VISIBLE);
                    else
                        listView.setVisibility(View.GONE);
                    if(!texts_from_book_description.contains(last_word)){
                        if (text_splitted.size() > 1) {
                            String word_bef_last = text_splitted.get(text_splitted.size()-2);
                            if(word_bef_last.endsWith(".") || word_bef_last.endsWith("?") || word_bef_last.endsWith("!")){
                                texts_from_book_description = changeFirstLetter(texts_from_book_description);
                                lower_case[0] = false;
                            }
                            else if(!lower_case[0]){
                                texts_from_book_description = listToLowerCase(texts_from_book_description);
                                lower_case[0] = true;
                            }
                        }

                        ArrayList<String> words = FullTextSearch.searchForText(last_word, texts_from_book_description);
                        listView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, words));
                    }
                    else listView.setVisibility(View.GONE);
                }
                else{
                    listView.setVisibility(View.GONE);
                    texts_from_book_description = changeFirstLetter(texts_from_book_description);
                    lower_case[0] = false;
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String input = add_review_text.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    add_review_text.setText(input + parent.getItemAtPosition(position));
                }
                else
                    add_review_text.setText(parent.getItemAtPosition(position).toString());
                add_review_text.setSelection(add_review_text.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });


        add_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDatabase(v);
            }
        });

        cancel_add_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.card_view_add_review).setVisibility(View.GONE);
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

    public void getTextFromAllBooks(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String description = String.valueOf(dataSnapshot.child("description").getValue());
                    if(description!=null){
                        description = description.toLowerCase();
                        Character[] end_characters = {'.', ',', ';', '!', '?'};
                        ArrayList<Character> end_characters_arraylist = new ArrayList<>();
                        end_characters_arraylist.addAll(Arrays.asList(end_characters));
                        List<String> words = Arrays.asList(description.split("\\s+"));
                        for (String word : words) {
                            if(end_characters_arraylist.contains(word.charAt(word.length()-1))){
                                String substring = word.substring(0, word.length()-1);
                                word = substring;
                            }
                            if (!texts_from_book_description.contains(word)) {
                                texts_from_book_description.add(word);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public ArrayList<String> listToLowerCase(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            word = word.toLowerCase();
            words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public ArrayList<String> changeFirstLetter(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            if(word.charAt(0)>='a' && word.charAt(0)<='z'){
                String firstCh = String.valueOf(word.charAt(0));
                firstCh = firstCh.toUpperCase();
                String substring = word.substring(1);
                firstCh = firstCh.concat(substring);
                words_list_aux.add(firstCh);
            }
            else words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public StringBuilder replaceTextWithItemView(String text){
        StringBuilder input1 = new StringBuilder();
        input1.append(text);
        input1.reverse();
        input1.replace(0, input1.indexOf(" "), "");
        input1.reverse();
        return input1;
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
            NUMBER_OF_REPLIES.clear();

            List<AppreciateBookModel> aux_list = new ArrayList<>();
            aux_list.addAll(reviews_list);
            reviews_list.clear();
            Collections.sort(like_score_list, Collections.reverseOrder());
            for(ReviewLikeModel reviewLikeModel : like_score_list){
                for(AppreciateBookModel appreciateBookModel : aux_list){
                    if(reviewLikeModel.getReview_id().equals(appreciateBookModel.getId())){
                        if(!reviews_list.contains(appreciateBookModel)){
                            USER_GMAIL_LIST.add(getUserEmail(appreciateBookModel.getUser_id()));
                            NUMBER_OF_LIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "like"));
                            NUMBER_OF_DISLIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "dislike"));
                            REVIEW_APPRECIATION_FROM_CURRENT_USER.add(getAppreciationFromCurrentUser(appreciateBookModel.getId(), currentUser.getUid()));
                            NUMBER_OF_REPLIES.add(getNumberOfReplies(appreciateBookModel.getId()));
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
            }
            setRecyclerView();
        }

    }

    ArrayList<ReviewLikeModel> list_number_of_replies = new ArrayList<>();
    public void getListNumberOfReplies(){
        for(String review_id:list_review_ids){
            ReviewLikeModel replyModel = new ReviewLikeModel();
            replyModel.setReview_id(review_id);
            replyModel.setNo_of_replies("0");
            mRepliesDatabase.child(review_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    //Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();
                    replyModel.setNo_of_replies(String.valueOf(count));
                    list_number_of_replies.add(replyModel);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
    }

    public String getNumberOfReplies(String review_id){
        for(ReviewLikeModel reviewLikeModel:list_number_of_replies){
            if(reviewLikeModel.getReview_id().equals(review_id))
                return reviewLikeModel.getNo_of_replies();
        }
        return "0";
    }

    public void setRecyclerView(){
        reviewAdapter = new ReviewAdapter((ArrayList<AppreciateBookModel>) reviews_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reviewAdapter);
    }

    public void addToDatabase(View view) {

        if (add_review_text.getText() == null || add_review_text.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Review field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        AppreciateBookModel newReview = new AppreciateBookModel(appreciateBookStorageHelper.getUser_id(), appreciateBookStorageHelper.getBook_id(), add_review_text.getText().toString());
        String review_id = mReviewsDatabase.push().getKey();
        mReviewsDatabase.child(review_id).setValue(newReview);
        Toast.makeText(getApplicationContext(), "Review added successfully", Toast.LENGTH_SHORT).show();
        add_review_text.setText(null);
        findViewById(R.id.card_view_add_review).setVisibility(View.GONE);
        //Intent intent = new Intent(this, BookReviewsActivity.class);
        //intent.putExtra(ADD_REVIEW_ENABLED, "YES");
        //startActivity(intent);
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
                like_list.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(!Objects.equals(ds.getKey(), "replies")){
                        String review_id= String.valueOf(ds.child("review_id").getValue());
                        String appreciation= String.valueOf(ds.child("appreciation").getValue());
                        String user_id= String.valueOf(ds.child("user_id").getValue());
                        ReviewLikeModel newReviewLikeModel = new ReviewLikeModel(review_id, appreciation);
                        newReviewLikeModel.setId(ds.getKey());
                        newReviewLikeModel.setUser_id(user_id);
                        like_list.add(newReviewLikeModel);
                    }
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

        add_review_text = findViewById(R.id.et_add_review);
        add_review_btn = findViewById(R.id.btn_add_review);
        cancel_add_review_btn = findViewById(R.id.btn_cancel_add_review);
        findViewById(R.id.card_view_add_review).setVisibility(View.GONE);

        if(add_review_enabled.equals("NO"))
            fab.setVisibility(View.GONE);
    }

}