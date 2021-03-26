package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.dataModels.BookReadData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class AutoCompleteActivity extends AppCompatActivity {

    private static final String[] NAMES = new String[]{
        "Andrada Baleti", "Andrada Popescu", "Cristina Adam", "Lorena Bakaity", "Oana Badea", "Anna Benzar"
    };

    ArrayList<String> authorNames = new ArrayList<String>();
    ArrayList<String> bookTitles = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete);

        getAuthorAndTitles();

        MultiAutoCompleteTextView editText = findViewById(R.id.autoCompleteTv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, authorNames);
        editText.setTokenizer(new SpaceTokenizer());

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

    private void getAuthorAndTitles(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                authorNames.removeAll(authorNames);
                bookTitles.removeAll(authorNames);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    List<String> names = Arrays.asList(author_name.split("\\s+"));
                    authorNames.addAll(names);
                    bookTitles.add(book_title);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}