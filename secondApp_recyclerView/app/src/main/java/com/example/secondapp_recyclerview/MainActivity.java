package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.secondapp_recyclerview.R;

public class MainActivity extends AppCompatActivity {

    private TextView firstTextView;
    private Button firstButton;

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
    }

    public void openActivity4()
    {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void initializeViews()
    {
        firstTextView = findViewById(R.id.tv_first_text);
        firstButton = findViewById(R.id.btn_first_button);
    }
}