package com.example.firstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ThirdActivity extends AppCompatActivity {
    private TextView firstTextView;
    private String extra_param_value;
    private Button goToListView_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        initializeViews();

        Intent intent = getIntent();
        extra_param_value = intent.getStringExtra(AppConstants.extra_param_key);
        extra_param_value = extra_param_value.concat(" is your name.");
        firstTextView.setText(extra_param_value);

        goToListView_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openListViewActivity();
            }
        });
    }

    public void openListViewActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void initializeViews(){
        firstTextView = findViewById(R.id.tv_first_text);
        goToListView_button = findViewById(R.id.btn_goToListView_button);
    }
}
