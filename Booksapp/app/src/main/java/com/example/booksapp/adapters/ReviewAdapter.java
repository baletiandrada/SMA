package com.example.booksapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AppConstants;
import com.example.booksapp.FullTextSearch;
import com.example.booksapp.R;
import com.example.booksapp.RepliesActivity;
import com.example.booksapp.SpaceTokenizer;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.BookData;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.dataModels.ReviewLikeModel;
import com.example.booksapp.dataModels.UserDetailsModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.AppConstants.INDEX_FOR_GETTING_NO_OF_LIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_DISLIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_LIKES;
import static com.example.booksapp.AppConstants.NUMBER_OF_REPLIES;
import static com.example.booksapp.AppConstants.USER_GMAIL_LIST;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mLikesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRepliesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{

    private ArrayList<AppreciateBookModel> reviews;
    private Context context;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();

    private ArrayList<String> texts_from_book_description = new ArrayList<>();
    public ReviewAdapter(ArrayList<AppreciateBookModel> reviews)
    {
        this.reviews = reviews;
    }

    @NonNull
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.row_change_content, viewGroup, false);
        return new ReviewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        AppreciateBookModel current_review = reviews.get(i);
        viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.GONE);
        if(current_review.getUser_id().equals(currentUser.getUid())){
            viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
            viewHolder.itemView.findViewById(R.id.layout_like_icons).setVisibility(View.GONE);
        }
        viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);

        viewHolder.username.setVisibility(View.VISIBLE);

        if(USER_GMAIL_LIST.get(i)!=null)
            viewHolder.username.setText(USER_GMAIL_LIST.get(i));
        else
            viewHolder.username.setText("Anonymous");

        viewHolder.reviewText.setText(current_review.getContent());

        String s1, s2;
        int no_of_likes = Integer.valueOf(NUMBER_OF_LIKES.get(i));
        if(no_of_likes==1)
            s1="like";
        else s1="likes";
        int no_of_dislikes = Integer.valueOf(NUMBER_OF_DISLIKES.get(i));
        if(no_of_dislikes==1)
            s2="like";
        else s2="likes";

        viewHolder.number_of_likes.setText(AppConstants.NUMBER_OF_LIKES.get(i) + " " + s1);
        viewHolder.number_of_dislikes.setText(AppConstants.NUMBER_OF_DISLIKES.get(i) + " " + s2);

        if(AppConstants.REVIEW_APPRECIATION_FROM_CURRENT_USER.get(i).equals("like")){
            viewHolder.itemView.findViewById(R.id.iv_like_discolored_icon).setVisibility(View.GONE);
            viewHolder.itemView.findViewById(R.id.iv_like_colored_icon).setVisibility(View.VISIBLE);
        }

        if(AppConstants.REVIEW_APPRECIATION_FROM_CURRENT_USER.get(i).equals("dislike")){
            viewHolder.itemView.findViewById(R.id.iv_dislike_discolored_icon).setVisibility(View.GONE);
            viewHolder.itemView.findViewById(R.id.iv_dislike_colored_icon).setVisibility(View.VISIBLE);
        }

        viewHolder.itemView.findViewById(R.id.iv_arrow_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.itemView.findViewById(R.id.iv_arrow_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
                viewHolder.reviewText.setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.VISIBLE);
                viewHolder.editReview.setText(viewHolder.reviewText.getText().toString());
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.VISIBLE);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this review?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(reviews==null)){
                                    Toast.makeText(context, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                                    if (currentUser != null) {
                                        mReviewsDatabase.child(current_review.getId()).removeValue();
                                        reviews.remove(current_review);
                                        notifyItemRemoved(i);
                                        notifyItemRangeChanged(i, reviews.size());
                                    }

                                }
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        ListView listView = viewHolder.itemView.findViewById(R.id.words_for_edit_review_lv);
        viewHolder.cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.reviewText.setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
            }
        });

        getTextFromAllBooks();

        final boolean[] lower_case = {false};
        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};

        viewHolder.editReview.setTokenizer(new SpaceTokenizer());
        viewHolder.editReview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(viewHolder.editReview.getText().toString().length()>1){
                    String text_input = viewHolder.editReview.getText().toString();
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
                        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, words));
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
                String input = viewHolder.editReview.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    viewHolder.editReview.setText(input + parent.getItemAtPosition(position));
                }
                else
                    viewHolder.editReview.setText(parent.getItemAtPosition(position).toString());
                viewHolder.editReview.setSelection(viewHolder.editReview.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });


        viewHolder.save_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.editReview.getText().toString().isEmpty()){
                    Toast.makeText(context, "Review field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("content", viewHolder.editReview.getText().toString());
                mReviewsDatabase.child(current_review.getId()).updateChildren(map);
                Toast.makeText(context, "Review updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        viewHolder.itemView.findViewById(R.id.iv_like_discolored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_like_discolored_icon).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_like_colored_icon).setVisibility(View.VISIBLE);
                if(AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.get(i).equals("No")){
                    ReviewLikeModel newReviewLikeModel = new ReviewLikeModel(current_review.getId(), "like");
                    newReviewLikeModel.setUser_id(currentUser.getUid());
                    String like_id = mLikesDatabase.push().getKey();
                    mLikesDatabase.child(like_id).setValue(newReviewLikeModel);

                    if(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i).equals("none"))
                        AppConstants.ID_LIKE_FOR_CURRENT_USER.set(i, like_id);
                    AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.set(i, "Yes");

                }
                else{
                    viewHolder.itemView.findViewById(R.id.iv_dislike_colored_icon).setVisibility(View.GONE);
                    viewHolder.itemView.findViewById(R.id.iv_dislike_discolored_icon).setVisibility(View.VISIBLE);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("review_id", current_review.getId());
                    map.put("user_id", currentUser.getUid());
                    map.put("appreciation", "like");
                    mLikesDatabase.child(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i)).updateChildren(map);
                    int number_of_dislikes = Integer.valueOf(AppConstants.NUMBER_OF_DISLIKES.get(i));
                    number_of_dislikes--;
                    viewHolder.number_of_dislikes.setText(number_of_dislikes + " dislikes");
                    AppConstants.NUMBER_OF_DISLIKES.set(i, String.valueOf(number_of_dislikes));
                }

                int number_of_likes = Integer.valueOf(AppConstants.NUMBER_OF_LIKES.get(i));
                number_of_likes++;
                viewHolder.number_of_likes.setText(number_of_likes + " likes");
                AppConstants.NUMBER_OF_LIKES.set(i, String.valueOf(number_of_likes));
            }
        });

        viewHolder.itemView.findViewById(R.id.iv_dislike_discolored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_dislike_discolored_icon).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_dislike_colored_icon).setVisibility(View.VISIBLE);
                if(AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.get(i).equals("No")){
                    ReviewLikeModel newReviewLikeModel = new ReviewLikeModel(current_review.getId(), "dislike");
                    newReviewLikeModel.setUser_id(currentUser.getUid());
                    String like_id = mLikesDatabase.push().getKey();
                    mLikesDatabase.child(like_id).setValue(newReviewLikeModel);

                    if(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i).equals("none"))
                        AppConstants.ID_LIKE_FOR_CURRENT_USER.set(i, like_id);

                    AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.set(i, "Yes");

                }
                else{
                    viewHolder.itemView.findViewById(R.id.iv_like_colored_icon).setVisibility(View.GONE);
                    viewHolder.itemView.findViewById(R.id.iv_like_discolored_icon).setVisibility(View.VISIBLE);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("review_id", current_review.getId());
                    map.put("user_id", currentUser.getUid());
                    map.put("appreciation", "dislike");
                    mLikesDatabase.child(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i)).updateChildren(map);
                    int number_of_likes = Integer.valueOf(AppConstants.NUMBER_OF_LIKES.get(i));
                    number_of_likes--;
                    viewHolder.number_of_likes.setText(number_of_likes + " likes");
                    AppConstants.NUMBER_OF_LIKES.set(i, String.valueOf(number_of_likes));
                }

                int number_of_dislikes = Integer.valueOf(AppConstants.NUMBER_OF_DISLIKES.get(i));
                number_of_dislikes++;
                viewHolder.number_of_dislikes.setText(number_of_dislikes + " dislikes");
                AppConstants.NUMBER_OF_DISLIKES.set(i, String.valueOf(number_of_dislikes));

            }
        });

        viewHolder.itemView.findViewById(R.id.iv_like_colored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_like_colored_icon).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_like_discolored_icon).setVisibility(View.VISIBLE);
                mLikesDatabase.child(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i)).removeValue();
                int number_of_likes = Integer.valueOf(AppConstants.NUMBER_OF_LIKES.get(i));
                number_of_likes--;
                viewHolder.number_of_likes.setText(number_of_likes + " likes");
                AppConstants.NUMBER_OF_LIKES.set(i, String.valueOf(number_of_likes));
                AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.set(i, "No");
                AppConstants.ID_LIKE_FOR_CURRENT_USER.set(i, "none");
            }
        });

        viewHolder.itemView.findViewById(R.id.iv_dislike_colored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.itemView.findViewById(R.id.iv_dislike_colored_icon).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.iv_dislike_discolored_icon).setVisibility(View.VISIBLE);
                mLikesDatabase.child(AppConstants.ID_LIKE_FOR_CURRENT_USER.get(i)).removeValue();
                int number_of_dislikes = Integer.valueOf(AppConstants.NUMBER_OF_DISLIKES.get(i));
                number_of_dislikes--;
                viewHolder.number_of_dislikes.setText(number_of_dislikes + " dislikes");
                AppConstants.NUMBER_OF_DISLIKES.set(i, String.valueOf(number_of_dislikes));
                AppConstants.REVIEW_EXISTS_IN_LIKE_LIST.set(i, "No");
                AppConstants.ID_LIKE_FOR_CURRENT_USER.set(i, "none");
            }
        });


        viewHolder.see_replies.setText("Click here to open replies (" + NUMBER_OF_REPLIES.get(i) + ")");
        getUsersEmail();
        viewHolder.see_replies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RepliesActivity.class);
                AppConstants.REVIEW_ID = current_review.getId();
                AppConstants.EMAIL_FROM_REVIEW = getUserEmail(current_review.getUser_id());
                AppConstants.CONTENT_REVIEW = current_review.getContent();
                AppConstants.INDEX_FOR_GETTING_NO_OF_LIKES = i;
                context.startActivity(intent);
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


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView reviewText, username, number_of_likes, number_of_dislikes, see_replies;
        public MultiAutoCompleteTextView editReview;
        public ImageView delete, edit, save_edit, cancel_edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewText = itemView.findViewById(R.id.tv_text_quote);
            editReview = itemView.findViewById(R.id.et_edit_quote);
            edit = itemView.findViewById(R.id.iv_edit_quote);
            delete = itemView.findViewById(R.id.iv_delete_quote);
            save_edit = itemView.findViewById(R.id.iv_save_edit_quote);
            cancel_edit = itemView.findViewById(R.id.iv_clear_edit_quote);
            username = itemView.findViewById(R.id.tv_username_in_special_activity);
            number_of_likes = itemView.findViewById(R.id.tv_number_of_likes);
            number_of_dislikes = itemView.findViewById(R.id.tv_number_of_dislikes);
            see_replies = itemView.findViewById(R.id.tv_see_replies);
        }
    }
}
