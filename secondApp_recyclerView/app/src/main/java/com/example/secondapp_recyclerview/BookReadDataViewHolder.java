package com.example.secondapp_recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookReadDataViewHolder extends RecyclerView.ViewHolder{
    private TextView author_name;
    private TextView book_tile;
    private TextView read_year;
    private ImageView iv_delete, iv_edit;

    public BookReadDataViewHolder(@NonNull View itemView){
        super(itemView);
        initializeView();

    }

    private void initializeView(){
        author_name = itemView.findViewById(R.id.tv_row_author_name);
        book_tile = itemView.findViewById(R.id.tv_row_book_title);
        read_year = itemView.findViewById(R.id.tv_row_read_year);
        iv_delete = itemView.findViewById(R.id.iv_delete_image);
        iv_edit = itemView.findViewById(R.id.iv_edit_icon);
    }

    public void setValues(String author_name, String book_title, String read_year){
        this.author_name.setText(author_name);
        this.book_tile.setText(book_title);
        this.read_year.setText(read_year);
    }

}
