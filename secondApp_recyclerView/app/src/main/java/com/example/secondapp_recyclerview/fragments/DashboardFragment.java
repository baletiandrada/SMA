package com.example.secondapp_recyclerview.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.secondapp_recyclerview.AppExecutors;
import com.example.secondapp_recyclerview.FirebaseHelper;
import com.example.secondapp_recyclerview.ImageActivity;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.StorageHelper;
import com.example.secondapp_recyclerview.TestDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class DashboardFragment extends Fragment {

    private TextView intro_tv;
    private EditText username_et, age_et, email_et, password_et;
    private Button updateUserData_button;
    private FirebaseAuth mAuth;
    StorageHelper userData = StorageHelper.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        initializeViews(root);

        mAuth = FirebaseAuth.getInstance();
        getDataFromStorageHelper();

        updateUserData_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });

        return root;
    }

    public void updateUserData(){

        if(username_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }

        if(age_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter an age", Toast.LENGTH_LONG).show();
            return;
        }

        if(email_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a email", Toast.LENGTH_LONG).show();
            return;
        }

        if(password_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        if(!username_et.getText().toString().equals(userData.username) ||
                !age_et.getText().toString().equals(userData.age) ||
                !email_et.getText().toString().equals(userData.email) ||
                !password_et.getText().toString().equals(userData.password)){

            FirebaseUser user = mAuth.getCurrentUser();
            String new_email = email_et.getText().toString();

            assert user != null;
            if (!new_email.equals(user.getEmail())) {
                final Task<Void> voidTask = user.updateEmail(email_et.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                if(!voidTask.isSuccessful())
                    return;
            }

            if (!userData.password.equals(password_et.getText().toString())){
                if(password_et.getText().toString().length()>=6)
                    user.updatePassword(password_et.getText().toString());
                else{
                    Toast.makeText(getActivity(), "Your password must be at least 6 characters", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username_et.getText().toString());
            map.put("age", age_et.getText().toString());
            map.put("email", email_et.getText().toString());
            map.put("password", password_et.getText().toString());

            String node_name = userData.idUserFirebase;
            FirebaseHelper.mUserDatabase.child(node_name).updateChildren(map);

            Toast.makeText(getActivity(), "Your profile updated successfully", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getActivity(), "Please enter new values in the above fields", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    public void getDataFromStorageHelper(){
        if(!userData.username.equals("") && !userData.age.equals("") && !userData.email.equals("") && !userData.password.equals(""))
        {
            intro_tv.setText("Welcome " + userData.getUsername() + "!");
            username_et.setText(userData.getUsername());
            age_et.setText(userData.getAge());
            email_et.setText(userData.getEmail());
            password_et.setText(userData.getPassword());
        }

    }

    public void initializeViews(View root){
        intro_tv = root.findViewById(R.id.tv_intro);
        username_et = root.findViewById(R.id.et_username);
        age_et = root.findViewById(R.id.et_age);
        email_et = root.findViewById(R.id.et_email);
        password_et = root.findViewById(R.id.et_password);
        updateUserData_button = root.findViewById(R.id.btn_updateUserData);
    }

}