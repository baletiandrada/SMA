package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


public class PreferedRefActivity extends AppCompatActivity {

    private TextView firstText;
    private EditText name;
    private EditText firstName;
    private Button add_button;
    private Button changeContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefered_ref);
        initializeViews();

        add_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setOnclickListener(); //add into shared preferences
                name.setText(null);
                firstName.setText(null);
                add_button.setVisibility(View.GONE);
                name.setVisibility(View.GONE);
                firstName.setVisibility(View.GONE);
                changeContent.setVisibility(View.VISIBLE);
            }
        });

        changeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContent.setVisibility(View.GONE);
                name.setVisibility(View.VISIBLE);
                firstName.setVisibility(View.VISIBLE);
                add_button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setOnclickListener(){

                if(name.getText().toString().isEmpty())
                {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter a name", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                else if(firstName.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter a first name", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                else {
                    writeSharedPref();
                    readSharedPref();
                }

    }

    public void writeSharedPref(){
        SharedPreferences.Editor editor = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConstants.KEY1_NAME, name.getText().toString());
        editor.putString(AppConstants.KEY2_NAME, firstName.getText().toString());
        editor.apply();
    }

    public void readSharedPref(){
        SharedPreferences prefs = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(AppConstants.KEY1_NAME, "No name defined");
        String firstName = prefs.getString(AppConstants.KEY2_NAME, "No name defined");
        name = name.concat(" " + firstName);
        firstText.setText(name);
    }

    public void initializeViews()
    {
        firstText=findViewById(R.id.tv_show_text);
        name = findViewById(R.id.et_name);
        firstName = findViewById(R.id.et_firstName);
        add_button = findViewById(R.id.add_button);
        changeContent = findViewById(R.id.btn_changeContent);
        changeContent.setVisibility(View.GONE);
    }
}