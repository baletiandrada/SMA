package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secondapp_recyclerview.R;

import static com.example.secondapp_recyclerview.AppConstants.KEY_password;
import static com.example.secondapp_recyclerview.AppConstants.KEY_username;
import static com.example.secondapp_recyclerview.AppConstants.MY_PREFS_NAME;

public class MainActivity extends AppCompatActivity {

    private TextView firstTextView;
    private Button firstButton, secondButton, thirdButton, roomButton, navBarButton;

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

    private void initializeViews()
    {
        firstTextView = findViewById(R.id.tv_first_text);
        firstButton = findViewById(R.id.btn_first_button);
        secondButton = findViewById(R.id.btn_preferedReferences_button);
        thirdButton = findViewById(R.id.btn_fileManagement_button);
        roomButton = findViewById(R.id.btn_room_button);
        navBarButton = findViewById(R.id.btn_navBar_button);
    }
}