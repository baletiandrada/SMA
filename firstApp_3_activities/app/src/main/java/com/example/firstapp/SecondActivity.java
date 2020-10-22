package com.example.firstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {

    private Button okButton, cancelButton, openDialogButton;
    private EditText firstEditText;

    public SecondActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initializeViews();
        setOnClickListeners1();
        setOnClickListeners2();
        setOnClickListeners3();
    }

    private void setOnClickListeners1()
    {
        openDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogButton.setVisibility(View.GONE);
                firstEditText.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setOnClickListeners2()
    {
        View mainButton;
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openThirdActivity();
            }
        });
    }

    private void openThirdActivity()
    {
        String extra_param_value=firstEditText.getText().toString();

        if(extra_param_value.isEmpty())
        {
            Toast toast = Toast.makeText(this, "Please enter your name to go to a new activity", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -100);
            toast.show();
            return;
        }
        else {
            Intent intent = new Intent(this, ThirdActivity.class);
            intent.putExtra(AppConstants.extra_param_key, extra_param_value);
            startActivity(intent);
        }
    }

    private void setOnClickListeners3()
    {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstEditText.setText(null);
                firstEditText.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                showCancelingText();
                openDialogButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showCancelingText()
    {
        Toast toast = Toast.makeText(this, "You closed the dialog", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -100);
        toast.show();
        return;
    }

    private void initializeViews()
    {
        firstEditText = findViewById(R.id.et_first_input);
        okButton = findViewById(R.id.btn_ok_button);
        cancelButton = findViewById(R.id.btn_cancel_button);
        openDialogButton = findViewById(R.id.btn_openDialog_button);

        firstEditText.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }
}