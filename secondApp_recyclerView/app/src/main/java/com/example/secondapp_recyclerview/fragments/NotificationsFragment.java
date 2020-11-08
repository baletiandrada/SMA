package com.example.secondapp_recyclerview.fragments;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondapp_recyclerview.NotificationAdapter;
import com.example.secondapp_recyclerview.NotificationModel;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.RoomAdapter;
import com.example.secondapp_recyclerview.TestEntity;
import com.example.secondapp_recyclerview.ui.notifications.NotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {


    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        final TextView textView = root.findViewById(R.id.text_notif);
        //final RecyclerView exampleNotificationList = root.findViewById(R.id.rv_list);
        final List<NotificationModel> notifications = new ArrayList<NotificationModel>();

        initializeNotificationList(notifications);

        final NotificationAdapter listExampleAdapterNotifications = new NotificationAdapter(notifications);
        recyclerView = root.findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listExampleAdapterNotifications);

        textView.setText("You received " + String.valueOf(listExampleAdapterNotifications.getItemCount()) + " notifications.");
        return root;
    }

    public void initializeNotificationList(List<NotificationModel> notifications){
        notifications.add(new NotificationModel("notification 1", "09:00"));
        notifications.add(new NotificationModel("notification 2", "09:30"));
        notifications.add(new NotificationModel("notification 3", "10:00"));
        notifications.add(new NotificationModel("notification 4", "10:30"));
    }


}