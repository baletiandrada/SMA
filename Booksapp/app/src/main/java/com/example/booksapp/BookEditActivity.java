package com.example.booksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class BookEditActivity extends AppCompatActivity {

    private EditText author_name, book_title, read_month, read_year, genre;
    private Button edit_book_button, cancel_edit_activity_button;
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);
        initializeViews();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        edit_book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBookInFirebase();
            }
        });

        cancel_edit_activity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPreviousActivity();
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

    public void goToPreviousActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    public void updateBookInFirebase(){
        if(author_name.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the author name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(book_title.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the book title", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        String param_bookTable = intent.getStringExtra(AppConstants.param_bookTable);

        if(param_bookTable.isEmpty()){
            Toast.makeText( getApplicationContext() , "Something went worng. Please login again", Toast.LENGTH_SHORT).show();
            goToPreviousActivity();
        }

        else if( ! param_bookTable.equals("Books recommended") ){
                if(read_year.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter the year", Toast.LENGTH_SHORT).show();
                    return;
                }
                String month;
                if(read_month.getText().toString().isEmpty())
                    month = "";
                else {
                    month = read_month.getText().toString();
                    switch (month){
                        case "ianuarie": {
                            month = "ian";
                            break;
                        }
                        case "februarie": {
                            month = "feb";
                            break;
                        }
                        case "martie": break;
                        case "aprilie": break;
                        case "mai": break;
                        case "iunie": break;
                        case "iulie": break;
                        case "august": break;
                        case "septembrie": {
                            month = "sept";
                            break;
                        }
                        case "octombrie": {
                            month = "oct";
                            break;
                        }
                        case "noiembrie": {
                            month = "nov";
                            break;
                        }
                        case "decembrie": {
                            month = "dec";
                            break;
                        }
                        default:{
                            Toast.makeText(this, "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                }

                BookReadData updatedBook = new BookReadData(author_name.getText().toString(), book_title.getText().toString(), month, read_year.getText().toString());
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(param_bookTable.equals("Read books"))
                    mBooksReadDatabase.child(currentUser.getUid()).child(bookStorageHelper.getId_book()).setValue(updatedBook);
                else if(param_bookTable.equals("Planned books"))
                    mBooksPlannedDatabase.child(currentUser.getUid()).child(bookStorageHelper.getId_book()).setValue(updatedBook);
            }
        }
        else{

            if(genre.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter the genre", Toast.LENGTH_SHORT).show();
                return;
            }

            BookReadData updatedRecommendedBook = new BookReadData(author_name.getText().toString(), book_title.getText().toString(), genre.getText().toString());
            mBooksRecommendedDatabase.child(bookStorageHelper.getId_book()).setValue(updatedRecommendedBook);
        }
        goToPreviousActivity();
        Toast.makeText( getApplicationContext() , "Book data updated successfully", Toast.LENGTH_SHORT).show();
    }

    public void initializeViews(){
        author_name = findViewById(R.id.et_edit_author_name);
        book_title = findViewById(R.id.et_edit_book_title);
        read_month = findViewById(R.id.et_edit_read_month);
        read_year = findViewById(R.id.et_edit_read_year);
        genre = findViewById(R.id.et_edit_genre);
        edit_book_button = findViewById(R.id.btn_change_book_data);
        cancel_edit_activity_button = findViewById(R.id.btn_cancel_edit_activity);

        Intent intent = getIntent();
        String param_bookTable = intent.getStringExtra(AppConstants.param_bookTable);

        if( ! param_bookTable.equals("Books recommended") ){
            genre.setVisibility(View.GONE);
            read_month.setText(bookStorageHelper.getRead_month());
            read_year.setText(bookStorageHelper.getRead_year());
        }
        else{
            edit_book_button.setVisibility(View.GONE);
            cancel_edit_activity_button.setVisibility(View.GONE);
            genre.setText(bookStorageHelper.getGenre());
        }

        author_name.setText(bookStorageHelper.getAuthor_name());
        book_title.setText(bookStorageHelper.getBook_title());
    }

}