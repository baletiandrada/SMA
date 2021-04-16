package com.example.booksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AppConstants;
import com.example.booksapp.R;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.dataModels.ReviewLikeModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.example.booksapp.AppConstants.USER_GMAIL_LIST;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mLikesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{

    private ArrayList<AppreciateBookModel> reviews;
    private Context context;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

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

        viewHolder.number_of_likes.setText(AppConstants.NUMBER_OF_LIKES.get(i) + " likes");
        viewHolder.number_of_dislikes.setText(AppConstants.NUMBER_OF_DISLIKES.get(i) + " dislikes");

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

        viewHolder.cancel_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.reviewText.setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.iv_arrow_down).setVisibility(View.VISIBLE);
                viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
                viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);
            }
        });

        viewHolder.save_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.editReview.getText().toString().isEmpty()){
                    Toast.makeText(context, "Quote field is empty", Toast.LENGTH_SHORT).show();
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
            }
        });

    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView reviewText, username, number_of_likes, number_of_dislikes;
        public EditText editReview;
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
        }
    }
}
