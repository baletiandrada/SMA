package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.secondapp_recyclerview.FirebaseHelper.mBooksReadDatabase;

public class BookEditActivity extends AppCompatActivity {

    private EditText author_name, book_title, read_year;
    private Button edit_book_button, cancel_edit_activity_button;
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);
        initializeViews();

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

    public void goToPreviousActivity(){
        Intent intent = new Intent(this, AddBookReadToFirebase.class);
        startActivity(intent);
    }

    public void updateBookInFirebase(){
        if(author_name.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the author name", Toast.LENGTH_LONG).show();
            return;
        }
        if(book_title.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the book title", Toast.LENGTH_LONG).show();
            return;
        }
        if(read_year.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the year", Toast.LENGTH_LONG).show();
            return;
        }

        BookReadData updatedBook = new BookReadData(author_name.getText().toString(), book_title.getText().toString(), read_year.getText().toString());
        mBooksReadDatabase.child(bookStorageHelper.getId_book()).setValue(updatedBook);

        goToPreviousActivity();
        Toast.makeText( getApplicationContext() , "Book data updated successfully", Toast.LENGTH_LONG).show();
    }

    public void initializeViews(){
        author_name = findViewById(R.id.et_edit_author_name);
        book_title = findViewById(R.id.et_edit_book_title);
        read_year = findViewById(R.id.et_edit_read_year);
        edit_book_button = findViewById(R.id.btn_change_book_data);
        cancel_edit_activity_button = findViewById(R.id.btn_cancel_edit_activity);

        author_name.setText(bookStorageHelper.getAuthor_name());
        book_title.setText(bookStorageHelper.getBook_title());
        read_year.setText(bookStorageHelper.getRead_year());
    }
}