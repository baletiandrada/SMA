package com.example.booksapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.BookQuotesActivity;
import com.example.booksapp.R;
import com.example.booksapp.dataModels.BookData;
import com.example.booksapp.helpers.BookStorageHelper;

import java.util.List;

public class FavouriteBooksAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{
    private List<BookData> choicesList;
    private Context context;

    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    public FavouriteBooksAdapter(List<BookData> bookList){
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


    @Override
    public void onBindViewHolder(@NonNull BookReadDataViewHolder holder, int position) {
        BookData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.youtube_player).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_edit_delete_icons).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_done_text).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.tv_see_quotes).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_month_year).setVisibility(View.GONE);

        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
        if(!(bookModel.getUri()==null) && !bookModel.getUri().isEmpty() && !bookModel.getUri().equals("null"))
            Glide.with(context).load(bookModel.getUri()).placeholder(R.mipmap.ic_launcher).into(holder.iv_book_image);

        holder.itemView.findViewById(R.id.tv_see_quotes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookQuotesActivity.class);
                bookStorageHelper.setBook_title(bookModel.getTitle());
                bookStorageHelper.setAuthor_name(bookModel.getAuthor_name());
                bookStorageHelper.setId_book(bookModel.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }

}
