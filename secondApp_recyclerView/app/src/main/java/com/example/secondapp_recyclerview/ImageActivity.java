package com.example.secondapp_recyclerview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static com.example.secondapp_recyclerview.FirebaseHelper.mImagesDatabase;

import java.io.IOException;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    // Folder path for Firebase Storage.
    private String Storage_Path = "Images uploaded/";
    // Creating button.
    private Button ChooseButton, UploadButton, goToImageGallery_button;
    // Creating EditText.
    private EditText ImageName;
    // Creating ImageView.
    private ImageView SelectImage;
    // Creating URI.
    private Uri FilePathUri;
    // Creating StorageReference and DatabaseReference object.
    private StorageReference storageReference;
    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        initializeViews();

        // Adding click listener to Choose image button.
        ChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating intent.
                Intent intent = new Intent();
                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);
            }
        });
        // Adding click listener to Upload image button.
        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calling method to upload selected image on Firebase storage.
                UploadImageFileToFirebaseStorage();

            }
        });

        goToImageGallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewGallery();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {
            FilePathUri = data.getData();
            Glide.with(this).load(FilePathUri).placeholder(R.mipmap.ic_launcher).into(SelectImage);
            ChooseButton.setText("Choose another image");
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage() {
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {
            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
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
                         Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                         Uri downloadUrl = task.getResult();
                         String mUri = downloadUrl.toString();

                         String TempImageName = ImageName.getText().toString().trim();
                         ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, mUri);

                         String ImageUploadId = mImagesDatabase.push().getKey();
                         mImagesDatabase.child(ImageUploadId).setValue(imageUploadInfo);

                         SelectImage.setImageBitmap(null);
                         ImageName.setText(null);
                         ChooseButton.setText("Choose image");
                     }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();
        }
    }

    public void viewGallery(){
        Intent intent = new Intent(this, ImageGalleryActivity.class);
        startActivity(intent);
    }

    public void initializeViews(){
        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();
        //Assign ID'S to button.
        ChooseButton = (Button) findViewById(R.id.ButtonChooseImage);
        UploadButton = (Button) findViewById(R.id.ButtonUploadImage);
        // Assign ID's to EditText.
        ImageName = (EditText) findViewById(R.id.ImageNameEditText);
        // Assign ID'S to image view.
        SelectImage = findViewById(R.id.ShowImageView);
        goToImageGallery_button = findViewById(R.id.btn_goToImageGallery);
    }

}