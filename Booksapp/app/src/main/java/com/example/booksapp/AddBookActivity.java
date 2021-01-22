package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.dataModels.ImageUploadInfo;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mImagesDatabase;

public class AddBookActivity extends AppCompatActivity {

    private EditText authorName, bookTitle, month, year, genre, description;
    private TextInputLayout layout_author, layout_title, layout_month, layout_year, layout_genre, layout_description;
    private Button add_book_button, cancel_add_activity_button;
    private ImageView chooseImg, choosedImg;
    private CardView cardViewImg;
    private CheckBox checkBox_add_photo;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Animation scaleUp, scaleDown;

    BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();

    private String storagePath = "Book Images/";
    private StorageReference storageReference;
    int imageRequestCode = 7;
    private Uri filePath;
    private String mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        initializeViews();
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), imageRequestCode);
            }
        });

        checkBox_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImg.setVisibility(View.VISIBLE);
                choosedImg.setVisibility(View.VISIBLE);
            }
        });

        add_book_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    add_book_button.startAnimation(scaleUp);
                    if(checkBox_add_photo.isChecked()){
                        Toast.makeText(getApplicationContext(), "Wait a few seconds...", Toast.LENGTH_LONG).show();
                        setImageUri();
                    }
                    else addBookInFirebase();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    add_book_button.startAnimation(scaleDown);
                }
                return true;
            }
        });

        cancel_add_activity_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    cancel_add_activity_button.startAnimation(scaleUp);
                    goToPreviousActivity();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    cancel_add_activity_button.startAnimation(scaleDown);
                }
                return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == imageRequestCode && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            cardViewImg.setVisibility(View.VISIBLE);
            Glide.with(this).load(filePath).placeholder(R.mipmap.ic_launcher).into(choosedImg);
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }


    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToPreviousActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    public void setImageUri(){
        if (filePath != null) {
            StorageReference storageReference2nd = storageReference.child(storagePath + System.currentTimeMillis() + "." + getFileExtension(filePath));

            storageReference2nd.putFile(filePath)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if(!task.isSuccessful())
                                throw task.getException();
                            return storageReference2nd.getDownloadUrl();
                        }
                    }). addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        //Toast.makeText(getApplicationContext(), "Image uploaded successfully ", Toast.LENGTH_LONG).show();
                        Uri downloadUrl = task.getResult();
                        mUri = downloadUrl.toString();
                        addBookInFirebase();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void addBookInFirebase(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        List<BookReadData> books_read_list = bookListStorageHelper.getBooks_read_list();
        List<BookReadData> books_planned_list = bookListStorageHelper.getBooks_planned_list();
        List<BookReadData> books_recommended_list = bookListStorageHelper.getBooks_recommended_list();

        if(authorName.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the author name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(bookTitle.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter the book title", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        String param_bookTable = intent.getStringExtra(AppConstants.param_bookTable);

        if(param_bookTable.isEmpty()){
            Toast.makeText( getApplicationContext() , "Something went worng. Please login again", Toast.LENGTH_SHORT).show();
            goToPreviousActivity();
        }

        if( ! param_bookTable.equals("Recommended books") ){

            if(year.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter the year", Toast.LENGTH_SHORT).show();
                return;
            }

            String month_aux;
            if(month.getText().toString().isEmpty())
                month_aux = "";
            else {
                month_aux = month.getText().toString();
                switch (month_aux) {
                    case "ianuarie": {
                        month_aux = "ian";
                        break;
                    }
                    case "februarie": {
                        month_aux = "feb";
                        break;
                    }
                    case "martie":
                        break;
                    case "aprilie":
                        break;
                    case "mai":
                        break;
                    case "iunie":
                        break;
                    case "iulie":
                        break;
                    case "august":
                        break;
                    case "septembrie": {
                        month_aux = "sept";
                        break;
                    }
                    case "octombrie": {
                        month_aux = "oct";
                        break;
                    }
                    case "noiembrie": {
                        month_aux = "nov";
                        break;
                    }
                    case "decembrie": {
                        month_aux = "dec";
                        break;
                    }
                    default: {
                        Toast.makeText(this, "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            if(param_bookTable.equals("Read books")){
                if(bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_read_list)
                        && bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_planned_list)){
                    BookReadData newBook;
                    newBook= new BookReadData(authorName.getText().toString(), bookTitle.getText().toString(), month_aux, year.getText().toString());
                    if(!(mUri==null) && !mUri.isEmpty())
                       newBook.setUri(mUri);
                    String book_id = mBooksReadDatabase.child(currentUser.getUid()).push().getKey();
                    mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                    Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();
            }
            else if(param_bookTable.equals("Planned books")){
                if(bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_read_list)
                        && bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_planned_list)){
                    BookReadData newBook = new BookReadData(authorName.getText().toString(), bookTitle.getText().toString(), month_aux, year.getText().toString());
                    if(!(mUri==null) && !mUri.isEmpty())
                        newBook.setUri(mUri);
                    String book_id = mBooksPlannedDatabase.child(currentUser.getUid()).push().getKey();
                    mBooksPlannedDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                    Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();
                
            }
            else Toast.makeText(getApplicationContext(), "Please go back to previous activity", Toast.LENGTH_SHORT).show();
        }
        else{
            if(genre.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter the genre", Toast.LENGTH_SHORT).show();
                return;
            }
            if(bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_recommended_list)){
                String book_id = mBooksRecommendedDatabase.child(currentUser.getUid()).push().getKey();
                BookReadData newBook = new BookReadData(authorName.getText().toString(), bookTitle.getText().toString(), genre.getText().toString());
                if(!(mUri==null) && !mUri.isEmpty())
                    newBook.setUri(mUri);

                if(!description.getText().toString().isEmpty())
                    newBook.setDescription(description.getText().toString());

                mBooksRecommendedDatabase.child(book_id).setValue(newBook);
                Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "The book already exists", Toast.LENGTH_LONG).show();

        }
        goToPreviousActivity();
    }

    public boolean bookNotExistsInDBBooks(String author, String title, List<BookReadData> books_list){
        for(BookReadData current_book : books_list){
            if(( current_book.getAuthor_name().toLowerCase().contains(author.toLowerCase())
                    ||author.toLowerCase().contains(current_book.getAuthor_name().toLowerCase()) )
                    &&( current_book.getTitle().toLowerCase().contains(title.toLowerCase())
                    ||title.toLowerCase().contains(current_book.getTitle().toLowerCase()) ))
                return false;
        }
        return true;
    }

    public void initializeViews(){
        authorName = findViewById(R.id.et_add_author_name);
        bookTitle = findViewById(R.id.et_add_book_title);
        month = findViewById(R.id.et_add_month);
        year = findViewById(R.id.et_add_year);
        genre = findViewById(R.id.et_add_genre);

        description = findViewById(R.id.et_add_description);
        layout_description = findViewById(R.id.layout_add_description);

        layout_author = findViewById(R.id.layout_add_author_name);
        layout_title = findViewById(R.id.layout_add_book_title);
        layout_month = findViewById(R.id.layout_add_month);
        layout_year = findViewById(R.id.layout_add_year);
        layout_genre = findViewById(R.id.layout_add_genre);
        add_book_button = findViewById(R.id.btn_add_book);
        cancel_add_activity_button = findViewById(R.id.btn_cancel_add_book_activity);

        checkBox_add_photo = findViewById(R.id.checkbox_add_photo);

        storageReference = FirebaseStorage.getInstance().getReference();
        chooseImg = findViewById(R.id.iv_chooseImg);
        choosedImg = findViewById(R.id.iv_showImgChoosed);
        cardViewImg = findViewById(R.id.card_view_img);
        cardViewImg.setVisibility(View.GONE);

        Intent intent = getIntent();
        String param_add_bookTable = intent.getStringExtra(AppConstants.PARAM_ADD_BOOK_TABLE);
        if(param_add_bookTable==null || param_add_bookTable.isEmpty())
        {
            goToPreviousActivity();
        }

        if( ! param_add_bookTable.equals("Recommended books") ){
            layout_genre.setVisibility(View.GONE);
            genre.setVisibility(View.GONE);
        }
        else{
            layout_description.setVisibility(View.VISIBLE);
            layout_month.setVisibility(View.GONE);
            month.setVisibility(View.GONE);
            layout_year.setVisibility(View.GONE);
            year.setVisibility(View.GONE);
        }
    }

}