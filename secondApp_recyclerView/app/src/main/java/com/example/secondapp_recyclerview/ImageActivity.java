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
    private String storagePath = "Images uploaded/";
    private Button chooseImage_button, uploadImage_button, uploadImageAgain_button, goToImageGallery_button;
    private EditText imageName_et;
    private ImageView choosedImage_iv;
    private Uri filePath;
    private StorageReference storageReference;
    int imageRequestCode = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        initializeViews();

        chooseImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please select an image"), imageRequestCode);
            }
        });
        uploadImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calling method to upload selected image on Firebase storage.
                UploadImageFileToFirebaseStorage();
                chooseImage_button.setVisibility(View.GONE);
                uploadImage_button.setVisibility(View.GONE);
                imageName_et.setVisibility(View.GONE);
                choosedImage_iv.setVisibility(View.GONE);

                uploadImageAgain_button.setVisibility(View.VISIBLE);
            }
        });


        uploadImageAgain_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageAgain_button.setVisibility(View.GONE);

                chooseImage_button.setVisibility(View.VISIBLE);
                uploadImage_button.setVisibility(View.VISIBLE);
                imageName_et.setVisibility(View.VISIBLE);
                choosedImage_iv.setVisibility(View.VISIBLE);
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

        if (requestCode == imageRequestCode && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Glide.with(this).load(filePath).placeholder(R.mipmap.ic_launcher).into(choosedImage_iv);
            chooseImage_button.setText("Choose another image");
        }
    }

    public String GetFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    public void UploadImageFileToFirebaseStorage() {
        if(imageName_et.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter an image name", Toast.LENGTH_LONG).show();
            return;
        }

        else if (filePath != null) {
            StorageReference storageReference2nd = storageReference.child(storagePath + System.currentTimeMillis() + "." + GetFileExtension(filePath));

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
                         Toast.makeText(getApplicationContext(), "Image uploaded successfully ", Toast.LENGTH_LONG).show();
                         Uri downloadUrl = task.getResult();
                         String mUri = downloadUrl.toString();

                         String TempImageName = imageName_et.getText().toString().trim();
                         ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, mUri);

                         String ImageUploadId = mImagesDatabase.push().getKey();
                         mImagesDatabase.child(ImageUploadId).setValue(imageUploadInfo);

                         choosedImage_iv.setImageBitmap(null);
                         imageName_et.setText(null);
                         chooseImage_button.setText("Choose image");
                     }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show();
        }
    }

    public void viewGallery(){
        Intent intent = new Intent(this, ImageGalleryActivity.class);
        startActivity(intent);
    }

    public void initializeViews(){
        storageReference = FirebaseStorage.getInstance().getReference();
        chooseImage_button = findViewById(R.id.btn_chooseImage);
        uploadImage_button = findViewById(R.id.btn_uploadImage);
        uploadImageAgain_button = findViewById(R.id.btn_uploadAgain);
        uploadImageAgain_button.setVisibility(View.GONE);
        imageName_et = findViewById(R.id.et_imageName);
        choosedImage_iv = findViewById(R.id.iv_showImageChoosed);
        goToImageGallery_button = findViewById(R.id.btn_goToImageGallery);
    }

}