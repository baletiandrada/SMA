package com.example.secondapp_recyclerview;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationViewHolder extends RecyclerView.ViewHolder{
    private TextView name;
    private TextView hour;


    public NotificationViewHolder(@NonNull View itemView){
        super(itemView);
        initializeView();
    }

    private void initializeView(){
        name = itemView.findViewById(R.id.tv_row_name);
        hour = itemView.findViewById(R.id.tv_row_hour);
    }

    public void setValues(String name, String hour){
        this.name.setText(name);
        this.hour.setText(hour);
    }
}
