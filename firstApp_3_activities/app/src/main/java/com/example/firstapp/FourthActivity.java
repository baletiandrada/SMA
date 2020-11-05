package com.example.firstapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FourthActivity extends AppCompatActivity {

    private EditText edit_name, edit_firstName;
    private Button add_item_first_button, add_item_second_button;
    private RecyclerView exampleListRv;
    private ListAdapter listExampleAdapter;
    private List<ListModel> exampleModelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);
        initializeViews();
        setRecyclerView();
        setOnclickListener();
        setOnClickListenersAdd();
    }

    void initializeViews(){
        edit_name = findViewById(R.id.et_name);
        edit_firstName = findViewById(R.id.et_firstName);
        add_item_first_button = findViewById(R.id.add_item_first_button);
        add_item_second_button = findViewById(R.id.add_item_second_button);
        exampleListRv = findViewById(R.id.rv_my_first_list);
        setAddingUIsGone();
    }

    private void setAddingUIsGone(){
        edit_name.setVisibility(View.GONE);
        edit_firstName.setVisibility(View.GONE);
        add_item_second_button.setVisibility(View.GONE);
    }

    private void setOnclickListener(){
        View view;
        add_item_first_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_item_first_button.setVisibility(View.GONE);
                edit_name.setVisibility(View.VISIBLE);
                edit_firstName.setVisibility(View.VISIBLE);
                add_item_second_button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setOnClickListenersAdd()
    {
        View view;
        add_item_second_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
    }

    private void addItem()
    {
        String name_entered = edit_name.getText().toString(), firstName_entered = edit_firstName.getText().toString();

        if(name_entered.isEmpty())
        {
            Toast toast = Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200);
            toast.show();
            return;
        }
        else if(firstName_entered.isEmpty()) {
                 Toast toast = Toast.makeText(this, "Please enter your first name", Toast.LENGTH_LONG);
                 toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200);
                 toast.show();
                 return;
              }
              else {
                      exampleModelList.add(new ListModel(name_entered, firstName_entered, 21));
                      setRecyclerView();
                      edit_name.setText(null);
                      edit_firstName.setText(null);
                      setAddingUIsGone();
                      add_item_first_button.setVisibility(View.VISIBLE);
              }
    }

    private void setRecyclerView() {
        listExampleAdapter = new ListAdapter(exampleModelList);
        exampleListRv.setLayoutManager(new LinearLayoutManager(this));
        exampleListRv.setAdapter(listExampleAdapter);
    }

}