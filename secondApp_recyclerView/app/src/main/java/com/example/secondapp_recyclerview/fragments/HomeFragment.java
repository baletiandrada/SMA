package com.example.secondapp_recyclerview.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.secondapp_recyclerview.AppConstants;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.StorageHelper;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView1 = root.findViewById(R.id.text_home);

        final TextView tv_email = root.findViewById(R.id.tv_email);
        final TextView tv_age = root.findViewById(R.id.tv_age);
        final TextView tv_username = root.findViewById(R.id.tv_username);
        final TextView tv_password = root.findViewById(R.id.tv_passwd);
        StorageHelper received_data = StorageHelper.getInstance().getValues();

        if(received_data.email!=null && received_data.age!=null && received_data.username!=null && received_data.password!=null){
            tv_email.setText(received_data.email);
            tv_age.setText(received_data.age);
            tv_username.setText(received_data.username);
            tv_password.setText(received_data.password);
        }

        return root;
    }

}