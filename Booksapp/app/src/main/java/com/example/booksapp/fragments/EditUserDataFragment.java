package com.example.booksapp.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.booksapp.AppConstants;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.helpers.FirebaseHelper;
import com.example.booksapp.helpers.StorageHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class EditUserDataFragment extends Fragment {
    private TextView intro_tv;
    private EditText username_et, age_et, email_et, old_password_et, new_password_et, confirm_new_password_et;
    private Button updateUserData_button, changePasswordTop_button, changePasswordBottom_button, cancelChangePassword_button;
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

        changePasswordTop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordTop_button.setVisibility(View.GONE);
                old_password_et.setVisibility(View.VISIBLE);
                new_password_et.setVisibility(View.VISIBLE);
                confirm_new_password_et.setVisibility(View.VISIBLE);
                changePasswordBottom_button.setVisibility(View.VISIBLE);
                cancelChangePassword_button.setVisibility(View.VISIBLE);
            }
        });

        changePasswordBottom_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        cancelChangePassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPasswordEtGone();
                changePasswordTop_button.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            intro_tv.setVisibility(View.GONE);
            username_et.setVisibility(View.GONE);
            age_et.setVisibility(View.GONE);
            email_et.setVisibility(View.GONE);
            updateUserData_button.setVisibility(View.GONE);
            changePasswordTop_button.setVisibility(View.GONE);
            setPasswordEtGone();
            goToLoginActivity();
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void changePassword(){
        if(old_password_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter your old password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(new_password_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(confirm_new_password_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please confirm the new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if(old_password_et.getText().toString().equals(userData.password)){
            FirebaseUser user = mAuth.getCurrentUser();
            if (new_password_et.getText().toString().equals(confirm_new_password_et.getText().toString())){
                if(new_password_et.getText().toString().length()>=6){
                    user.updatePassword(new_password_et.getText().toString());
                    FirebaseHelper.mUserDatabase.child(user.getUid()).child("password").setValue(new_password_et.getText().toString());
                    Toast.makeText(getActivity(), "You password updated successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity(), "Your password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else{
                Toast.makeText(getActivity(), "The passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            Toast.makeText(getActivity(), "You entered the wrong old password", Toast.LENGTH_SHORT).show();
            return;
        }
        setPasswordEtGone();
        changePasswordTop_button.setVisibility(View.VISIBLE);
    }

    public void updateUserData(){

        if(username_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(age_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter an age", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!username_et.getText().toString().equals(userData.username) ||
                !age_et.getText().toString().equals(userData.age) ||
                !email_et.getText().toString().equals(userData.email)) {

            FirebaseUser user = mAuth.getCurrentUser();
            String new_email = email_et.getText().toString();

            assert user != null;
            if (!new_email.equals(user.getEmail())) {
                final Task<Void> voidTask = user.updateEmail(email_et.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                if(!voidTask.isSuccessful())
                    return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username_et.getText().toString());
            map.put("age", age_et.getText().toString());
            map.put("email", email_et.getText().toString());
            map.put("password", userData.getPassword());

            String node_name = userData.idUserFirebase;
            FirebaseHelper.mUserDatabase.child(node_name).updateChildren(map);

            Toast.makeText(getActivity(), "Your profile updated successfully", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "Please enter new values in the above fields", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void getDataFromStorageHelper(){
        if(userData.username == null && userData.age == null && userData.email == null && userData.password == null)
        {
            mAuth.signOut();
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(AppConstants.EMAIL, "");
            editor.apply();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
        else{
            if(!userData.username.equals("") && !userData.age.equals("") && !userData.email.equals("") && !userData.password.equals(""))
            {
                intro_tv.setText("Edit your profile");
                username_et.setText(userData.getUsername());
                age_et.setText(userData.getAge());
            }
           email_et.setText(userData.getEmail());
        }
    }

    public void setPasswordEtGone(){
        old_password_et.setVisibility(View.GONE);
        new_password_et.setVisibility(View.GONE);
        confirm_new_password_et.setVisibility(View.GONE);
        changePasswordBottom_button.setVisibility(View.GONE);
        cancelChangePassword_button.setVisibility(View.GONE);
    }

    public void initializeViews(View root){
        intro_tv = root.findViewById(R.id.tv_intro);
        username_et = root.findViewById(R.id.et_username);
        age_et = root.findViewById(R.id.et_age);
        email_et = root.findViewById(R.id.et_email);
        updateUserData_button = root.findViewById(R.id.btn_updateUserData);

        changePasswordTop_button = root.findViewById(R.id.btn_changePasswordTop);
        old_password_et = root.findViewById(R.id.et_old_password);
        new_password_et = root.findViewById(R.id.et_new_password);
        confirm_new_password_et = root.findViewById(R.id.et_confirm_new_password);
        changePasswordBottom_button = root.findViewById(R.id.btn_changePasswordBottom);
        cancelChangePassword_button = root.findViewById(R.id.btn_cancelChangePasswordBottom);

        setPasswordEtGone();
    }
}
