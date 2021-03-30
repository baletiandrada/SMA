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

import com.example.booksapp.R;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.dataModels.ReviewModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mReviewsDatabase;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{

    private ArrayList<ReviewModel> reviews;
    private Context context;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    public ReviewAdapter(ArrayList<ReviewModel> reviews)
    {
        this.reviews = reviews;
    }

    @NonNull
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.row_quotes_data, viewGroup, false);
        return new ReviewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ReviewModel current_review = reviews.get(i);
        viewHolder.itemView.findViewById(R.id.layout_open_edit).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.iv_arrow_up).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_edit_quote).setVisibility(View.GONE);
        viewHolder.itemView.findViewById(R.id.layout_close_edit).setVisibility(View.GONE);

        viewHolder.username.setVisibility(View.VISIBLE);
        viewHolder.username.setText(currentUser.getEmail());

        viewHolder.reviewText.setText(current_review.getContent());

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
    }


    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView reviewText, username;
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
        }
    }
}
