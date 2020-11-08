package com.example.secondapp_recyclerview.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.secondapp_recyclerview.AppExecutors;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.StorageHelper;
import com.example.secondapp_recyclerview.TestDatabase;

public class DashboardFragment extends Fragment {

    private EditText new_email_et;
    private EditText new_age_et;
    private EditText new_username_et;
    private EditText new_password_et;
    private Button update_button;
    private Button open_update;

    private TestDatabase testDatabase;
    StorageHelper received_data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initializeViews(root);
        testDatabase = TestDatabase.getInstance(root.getContext());

        received_data = StorageHelper.getInstance();
        if(received_data.email!=null && received_data.age!=null && received_data.username!=null && received_data.password!=null)
        {
            new_email_et.setText(received_data.email);
            new_age_et.setText(received_data.age);
            new_username_et.setText(received_data.username);
            new_password_et.setText(received_data.password);
        }

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new_email_et.getText().toString().isEmpty())
                {
                    Toast toast = Toast.makeText( root.getContext() , "Please enter your email", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(new_age_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( root.getContext() , "Please enter your age", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(new_username_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( root.getContext(), "Please enter a username", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(new_password_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( root.getContext() , "Please enter a password", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        String new_email = new_email_et.getText().toString();
                        String new_age = new_age_et.getText().toString();
                        String new_username = new_username_et.getText().toString();
                        String new_password = new_password_et.getText().toString();
                        update_database(received_data.id, new_email, new_age, new_username, new_password, root);
                    }
                });
            }
        });

        open_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_update.setVisibility(View.GONE);
                new_email_et.setVisibility(View.VISIBLE);
                new_age_et.setVisibility(View.VISIBLE);
                new_username_et.setVisibility(View.VISIBLE);
                new_password_et.setVisibility(View.VISIBLE);
                update_button.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    public void initializeViews(View root) {
        new_email_et = root.findViewById(R.id.et_email);
        new_age_et = root.findViewById(R.id.et_age);
        new_username_et = root.findViewById(R.id.et_username);
        new_password_et = root.findViewById(R.id.et_passwd);
        update_button = root.findViewById(R.id.btn_update);
        open_update = root.findViewById(R.id.btn_open_update);
        open_update.setVisibility(View.GONE);
    }

    public void update_database(int id, String email, String age, String username, String password, View root){
        class UpdateValue extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                testDatabase.testDAO().update(id, email, age, username, password);
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                new_email_et.setVisibility(View.GONE);
                new_age_et.setVisibility(View.GONE);
                new_username_et.setVisibility(View.GONE);
                new_password_et.setVisibility(View.GONE);
                update_button.setVisibility(View.GONE);
                open_update.setVisibility(View.VISIBLE);

                Toast toast = Toast.makeText(root.getContext(), "Your data is updated", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                toast.show();

                received_data.setEmail(email);
                received_data.setAge(age);
                received_data.setUsername(username);
                received_data.setPassword(password);
            }
        }

        UpdateValue updateTask = new UpdateValue();
        updateTask.execute();
    }

}