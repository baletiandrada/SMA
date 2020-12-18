package com.example.booksapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.R;
import com.example.booksapp.dataModels.ImageUploadInfo;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class GenresAdapter extends ArrayAdapter<String>{

    public GenresAdapter(Context context, ArrayList<String> genres)
    {
        super(context, 0, genres);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.row_genres, parent, false
            );
        }

        TextView genreTV = convertView.findViewById(R.id.tv_row_genre);
        String currentItem = getItem(position);
        if(currentItem!=null)
            genreTV.setText(currentItem);
        return convertView;
    }

}
