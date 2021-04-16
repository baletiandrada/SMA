package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TryActivity extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener{

    private TextView tv;
    private Button btn;
    private CardView cardViewImg;
    private ImageView chooseImg, showImg;

    int requestCode = 12345;
    private Uri filePath;
    private Bitmap mBitmap;

    @Override
    public void getImagePath(Uri imagePath) {
        cardViewImg.setVisibility(View.VISIBLE);
        Glide.with(this).load(imagePath).placeholder(R.mipmap.ic_launcher).into(showImg);
        filePath = imagePath;
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap==null)
            Toast.makeText(this, "bitmap is null", Toast.LENGTH_LONG).show();
        else {
            mBitmap = bitmap;
            cardViewImg.setVisibility(View.VISIBLE);
            showImg.setImageBitmap(bitmap);
        }

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
        setContentView(R.layout.activity_try);

        tv = findViewById(R.id.tv_text_detect_try);
        btn = findViewById(R.id.btn_detect_text_try);
        cardViewImg = findViewById(R.id.card_view_img_try);
        chooseImg = findViewById(R.id.iv_chooseImg_try);
        showImg = findViewById(R.id.iv_showImgChoosed_try);

        init();

       btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    detectTextFromImage();
                } catch (IOException | CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void detectTextFromImage() throws IOException, CameraAccessException {
        InputImage image;
        if(filePath!=null)
            image = InputImage.fromFilePath(this, filePath);
        else
            image = InputImage.fromBitmap(mBitmap, 0);

        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                displayTextFromImage(text);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayTextFromImage(Text text) {
        String text_for_tv = "";
        List<Text.TextBlock> blocks = text.getTextBlocks();

        if(blocks.size()==0)
            Toast.makeText(getApplicationContext(), "No text found in image", Toast.LENGTH_LONG).show();

        for(Text.TextBlock block: blocks){
            String text_aux = block.getText().concat(" ");
            text_for_tv = text_for_tv.concat(text_aux);
        }
        Toast.makeText(getApplicationContext(), text_for_tv, Toast.LENGTH_LONG).show();
        tv.setText(text_for_tv);
    }

    private byte[] getBytes(InputStream iStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = iStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


}