package com.example.booksapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.booksapp.AppConstants;
import com.example.booksapp.R;
import com.example.booksapp.dataModels.GenreModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.AppConstants.MY_PREFS_NAME;

public class GenreModelAdapter extends RecyclerView.Adapter<GenreModelAdapter.ViewHolder>{
    public List<GenreModel> genreList;
    Context context;

    public GenreModelAdapter(Context context, List<GenreModel> genreList)
    {
        this.context = context;
        this.genreList = genreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_genre_data, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        GenreModel genreModel = genreList.get(position);
        viewHolder.setGenreName(genreModel.getName());
        Glide.with(context).load(genreModel.getSrc()).placeholder(R.mipmap.ic_launcher).into(viewHolder.genreIcon);


    }

    private void removeItemAtPosition(int position){
        genreList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, genreList.size());
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView genreName;
        public ImageView genreIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            genreName = itemView.findViewById(R.id.tv_genre_name);
            genreIcon = itemView.findViewById(R.id.iv_genre_view);

        }
        public void setGenreName(String genreName)
        {
            this.genreName.setText(genreName);
        }
    }

}
