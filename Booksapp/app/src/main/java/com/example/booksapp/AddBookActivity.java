package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mImagesDatabase;

public class AddBookActivity extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener {

    private EditText year, genre, description, month;
    private MultiAutoCompleteTextView authorName, bookTitle;
    private TextInputLayout layout_month, layout_year, layout_genre, layout_description;
    private Button add_book_button, cancel_add_activity_button;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();

    ArrayList<String> authorNames = new ArrayList<String>();
    ArrayList<String> bookTitles = new ArrayList<String>();

    List<BookReadData> books_from_DB = new ArrayList<BookReadData>();

    private String storagePath = "Book Images/";
    private StorageReference storageReference;
    int requestCode = 1234;
    private Uri filePath;
    private String mUri;

    private ImageView chooseImg, choosedImg;
    private CardView cardViewImg;
    private CheckBox checkBox_add_photo;

    @Override
    public void getImagePath(Uri imagePath) {
        cardViewImg.setVisibility(View.VISIBLE);
        Glide.with(this).load(imagePath).placeholder(R.mipmap.ic_launcher).into(choosedImg);
        filePath = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        cardViewImg.setVisibility(View.VISIBLE);
        //choosedImg.setImageBitmap(bitmap);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "val", null);
        Uri uri = Uri.parse(path);
        Glide.with(this).load(uri).placeholder(R.mipmap.ic_launcher).into(choosedImg);
        filePath = uri;
    }

    private void init(){
        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText( getApplicationContext() , "Opening dialog for choosing a photo", Toast.LENGTH_SHORT).show();
                SelectPhotoDialog dialog = new SelectPhotoDialog();
                dialog.show(getSupportFragmentManager(), "Select Photo");

            }
        });
    }

    private void verifyPermissions(){
        String permissions[] = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        initializeViews();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        getAuthorAndTitles();

        ArrayAdapter<String> adapterAuthor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, authorNames);
        authorName.setTokenizer(new SpaceTokenizer());
        authorName.setAdapter(adapterAuthor);

        ArrayAdapter<String> adapterTitle = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookTitles);
        bookTitle.setTokenizer(new SpaceTokenizer());
        bookTitle.setAdapter(adapterTitle);

        checkBox_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImg.setVisibility(View.VISIBLE);
                choosedImg.setVisibility(View.VISIBLE);
            }
        });

        add_book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox_add_photo.isChecked()){
                    Toast.makeText(getApplicationContext(), "Wait a few seconds...", Toast.LENGTH_LONG).show();
                    setImageUri();
                }
                else addBookInFirebase();
            }
        });

        cancel_add_activity_button.setOnClickListener(new View.OnClickListener() {
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

    private void getAuthorAndTitles(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                authorNames.removeAll(authorNames);
                bookTitles.removeAll(authorNames);
                books_from_DB.removeAll(books_from_DB);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    List<String> titles = Arrays.asList(book_title.split("\\s+"));
                    if(!authorNames.contains(author_name))
                        authorNames.add(author_name);
                    for(String title: titles){
                        if(!bookTitles.contains(title))
                            bookTitles.add(title);
                    }

                    BookReadData newBook = new BookReadData();
                    newBook.setAuthor_name(author_name);
                    newBook.setTitle(book_title);
                    newBook.setId(String.valueOf(ds.getKey()));
                    books_from_DB.add(newBook);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToPreviousActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

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

            String month_aux = "";
            if(month.getText().toString().isEmpty()){
                month_aux = "";
            }
            else {
                String month_entered = month.getText().toString();
                List<String> list = Arrays.asList(AppConstants.MONTHS);
                if (list.contains(month_entered)) {
                    month_aux = month_entered.substring(0, 3);
                } else {
                    Toast.makeText(this, "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            boolean book_exists_in_DB = false;
            String book_id_extracted_from_DB="";

            if(!bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_from_DB)){
                book_exists_in_DB = true;
                book_id_extracted_from_DB = getBookId(authorName.getText().toString(), bookTitle.getText().toString());
            }

            if(param_bookTable.equals("Read books")){
                if(book_exists_in_DB){
                    if(!bookExistsById(book_id_extracted_from_DB, books_read_list) && !bookExistsById(book_id_extracted_from_DB, books_planned_list)){
                        String book_id = mBooksReadDatabase.child(currentUser.getUid()).push().getKey();
                        BookReadData newBook = new BookReadData();
                        newBook.setId(book_id_extracted_from_DB);
                        newBook.setRead_month(month_aux);
                        newBook.setRead_year(year.getText().toString());
                        mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                    }
                    else Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();
                }
                else{
                    if(bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_read_list)
                            && bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_planned_list)){
                        BookReadData newBook;
                        newBook= new BookReadData(authorName.getText().toString(), bookTitle.getText().toString(), month_aux, year.getText().toString());
                        String book_id = mBooksReadDatabase.child(currentUser.getUid()).push().getKey();
                        mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                        Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();
                }

            }
            else if(param_bookTable.equals("Planned books")){
                if(book_exists_in_DB){
                    if(!bookExistsById(book_id_extracted_from_DB, books_planned_list) && !bookExistsById(book_id_extracted_from_DB, books_read_list)){
                        String book_id = mBooksPlannedDatabase.child(currentUser.getUid()).push().getKey();
                        BookReadData newBook = new BookReadData();
                        newBook.setId(book_id_extracted_from_DB);
                        newBook.setRead_month(month_aux);
                        newBook.setRead_year(year.getText().toString());
                        mBooksPlannedDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                    }
                    else Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();

                }
                else{
                    if(bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_read_list)
                            && bookNotExistsInDBBooks(authorName.getText().toString(), bookTitle.getText().toString(), books_planned_list)){
                        BookReadData newBook = new BookReadData(authorName.getText().toString(), bookTitle.getText().toString(), month_aux, year.getText().toString());
                        String book_id = mBooksPlannedDatabase.child(currentUser.getUid()).push().getKey();
                        mBooksPlannedDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
                        Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();
                }
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

    private boolean bookExistsById(String book_id, List<BookReadData> books) {
        for(BookReadData current_book : books){
            if(book_id.equals(current_book.getId()))
                return true;
        }
        return false;
    }

    public String getBookId(String author_name, String book_title){
        for(BookReadData book: books_from_DB){
            if(author_name.toLowerCase().contains(book.getAuthor_name().toLowerCase())
            && book_title.toLowerCase().contains(book.getTitle().toLowerCase())){
                return book.getId();
            }
        }
        return null;
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

        checkBox_add_photo = findViewById(R.id.checkbox_add_photo);
        storageReference = FirebaseStorage.getInstance().getReference();
        chooseImg = findViewById(R.id.iv_chooseImg);
        choosedImg = findViewById(R.id.iv_showImgChoosed);
        cardViewImg = findViewById(R.id.card_view_img);
        cardViewImg.setVisibility(View.GONE);

        layout_month = findViewById(R.id.layout_add_month);
        layout_year = findViewById(R.id.layout_add_year);
        layout_genre = findViewById(R.id.layout_add_genre);
        add_book_button = findViewById(R.id.btn_add_book);
        cancel_add_activity_button = findViewById(R.id.btn_cancel_add_book_activity);

        Intent intent = getIntent();
        String param_add_bookTable = intent.getStringExtra(AppConstants.PARAM_ADD_BOOK_TABLE);
        if(param_add_bookTable==null || param_add_bookTable.isEmpty())
        {
            goToPreviousActivity();
        }

        if( ! param_add_bookTable.equals("Recommended books") ){
            layout_genre.setVisibility(View.GONE);
            genre.setVisibility(View.GONE);
            checkBox_add_photo.setVisibility(View.GONE);
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