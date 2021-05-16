package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
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
import java.util.ArrayList;
import java.util.Arrays;
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


    ArrayList<String> words_list = new ArrayList<>();

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

        final boolean[] lower_case = {false};

        words_list.add("Calul?");
        words_list.add("lacul!");
        words_list.add("parul, seara.");
        words_list.add("nu");
        words_list.add("este");
        words_list.add("aici");
        words_list.add("dincolo");

        Character[] end_characters = {'.', ',', ';', '!', '?', ' '};

        MultiAutoCompleteTextView text = findViewById(R.id.text_multi_autocomplete);
        text.setTokenizer(new SpaceTokenizer());

        /*ArrayAdapter<String> adapterAuthor = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                FullTextSearch.searchForText(text.getText().toString(),
                        words_list));

        text.setAdapter(adapterAuthor);*/

        ListView listView = findViewById(R.id.words_lv);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                if(text.getText().toString().length()>1){
                    String text_input = text.getText().toString();
                    List<String> text_splitted = Arrays.asList(text_input.split("\\s+"));
                    String last_word = text_splitted.get(text_splitted.size()-1);
                    if(last_word.length()>1 && !Arrays.asList(end_characters).contains(text_input.charAt(text_input.length()-1)))
                        listView.setVisibility(View.VISIBLE);
                    else
                        listView.setVisibility(View.GONE);
                    if(!words_list.contains(last_word)){
                        if (text_splitted.size() > 1) {
                            String word_bef_last = text_splitted.get(text_splitted.size()-2);
                            if(word_bef_last.endsWith(".") || word_bef_last.endsWith("?") || word_bef_last.endsWith("!")){
                                words_list = changeFirstLetter(words_list);
                                lower_case[0] = false;
                            }
                            else if(!lower_case[0]){
                                words_list = listToLowerCase(words_list);
                                lower_case[0] = true;
                            }
                        }

                        ArrayList<String> words = FullTextSearch.searchForText(last_word, words_list);
                        listView.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, words));
                    }
                    else listView.setVisibility(View.GONE);
                }
                else{
                    listView.setVisibility(View.GONE);
                    words_list = changeFirstLetter(words_list);
                    lower_case[0] = false;
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String input = text.getText().toString();
                if(input.contains(" ")){
                    StringBuilder input1 = replaceTextWithItemView(input);
                    input = String.valueOf(input1);
                    text.setText(input + parent.getItemAtPosition(position));
                }
                else
                   text.setText(parent.getItemAtPosition(position).toString());
                text.setSelection(text.getText().toString().length());
                listView.setVisibility(View.GONE);
            }
        });

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

    public ArrayList<String> listToLowerCase(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            word = word.toLowerCase();
            words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public ArrayList<String> changeFirstLetter(ArrayList<String> words_list){
        ArrayList<String> words_list_aux = new ArrayList<>();
        for(String word : words_list){
            if(word.charAt(0)>='a' && word.charAt(0)<='z'){
                String firstCh = String.valueOf(word.charAt(0));
                firstCh = firstCh.toUpperCase();
                String substring = word.substring(1);
                firstCh = firstCh.concat(substring);
                words_list_aux.add(firstCh);
            }
            else words_list_aux.add(word);
        }
        words_list.clear();
        words_list.addAll(words_list_aux);
        return words_list;
    }

    public StringBuilder replaceTextWithItemView(String text){
        StringBuilder input1 = new StringBuilder();
        input1.append(text);
        input1.reverse();
        input1.replace(0, input1.indexOf(" "), "");
        input1.reverse();
        return input1;
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