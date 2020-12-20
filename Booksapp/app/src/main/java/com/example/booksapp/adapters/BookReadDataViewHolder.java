package com.example.booksapp.adapters;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.R;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;

public class BookReadDataViewHolder extends RecyclerView.ViewHolder{
    private TextView author_name, book_tile, read_year, read_month, genre;
    private ImageView iv_delete, iv_edit, iv_check_book;
    private Button play_trailer_button;

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
        play_trailer_button = itemView.findViewById(R.id.btn_play_trailer);
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