package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.adapters.ReplyAdapter;
import com.example.booksapp.adapters.ReviewAdapter;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.ReviewLikeModel;
import com.example.booksapp.dataModels.UserDetailsModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
import static com.example.booksapp.AppConstants.ID_LIKE_FOR_CURRENT_USER;
import static com.example.booksapp.AppConstants.INDEX_FOR_GETTING_NO_OF_LIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_DISLIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_LIKES;
import static com.example.booksapp.AppConstants.REPLY_APPRECIATION_FROM_CURRENT_USER;
import static com.example.booksapp.AppConstants.REPLY_EXISTS_IN_LIKE_LIST;
import static com.example.booksapp.AppConstants.USER_GMAIL_LIST;
import static com.example.booksapp.AppConstants.enable_add_review;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mLikesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRepliesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class RepliesActivity extends AppCompatActivity {

    private TextView email_review, content_review, no_of_likes_tv, no_of_dislikes_tv;;
    private ExtendedFloatingActionButton add_reply_fab;
    private RecyclerView recyclerView;
    private ReplyAdapter replyAdapter;
    private MultiAutoCompleteTextView et_add_reply;
    private ListView listView;
    private ImageView save_reply, cancel_add_reply;

    private ArrayList<AppreciateBookModel> replyList = new ArrayList<>();

    ArrayList<String> texts_from_book_description = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    String add_review_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replies);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();

        no_of_likes_tv = findViewById(R.id.tv_number_of_likes_review_in_replies);
        no_of_dislikes_tv = findViewById(R.id.tv_number_of_dislikes_review_in_replies);
        String s1, s2;
        int no_of_likes = Integer.valueOf(NUMBER_OF_LIKES.get(INDEX_FOR_GETTING_NO_OF_LIKES));
        if(no_of_likes==1)
            s1="like";
        else s1="likes";
        int no_of_dislikes = Integer.valueOf(NUMBER_OF_DISLIKES.get(INDEX_FOR_GETTING_NO_OF_LIKES));
        if(no_of_dislikes==1)
            s2="dislike";
        else s2="dislikes";
        no_of_likes_tv.setText(NUMBER_OF_LIKES.get(INDEX_FOR_GETTING_NO_OF_LIKES) + " " + s1);
        no_of_dislikes_tv.setText(NUMBER_OF_DISLIKES.get(INDEX_FOR_GETTING_NO_OF_LIKES) + " " + s2);

        getUsersEmail();
        getLikeList();

        getRepliesFromDB(AppConstants.REVIEW_ID);
        email_review.setText(AppConstants.EMAIL_FROM_REVIEW);
        content_review.setText(AppConstants.CONTENT_REVIEW);

        getTextFromAllBooks();
        final boolean[] lower_case = {false};
        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};
        et_add_reply.setTokenizer(new SpaceTokenizer());
        et_add_reply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(et_add_reply.getText().toString().length()>1){
                    String text_input = et_add_reply.getText().toString();
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
                        Collections.sort(words, Collections.reverseOrder());
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
                String input = et_add_reply.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    et_add_reply.setText(input + parent.getItemAtPosition(position));
                }
                else
                    et_add_reply.setText(parent.getItemAtPosition(position).toString());
                et_add_reply.setSelection(et_add_reply.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });


        final NestedScrollView nestedScrollview = findViewById(R.id.reply_activ_nested_scroll_view);

        add_reply_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.card_view_add_reply).setVisibility(View.VISIBLE);
                add_reply_fab.setVisibility(View.GONE);

                nestedScrollview.post(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollview.fullScroll(NestedScrollView.FOCUS_DOWN);
                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        et_add_reply.requestFocus();
                    }
                }, 10);

            }
        });

        save_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_add_reply.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Reply field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String reply_id = mRepliesDatabase.child(AppConstants.REVIEW_ID).push().getKey();
                AppreciateBookModel newReply = new AppreciateBookModel();
                newReply.setUser_id(currentUser.getUid());
                newReply.setContent(et_add_reply.getText().toString());
                assert reply_id != null;
                mRepliesDatabase.child(AppConstants.REVIEW_ID).child(reply_id).setValue(newReply);
                Toast.makeText(getApplicationContext(), "Reply added successfully", Toast.LENGTH_SHORT).show();
                findViewById(R.id.card_view_add_reply).setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                et_add_reply.setText(null);
                add_reply_fab.setVisibility(View.VISIBLE);
            }
        });

        cancel_add_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.card_view_add_reply).setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                add_reply_fab.setVisibility(View.VISIBLE);
                et_add_reply.setText(null);
            }
        });

    }

    public void getRepliesFromDB(String review_id){
        mRepliesDatabase.child(review_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                replyList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    String reply_id = String.valueOf(dataSnapshot.getKey());
                    String text = String.valueOf(dataSnapshot.child("content").getValue());
                    String user_id = String.valueOf(dataSnapshot.child("user_id").getValue());
                    AppreciateBookModel replyModel = new AppreciateBookModel();
                    replyModel.setId(reply_id);
                    replyModel.setContent(text);
                    replyModel.setUser_id(user_id);
                    replyModel.setReview_id(review_id);
                    replyList.add(replyModel);
                }
                if(replyList.size()!=0){
                    USER_GMAIL_LIST.clear();
                    NUMBER_OF_LIKES.clear();
                    NUMBER_OF_DISLIKES.clear();
                    REPLY_APPRECIATION_FROM_CURRENT_USER.clear();
                    REPLY_EXISTS_IN_LIKE_LIST.clear();
                    ID_LIKE_FOR_CURRENT_USER.clear();

                    replyAdapter = new ReplyAdapter(replyList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setAdapter(replyAdapter);

                    for(AppreciateBookModel appreciateBookModel:replyList){
                        USER_GMAIL_LIST.add(getUserEmail(appreciateBookModel.getUser_id()));

                        NUMBER_OF_LIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "like"));
                        NUMBER_OF_DISLIKES.add(getNumberOfAppreciations(appreciateBookModel.getId(), "dislike"));
                        REPLY_APPRECIATION_FROM_CURRENT_USER.add(getAppreciationFromCurrentUser(appreciateBookModel.getId(), currentUser.getUid()));

                        if(likeListContainsReview(appreciateBookModel.getId(), currentUser.getUid(), like_list)){
                            ID_LIKE_FOR_CURRENT_USER.add(getLikeId(appreciateBookModel.getId(), currentUser.getUid()));
                            REPLY_EXISTS_IN_LIKE_LIST.add("Yes");
                        }
                        else {
                            REPLY_EXISTS_IN_LIKE_LIST.add("No");
                            ID_LIKE_FOR_CURRENT_USER.add("none");
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public boolean likeListContainsReview(String reply_id, String current_user_id, List<ReviewLikeModel> likeList){
        for(ReviewLikeModel replyLikeModel : likeList){
            if(replyLikeModel.getReply_id().equals(reply_id) && replyLikeModel.getUser_id().equals(current_user_id))
                return true;
        }
        return false;
    }

    public String getLikeId(String reply_id, String current_user_id){
        for(ReviewLikeModel replyLikeModel : like_list){
            if(replyLikeModel.getReply_id().equals(reply_id) && replyLikeModel.getUser_id().equals(current_user_id))
                return replyLikeModel.getId();
        }
        return null;
    }

    public String getNumberOfAppreciations(String reply_id, String variable){
        int sum=0;
        for(ReviewLikeModel replyLikeModel:like_list){
            if(replyLikeModel.getReply_id().equals(reply_id)) {
                String appreciation = replyLikeModel.getAppreciation();
                if(appreciation.equals(variable))
                    sum+=1;
            }
        }
        return String.valueOf(sum);
    }

    public String getAppreciationFromCurrentUser(String reply_id, String current_user_id){
        for(ReviewLikeModel replyLikeModel : like_list){
            if(replyLikeModel.getReply_id().equals(reply_id) && replyLikeModel.getUser_id().equals(current_user_id)){
                return replyLikeModel.getAppreciation();
            }
        }
        return "None";
    }

    private List<ReviewLikeModel> like_list = new ArrayList<>();
    public void getLikeList(){
        mLikesDatabase.child("replies").child(AppConstants.REVIEW_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                        String reply_id= String.valueOf(ds.child("reply_id").getValue());
                        String appreciation= String.valueOf(ds.child("appreciation").getValue());
                        String user_id= String.valueOf(ds.child("user_id").getValue());
                        ReviewLikeModel newReplyLikeModel = new ReviewLikeModel();
                        newReplyLikeModel.setReply_id(reply_id);
                        newReplyLikeModel.setAppreciation(appreciation);
                        newReplyLikeModel.setId(ds.getKey());
                        newReplyLikeModel.setUser_id(user_id);
                        like_list.add(newReplyLikeModel);
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    public void initializeViews(){
        add_reply_fab = findViewById(R.id.fab_extended_add_reply);
        recyclerView = findViewById(R.id.rv_replies_activ);
        et_add_reply = findViewById(R.id.et_add_reply);
        save_reply = findViewById(R.id.iv_save_reply);
        cancel_add_reply = findViewById(R.id.iv_cancel_add_reply);
        email_review = findViewById(R.id.tv_email_from_review);
        content_review = findViewById(R.id.tv_review_content);
        listView = findViewById(R.id.words_for_add_reply_lv);

        if(ADD_REPLY_ENABLED.get(0).equals("NO"))
            add_reply_fab.setVisibility(View.GONE);
    }
}