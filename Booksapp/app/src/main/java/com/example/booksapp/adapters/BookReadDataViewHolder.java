package com.example.booksapp.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.R;

public class BookReadDataViewHolder extends RecyclerView.ViewHolder{
    private TextView author_name;
    private TextView book_tile;
    private TextView read_year;
    private TextView read_month;
    private TextView genre;
    private ImageView iv_delete, iv_edit, iv_check_book;

    public BookReadDataViewHolder(@NonNull View itemView){
        super(itemView);
        initializeView();

    }

    private void initializeView(){
        author_name = itemView.findViewById(R.id.tv_row_author_name);
        book_tile = itemView.findViewById(R.id.tv_row_book_title);
        read_month = itemView.findViewById(R.id.tv_row_read_month);
        read_year = itemView.findViewById(R.id.tv_row_read_year);
        genre = itemView.findViewById(R.id.tv_genre);
        iv_delete = itemView.findViewById(R.id.iv_delete_image);
        iv_edit = itemView.findViewById(R.id.iv_edit_icon);
        iv_check_book = itemView.findViewById(R.id.iv_check_book_image);
    }

    public void setValues(String author_name, String book_title, String read_month, String read_year){
        this.author_name.setText(author_name);
        this.book_tile.setText(book_title);
        this.read_month.setText(read_month);
        this.read_year.setText(read_year);
    }

    public void setThreeValues(String author_name, String book_title, String genre){
        this.author_name.setText(author_name);
        this.book_tile.setText(book_title);
        this.genre.setText(genre);
    }

}