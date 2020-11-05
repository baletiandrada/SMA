package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FilesActivity extends AppCompatActivity {

    private TextView fileContent;
    private TextView showText;
    private EditText name;
    private EditText firstName;
    private Button addData;
    private Button changeContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        initializeViews();

        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIntoFile();
                name.setVisibility(View.GONE);
                firstName.setVisibility(View.GONE);
                addData.setVisibility(View.GONE);
                changeContent.setVisibility(View.VISIBLE);
            }
        });

        changeContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContent.setVisibility(View.GONE);
                name.setVisibility(View.VISIBLE);
                firstName.setVisibility(View.VISIBLE);
                addData.setVisibility(View.VISIBLE);
            }
        });

    }

    public void addIntoFile(){
        if(name.getText().toString().isEmpty())
        {
            Toast toast = Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
            toast.show();
            return;
        }
        else if(firstName.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(this, "Please enter a first name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
            toast.show();
            return;
        }
        else {
            fileContent.setText("File content:");
            Gson gson = new Gson();
            ListModel model = new ListModel(name.getText().toString(), firstName.getText().toString());
            String data=gson.toJson(model);
            writeToFile(data);
            name.setText(null);
            firstName.setText(null);
            readFromFile();
        }
    }

    public void readFromFile()
    {
        String stringFromFile=" ";
        try {
            InputStream inputStream = this.openFileInput("myTextFile.txt");
            if(inputStream != null){
                InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString=" ";
                StringBuilder stringBuilder = new StringBuilder();
                while( (receiveString = bufferedReader.readLine()) != null ){
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                stringFromFile = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("declaration activity", "File not found" + e.toString());
        } catch (IOException e) {
            Log.e("declaration activity", "Can not read file" + e.toString());
        }
        Gson gson = new Gson();
        ListModel model = gson.fromJson(stringFromFile, ListModel.class);
        showText.setText(model.getName() + " " + model.getFirstName());
    }

    public void writeToFile(String data)
    {
        try {

            FileOutputStream fileout=openFileOutput("myTextFile.txt", MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileout);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            //startActivity(new Intent(FilesActivity.this, MainActivity.class));
            //Toast.makeText(this, "Datele au fost salvate cu succes", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(this, "Eroare la salvarea declaratiei", Toast.LENGTH_SHORT).show();
            Log.e("Exception", "File write failed" + e.toString());
        }
    }

    public void initializeViews()
    {
        fileContent = findViewById(R.id.tv_fileContent);
        showText = findViewById(R.id.tv_files_text);
        name = findViewById(R.id.et_name);
        firstName = findViewById(R.id.et_firstName);
        addData = findViewById(R.id.btn_addStringToFile_button);
        changeContent = findViewById(R.id.btn_changeContent);

        changeContent.setVisibility(View.GONE);
    }
}