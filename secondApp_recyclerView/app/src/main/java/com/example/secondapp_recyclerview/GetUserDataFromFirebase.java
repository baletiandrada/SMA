package com.example.secondapp_recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.secondapp_recyclerview.FirebaseHelper.mBooksReadDatabase;

public class GetUserDataFromFirebase extends AppCompatActivity {

    private TextView intro_tv;
    private EditText username_et, age_et, email_et, password_et;
    private Button updateUserData_button, addBookRead_button, addImage_button;
    private FirebaseAuth mAuth;
    StorageHelper userData = StorageHelper.getInstance();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_data_from_firebase);
        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        getDataFromStorageHelper();

        updateUserData_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });

        addBookRead_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBookReadActivity();
            }
        });

        addImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToImageActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public void updateUserData(){

        if(username_et.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }

        if(age_et.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter an age", Toast.LENGTH_LONG).show();
            return;
        }

        if(email_et.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a email", Toast.LENGTH_LONG).show();
            return;
        }

        if(password_et.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("username", username_et.getText().toString());
        map.put("age", age_et.getText().toString());
        map.put("email", email_et.getText().toString());
        map.put("password", password_et.getText().toString());

        String node_name = userData.idUserFirebase;
        FirebaseHelper.mUserDatabase.child(node_name).updateChildren(map);

        FirebaseUser user = mAuth.getCurrentUser();
        user.updateEmail(email_et.getText().toString());
        user.updatePassword(password_et.getText().toString());
    }

    public void getDataFromStorageHelper(){
        if(userData.username != "" && userData.age != "" && userData.email != "" && userData.password!="")
        {
            intro_tv.setText("Welcome " + userData.getUsername() + "!");
            username_et.setText(userData.getUsername());
            age_et.setText(userData.getAge());
            email_et.setText(userData.getEmail());
            password_et.setText(userData.getPassword());
        }

    }


    public void goToBookReadActivity(){
        Intent intent = new Intent(this, AddBookReadToFirebase.class);
        startActivity(intent);
    }

    public void goToImageActivity(){
        Intent intent = new Intent(this, ImageActivity.class);
        startActivity(intent);
    }

    public void initializeViews(){
        intro_tv = findViewById(R.id.tv_intro);
        username_et = findViewById(R.id.et_username);
        age_et = findViewById(R.id.et_age);
        email_et = findViewById(R.id.et_email);
        password_et = findViewById(R.id.et_password);
        updateUserData_button = findViewById(R.id.btn_updateUserData);
        addBookRead_button = findViewById(R.id.btn_addBookRead);
        addImage_button = findViewById(R.id.btn_addImage);

    }
}