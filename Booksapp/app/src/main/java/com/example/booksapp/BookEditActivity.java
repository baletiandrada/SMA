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
import com.example.booksapp.helpers.BookStorageHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;

public class BookEditActivity extends AppCompatActivity {

    private EditText author_name, book_title, read_month, read_year, genre, description;
    private TextInputLayout layout_author, layout_title, layout_month, layout_year, layout_genre, layout_description;
    private Button edit_book_button, cancel_edit_activity_button;
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    private ImageView chooseImg, choosedImg;
    private CardView cardViewImg;
    private CheckBox checkBox_add_photo;

    private Animation scaleUp, scaleDown;

    private String storagePath = "Book Images/";
    private StorageReference storageReference;
    int imageRequestCode = 7;
    private Uri filePath;
    private String mUri;

    private List<BookReadData> fav_books = new ArrayList<BookReadData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);
        initializeViews();
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        getBooksFromFavouriteDB();

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


        edit_book_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    edit_book_button.startAnimation(scaleUp);
                    if(checkBox_add_photo.isChecked()){
                        Toast.makeText(getApplicationContext(), "Wait a few seconds...", Toast.LENGTH_LONG).show();
                        setImageUri();
                    }
                    else updateBookInFirebase();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    edit_book_button.startAnimation(scaleDown);
                }
                return true;
            }
        });
        /*edit_book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBookInFirebase();
            }
        });*/

        cancel_edit_activity_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    cancel_edit_activity_button.startAnimation(scaleUp);
                    goToPreviousActivity();
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    cancel_edit_activity_button.startAnimation(scaleDown);
                }
                return true;
            }
        });
        /*cancel_edit_activity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPreviousActivity();
            }
        });*/
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
                        updateBookInFirebase();
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

        if( ! param_bookTable.equals("Recommended books") ){

                if(read_year.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter the year", Toast.LENGTH_SHORT).show();
                    return;
                }

                String month;
                if(read_month.getText().toString().isEmpty())
                    month = "";
                else {
                    month = read_month.getText().toString();
                    switch (month) {
                        case "ianuarie": {
                            month = "ian";
                            break;
                        }
                        case "februarie": {
                            month = "feb";
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
                        default: {
                            Toast.makeText(this, "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("author_name", author_name.getText().toString());
                map.put("title", book_title.getText().toString());
                map.put("read_month", month);
                map.put("read_year", read_year.getText().toString());
                if(!(mUri==null) && !mUri.isEmpty())
                    map.put("uri", mUri);

                if(param_bookTable.equals("Read books")){
                    mBooksReadDatabase.child(currentUser.getUid()).child(bookStorageHelper.getId_book()).updateChildren(map);
                    if(favouriteDBContainsBook(bookStorageHelper.getAuthor_name(), bookStorageHelper.getBook_title()))
                        mFavouriteBooksDatabase.child(currentUser.getUid()).child(bookStorageHelper.getId_book()).updateChildren(map);
                }
                else if(param_bookTable.equals("Planned books"))
                        mBooksPlannedDatabase.child(currentUser.getUid()).child(bookStorageHelper.getId_book()).updateChildren(map);
                     else Toast.makeText(getApplicationContext(), "Please login again", Toast.LENGTH_SHORT).show();

        }
        else{
            if(genre.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter the genre", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("author_name", author_name.getText().toString());
            map.put("title", book_title.getText().toString());
            map.put("genre", genre.getText().toString());
            if(!description.getText().toString().isEmpty()) {
                map.put("description", description.getText().toString());
            }
            if(!(mUri==null) && !mUri.isEmpty())
                map.put("uri", mUri);
            mBooksRecommendedDatabase.child(bookStorageHelper.getId_book()).updateChildren(map);
        }
        goToPreviousActivity();
        Toast.makeText( getApplicationContext() , "Book data updated successfully", Toast.LENGTH_SHORT).show();
    }

    public void getBooksFromFavouriteDB(){
        mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fav_books.removeAll(fav_books);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    BookReadData newBook = new BookReadData(author_name, book_title);
                    newBook.setId(String.valueOf(ds.getKey()));
                    fav_books.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean favouriteDBContainsBook(String author, String title){
        for(BookReadData current_book : fav_books){
            if(current_book.getAuthor_name().toLowerCase().equals(author.toLowerCase())
                    && current_book.getTitle().toLowerCase().equals(title.toLowerCase()))
                return true;
        }
        return false;
    }

    public void initializeViews(){
        author_name = findViewById(R.id.et_edit_author_name);
        book_title = findViewById(R.id.et_edit_book_title);
        read_month = findViewById(R.id.et_edit_read_month);
        read_year = findViewById(R.id.et_edit_read_year);
        genre = findViewById(R.id.et_edit_genre);
        description = findViewById(R.id.et_edit_description);
        layout_description = findViewById(R.id.layout_edit_description);
        layout_author = findViewById(R.id.layout_edit_author_name);
        layout_title = findViewById(R.id.layout_edit_book_title);
        layout_month = findViewById(R.id.layout_edit_read_month);
        layout_year = findViewById(R.id.layout_edit_read_year);
        layout_genre = findViewById(R.id.layout_edit_genre);
        edit_book_button = findViewById(R.id.btn_change_book_data);
        cancel_edit_activity_button = findViewById(R.id.btn_cancel_edit_activity);

        checkBox_add_photo = findViewById(R.id.checkbox_add_photo_edit);

        storageReference = FirebaseStorage.getInstance().getReference();
        chooseImg = findViewById(R.id.iv_chooseImg_edit);
        choosedImg = findViewById(R.id.iv_showImgChoosed_edit);
        cardViewImg = findViewById(R.id.card_view_img_edit);
        cardViewImg.setVisibility(View.GONE);

        Intent intent = getIntent();
        String param_bookTable = intent.getStringExtra(AppConstants.param_bookTable);

        if( ! param_bookTable.equals("Recommended books") ){
            layout_genre.setVisibility(View.GONE);
            genre.setVisibility(View.GONE);
            read_month.setText(bookStorageHelper.getRead_month());
            read_year.setText(bookStorageHelper.getRead_year());
        }
        else{
            layout_description.setVisibility(View.VISIBLE);
            layout_month.setVisibility(View.GONE);
            read_month.setVisibility(View.GONE);
            layout_year.setVisibility(View.GONE);
            read_year.setVisibility(View.GONE);

            genre.setText(bookStorageHelper.getGenre());
        }

        author_name.setText(bookStorageHelper.getAuthor_name());
        book_title.setText(bookStorageHelper.getBook_title());
    }

}