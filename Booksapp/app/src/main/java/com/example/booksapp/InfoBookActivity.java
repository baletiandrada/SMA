package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.booksapp.adapters.BookReadDataAdapter;
import com.example.booksapp.adapters.QuoteAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.dataModels.QuoteModel;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;

public class InfoBookActivity extends AppCompatActivity {

    private TextView title, author;
    private RecyclerView recyclerView;
    private QuoteAdapter quoteAdapter;
    private List<QuoteModel> quotes_list = new ArrayList<QuoteModel>();
    FloatingActionButton fab;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_book);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        title.setText(bookStorageHelper.getBook_title());
        author.setText(bookStorageHelper.getAuthor_name());

        mQuotesDatabase.child(bookStorageHelper.getId_book()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDatabase(view);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        mQuotesDatabase.child(bookStorageHelper.getId_book()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData(DataSnapshot dataSnapshot) {
        quotes_list.removeAll(quotes_list);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String text = String.valueOf(ds.child("text_quote").getValue());
            QuoteModel newQuote = new QuoteModel(text);
            newQuote.setId(String.valueOf(ds.getKey()));
            quotes_list.add(newQuote);
        }
        if(!quotes_list.isEmpty())
            setRecyclerView();
    }

    public void setRecyclerView(){
        quoteAdapter = new QuoteAdapter((ArrayList<QuoteModel>) quotes_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(quoteAdapter);
    }

    public void addToDatabase(View view) {
        final TextInputEditText inputEditText;
        TextInputLayout textInputLayout;
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        new AlertDialog.Builder(this, R.style.InputDialogTheme);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.view_input_dialog, (ViewGroup) findViewById(R.id.et_input_dialog) , false);
        inputEditText = viewInflated.findViewById(R.id.et_input_dialog);
        textInputLayout = viewInflated.findViewById(R.id.til_input_dialog);
        alert.setView(viewInflated);
        alert.setTitle("Add quote");
        textInputLayout.setHint("Quote");
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (inputEditText.getText() == null || inputEditText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Quote field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                QuoteModel newQuote = new QuoteModel(inputEditText.getText().toString());
                String quote_id = mBooksReadDatabase.child(bookStorageHelper.getId_book()).push().getKey();
                mQuotesDatabase.child(bookStorageHelper.getId_book()).child(quote_id).setValue(newQuote);
                Toast.makeText(getApplicationContext(), "Quote added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void initializeViews(){
        title = findViewById(R.id.tv_title_info);
        author = findViewById(R.id.tv_author_info);
        recyclerView = findViewById(R.id.rv_quotes_info);
        fab = findViewById(R.id.fab_info);
    }
}