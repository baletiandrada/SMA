package com.example.booksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;

public class AutoCompleteActivity extends AppCompatActivity {

    private static final String[] NAMES = new String[]{
        "Andrada Baleti", "Andrada Popescu", "Cristina Adam", "Lorena Bakaity", "Oana Badea", "Anna Benzar"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete);

        AutoCompleteTextView editText = findViewById(R.id.autoCompleteTv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, NAMES);
        editText.setAdapter(adapter);

        TextView tv = findViewById(R.id.actv);
        Button btn = findViewById(R.id.actv_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(editText.getText().toString());
            }
        });

    }
}