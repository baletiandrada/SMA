package com.example.secondapp_recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder>{
    private List<NotificationModel> choicesList;
    private Context context;

    public NotificationAdapter(List<NotificationModel> notificationList){
        this.choicesList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_notification_model, parent, false);
        NotificationViewHolder viewHolder = new NotificationViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notificationModel = choicesList.get(position);
        holder.setValues(notificationModel.getName(), notificationModel.getHour());

    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }
}
