package com.example.booksapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

public class SelectPhotoDialog extends DialogFragment {

    private static final String TAG = "Select Photo Dialog";
    private static final int pickFileRequestCode = 7;
    private static final int cameraRequestCode = 10;

    //public interface for Edit book activity

    public interface OnPhotoSelectedListener{
        void getImagePath(Uri imagePath);
        void getImageBitmap(Bitmap bitmap);
    }
    OnPhotoSelectedListener onPhotoSelectedListener;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pickFileRequestCode && resultCode == Activity.RESULT_OK){
            Uri selectedImageUri = data.getData();
            onPhotoSelectedListener.getImagePath(selectedImageUri);
            getDialog().dismiss();
        }
        else if(requestCode == cameraRequestCode && resultCode == Activity.RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            onPhotoSelectedListener.getImageBitmap(bitmap);
            getDialog().dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            onPhotoSelectedListener = (OnPhotoSelectedListener) getActivity();
        }catch(ClassCastException e){
            Log.d(TAG, "Class Cast Exception " + e.getMessage());
        }
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_photo, container, false);

        TextView selectPhoto = view.findViewById(R.id.choose_photo_tv);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Accessing phone memory");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select image"), pickFileRequestCode);
            }
        });

        TextView takePhoto = view.findViewById(R.id.take_photo_tv);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: starting camera");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(Intent.createChooser(intent, "Select image"), cameraRequestCode);
            }
        });

        return view;
    }
}
