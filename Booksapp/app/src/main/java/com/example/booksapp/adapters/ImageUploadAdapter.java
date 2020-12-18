package com.example.booksapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.dataModels.ImageUploadInfo;
import com.example.booksapp.R;

import java.util.ArrayList;

public class ImageUploadAdapter extends RecyclerView.Adapter<ImageUploadAdapter.ViewHolder> {

    private ArrayList<ImageUploadInfo> imagesInfo;
    private Context context;

    public ImageUploadAdapter(ArrayList<ImageUploadInfo> imagesInfo)
    {
        this.imagesInfo = imagesInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.row_image_data, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        ImageUploadInfo imageInfoCurrent = imagesInfo.get(i);
        viewHolder.imageNameTv.setText(imageInfoCurrent.getImage_name());

        Glide.with(context).load(imageInfoCurrent.getPath()).placeholder(R.mipmap.ic_launcher).into(viewHolder.photoUploadedIv);
        /*Picasso.get()
                .load(imageInfoCurrent.getPath())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(viewHolder.photoUploadedIv);*/
    }

    @Override
    public int getItemCount() {
        return imagesInfo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView imageNameTv;
        public ImageView photoUploadedIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageNameTv = itemView.findViewById(R.id.iv_row_image_name);
            photoUploadedIv = itemView.findViewById(R.id.iv_row_image_view);
        }
    }

}
