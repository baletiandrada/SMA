package com.example.booksapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.AppConstants;
import com.example.booksapp.EditBookActivity;
import com.example.booksapp.R;
import com.example.booksapp.BookReviewsActivity;
import com.example.booksapp.dataModels.BookData;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.helpers.AppreciateBookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import static com.example.booksapp.AppConstants.ADD_REVIEW_ENABLED;
import static com.example.booksapp.AppConstants.BOOK_ID_LIST_PLAN;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;

public class BooksPlannedAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{

    private List<BookData> choicesList;
    private Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    public BooksPlannedAdapter(List<BookData> bookList){
        this.choicesList = bookList;
    }

    @NonNull
    @Override
    public BookReadDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_book_data, parent, false);
        BookReadDataViewHolder viewHolder = new BookReadDataViewHolder(contactView);
        return viewHolder;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookReadDataViewHolder holder, int position) {
        BookData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.youtube_player).setVisibility(View.GONE);
        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());

        holder.itemView.findViewById(R.id.tv_see_reviews).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.tv_see_reviews).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookReviewsActivity.class);
                AppreciateBookStorageHelper appreciateBookStorageHelper = AppreciateBookStorageHelper.getInstance();
                appreciateBookStorageHelper.setUser_id(currentUser.getUid());
                appreciateBookStorageHelper.setBook_id(BOOK_ID_LIST_PLAN.get(position));
                appreciateBookStorageHelper.setAuthor_name(bookModel.getAuthor_name());
                appreciateBookStorageHelper.setBook_title(bookModel.getTitle());
                intent.putExtra(ADD_REVIEW_ENABLED, "YES");
                context.startActivity(intent);
            }
        });

        if(!(bookModel.getUri()==null) && !bookModel.getUri().isEmpty() && !bookModel.getUri().equals("null"))
            Glide.with(context).load(bookModel.getUri()).placeholder(R.mipmap.ic_launcher).into(holder.iv_book_image);
        /*else
            holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);

        if(AppConstants.IMG_PLAN_CAME_FROM.get(position).equals("Admin"))
            holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);

        holder.itemView.findViewById(R.id.iv_delete_just_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBooksPlannedDatabase.child(currentUser.getUid()).child(bookModel.getId()).child("uri").removeValue();
                holder.itemView.findViewById(R.id.iv_delete_just_image).setBackgroundResource(R.drawable.default_book_image);
                holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);
            }
        });
        */

        //rating UI
        holder.itemView.findViewById(R.id.layout_users_rating).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_user_rating_discolored).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.tv_rating_user_score).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.GONE);
        if(!AppConstants.MEAN_RATING_PLAN_FRAG.get(position).equals("0")){
            holder.mean_rating.setText(AppConstants.MEAN_RATING_PLAN_FRAG.get(position) + "/5");
            holder.itemView.findViewById(R.id.iv_users_rating_discolored).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.VISIBLE);
        }


        holder.itemView.findViewById(R.id.iv_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this book?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(choicesList==null)){
                                    Toast.makeText(context,"Book deleted successfully", Toast.LENGTH_SHORT).show();
                                    if(currentUser!=null) {
                                        mBooksPlannedDatabase.child(currentUser.getUid()).child(bookModel.getId()).removeValue();
                                        choicesList.remove(bookModel);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, choicesList.size());
                                    }

                                }
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        holder.itemView.findViewById(R.id.iv_edit_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
                bookStorageHelper.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
                bookStorageHelper.setId_book(bookModel.getId());
                Intent intent = new Intent(context, EditBookActivity.class);
                String param_bookTable_value = "Planned books";
                intent.putExtra(AppConstants.PARAM_EDIT_BOOK_TABLE, param_bookTable_value);
                context.startActivity(intent);
            }
        });

        holder.itemView.findViewById(R.id.iv_check_book_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("This book will be added to the books read. Are you sure?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(currentUser!=null){
                                    String book_id = mBooksReadDatabase.child(currentUser.getUid()).push().getKey();
                                    if(BOOK_ID_LIST_PLAN.get(position)=="null")
                                        mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(bookModel);
                                    else{
                                        BookData book_aux = new BookData();
                                        book_aux.setId(BOOK_ID_LIST_PLAN.get(position));
                                        book_aux.setRead_month(bookModel.getRead_month());
                                        book_aux.setRead_year(bookModel.getRead_year());
                                        mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(book_aux);
                                    }
                                    mBooksPlannedDatabase.child(currentUser.getUid()).child(bookModel.getId()).removeValue();
                                    choicesList.remove(bookModel);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, choicesList.size());
                                }
                                Toast.makeText(context, "Book moved successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }

}
