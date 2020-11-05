package com.example.secondapp_recyclerview;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private List<TestEntity> choicesList;
    private Context context;

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_list_model, parent, false);
        ListViewHolder viewHolder = new ListViewHolder(contactView);
        return viewHolder;
    }

    public RoomAdapter(List<TestEntity> personalaDataList){
        this.choicesList = personalaDataList;
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, final int position) {
        final TestEntity personalDataModel = choicesList.get(position);
        holder.setValues(personalDataModel.getName(), personalDataModel.getFirstname());
        holder.itemView.findViewById(R.id.iv_delete_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                choicesList.remove(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }
}
