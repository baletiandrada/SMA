package com.example.secondapp_recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondapp_recyclerview.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.secondapp_recyclerview.AppConstants.KEY_password;
import static com.example.secondapp_recyclerview.AppConstants.KEY_username;
import static com.example.secondapp_recyclerview.AppConstants.MY_PREFS_NAME;
import static com.example.secondapp_recyclerview.FirebaseHelper.mUserDatabase;

public class MainActivity extends AppCompatActivity {

    private TextView firstTextView;
    private Button firstButton, secondButton, thirdButton, roomButton, navBarButton, firebaseButton;
    StorageHelper userData = StorageHelper.getInstance();

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity4();
            }
        });

        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityPR();
            }
        });

        thirdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityReadFromFile();
            }
        });

        roomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRoomActivity();
            }
        });

        navBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        firebaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFirebaseActivity();
            }
        });
    }

    public void openActivity4()
    {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    public void openActivityPR()
    {
        Intent intent = new Intent(this, PreferedRefActivity.class);
        startActivity(intent);
    }

    public void openActivityReadFromFile()
    {
        Intent intent = new Intent(this, FilesActivity.class);
        startActivity(intent);
    }

    public void openRoomActivity()
    {
        Intent intent = new Intent(this, RoomActivity.class);
        startActivity(intent);
    }

    public void openLoginActivity()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openFirebaseActivity(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;
        if(currentUser == null){
            intent = new Intent(this, FirebaseActivity.class);
            startActivity(intent);
        }
        else {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user != null){
                        getData(dataSnapshot, user);
                        goToUserActivity();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void goToUserActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    private void getData(DataSnapshot dataSnapshot, FirebaseUser user) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            if(ds.getKey().equals(user.getUid()))
            {
                userData.setIdUserFirebase(String.valueOf(ds.getKey()));
                userData.setUsername(String.valueOf(ds.child("username").getValue()));
                userData.setAge(String.valueOf(ds.child("age").getValue()));
                userData.setEmail(String.valueOf(ds.child("email").getValue()));
                userData.setPassword(String.valueOf(ds.child("password").getValue()));
                break;
            }
        }
    }

    private void initializeViews()
    {
        firstTextView = findViewById(R.id.tv_first_text);
        firstButton = findViewById(R.id.btn_first_button);
        secondButton = findViewById(R.id.btn_preferedReferences_button);
        thirdButton = findViewById(R.id.btn_fileManagement_button);
        roomButton = findViewById(R.id.btn_room_button);
        navBarButton = findViewById(R.id.btn_navBar_button);
        firebaseButton = findViewById(R.id.btn_firebase_button);
    }
}