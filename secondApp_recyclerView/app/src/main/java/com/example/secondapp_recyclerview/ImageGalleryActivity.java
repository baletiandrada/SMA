package com.example.secondapp_recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.secondapp_recyclerview.FirebaseHelper.mImagesDatabase;

public class ImageGalleryActivity extends AppCompatActivity {
    private ImageUploadAdapter mImageAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<ImageUploadInfo> mImagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        mRecyclerView = findViewById(R.id.rv_images_upload_list);
        mImagesList = new ArrayList<>();

        /*if(mImageAdapter == null)
            mImageAdapter = new ImageUploadAdapter(mImagesList);*/

        mImagesDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot : snapshot.getChildren()){
                    String nameRetrieved = String.valueOf(childSnapshot.child("image_name").getValue());
                    String pathRetrieved = String.valueOf(childSnapshot.child("path").getValue());
                    ImageUploadInfo imageInfoCurrent = new ImageUploadInfo(nameRetrieved, pathRetrieved);
                    mImagesList.add(imageInfoCurrent);
                }
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mImageAdapter = new ImageUploadAdapter(mImagesList);
        mRecyclerView.setAdapter(mImageAdapter);
    }
}