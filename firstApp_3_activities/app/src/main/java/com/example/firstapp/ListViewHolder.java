package com.example.firstapp;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder{
    private TextView nameTv;
    private TextView firstnameTv;
    private Button delete_button;

    public ListViewHolder(@NonNull View itemView){
        super(itemView);
        initializeView();
    }

    private void initializeView(){
        nameTv = itemView.findViewById(R.id.tv_row_name);
        firstnameTv = itemView.findViewById(R.id.tv_row_firstname);
        delete_button = itemView.findViewById(R.id.btn_delete_button);
    }

    public void setValues(String name, String firstname){
        nameTv.setText(name);
        firstnameTv.setText(firstname);
    }
}
