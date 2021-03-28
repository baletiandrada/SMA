package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;

public class BookEditActivity extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener{

    private EditText read_month, read_year, genre, description;
    private MultiAutoCompleteTextView author_name, book_title;
    private TextInputLayout layout_month, layout_year, layout_genre, layout_description;
    private Button edit_book_button, cancel_edit_activity_button;
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    private List<BookReadData> fav_books = new ArrayList<BookReadData>();

    ArrayList<String> authorNames = new ArrayList<String>();
    ArrayList<String> bookTitles = new ArrayList<String>();

    private ImageView chooseImg, choosedImg;
    private CardView cardViewImg;
    private CheckBox checkBox_add_photo;

    private String storagePath = "Book Images/";
    private StorageReference storageReference;
    int imageRequestCode = 123;
    private Uri filePath;
    private String mUri;

    @Override
    public void getImagePath(Uri imagePath) {
        cardViewImg.setVisibility(View.VISIBLE);
        Glide.with(this).load(imagePath).placeholder(R.mipmap.ic_launcher).into(choosedImg);
        filePath = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        cardViewImg.setVisibility(View.VISIBLE);
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
            ActivityCompat.requestPermissions(this, permissions, imageRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);
        initializeViews();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        getBooksFromFavouriteDB();


        getAuthorAndTitles();

        ArrayAdapter<String> adapterAuthor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, authorNames);
        author_name.setTokenizer(new SpaceTokenizer());
        author_name.setAdapter(adapterAuthor);

        ArrayAdapter<String> adapterTitle = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bookTitles);
        book_title.setTokenizer(new SpaceTokenizer());
        book_title.setAdapter(adapterTitle);

        checkBox_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImg.setVisibility(View.VISIBLE);
                choosedImg.setVisibility(View.VISIBLE);
            }
        });

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

    private void getAuthorAndTitles(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                authorNames.removeAll(authorNames);
                bookTitles.removeAll(authorNames);
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

            String month = "";
            if(read_month.getText().toString().isEmpty()){
                month = "";
            }
            else {
                String month_entered = read_month.getText().toString();
                List<String> list = Arrays.asList(AppConstants.MONTHS);
                if (list.contains(month_entered)) {
                    month = month_entered.substring(0, 3);
                } else {
                    Toast.makeText(this, "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
                HashMap<String, Object> map = new HashMap<>();
                map.put("author_name", author_name.getText().toString());
                map.put("title", book_title.getText().toString());
                map.put("read_month", month);
                map.put("read_year", read_year.getText().toString());

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
            checkBox_add_photo.setVisibility(View.GONE);
        }

        author_name.setText(bookStorageHelper.getAuthor_name());
        book_title.setText(bookStorageHelper.getBook_title());
    }


}