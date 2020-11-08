package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomActivity extends AppCompatActivity {

    private TextView welcomeRoom;
    private TestDatabase testDatabase;
    private List<TestEntity> testEntityList = new ArrayList<>();;
    private EditText name;
    private EditText firstName;
    private Button addData, changeContent, deleteButton, seeDB;

    private RecyclerView exampleListRv;
    private RoomAdapter listExampleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initializeViews();
        testDatabase = TestDatabase.getInstance(this);

        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDatabase();
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

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        deleteAllFromDatabase();
                    }
                });

            }
        });

        seeDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        getFromDatabase();
                    }
                });

            }
        });
    }

    private void addToDatabase(){
        if(name.getText().toString().isEmpty())
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
            toast.show();
            return;
        }
        else if(firstName.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter a first name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
            toast.show();
            return;
        }
        else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    insertToDatabase(name.getText().toString(), firstName.getText().toString());
                }
            });

            name.setText(null);
            firstName.setText(null);
            name.setVisibility(View.GONE);
            firstName.setVisibility(View.GONE);
            addData.setVisibility(View.GONE);
            changeContent.setVisibility(View.VISIBLE);
        }
    }

    private void insertToDatabase(final String name, final String firstName)
    {
        class InsertValue extends AsyncTask<Void, Void, List<TestEntity>> {

            @Override
            protected List<TestEntity> doInBackground(Void... voids) {
                TestEntity testEntity = new TestEntity(name,firstName);
                testDatabase.testDAO().insertAll(testEntity);
                testEntityList = testDatabase.testDAO().getAll();
                return testEntityList;
            }

            @Override
            protected void onPostExecute(List<TestEntity> testEntityList) {
                super.onPostExecute(testEntityList);
                setRecyclerView();
            }
        }

        InsertValue insertTask = new InsertValue();
        insertTask.execute();
    }

    public void getFromDatabase() {
        class GetValue extends AsyncTask<Void, Void, List<TestEntity>> {

            @Override
            protected List<TestEntity> doInBackground(Void... voids) {
                //@SuppressLint("WrongThread") TestEntity testEntity = new TestEntity("Baleti", "Andrada");
                testEntityList = testDatabase.testDAO().getAll();
                return testEntityList;
            }
            @Override
            protected void onPostExecute(List<TestEntity> testEntityList) {
                super.onPostExecute(testEntityList);
                setRecyclerView();
            }

        }
        GetValue getTask = new GetValue();
        getTask.execute();
    }

    public void deleteAllFromDatabase() {
        class RemoveAllValues extends AsyncTask<Void, Void, List<TestEntity>> {

            @Override
            protected List<TestEntity> doInBackground(Void... voids) {
                testDatabase.testDAO().delete();
                testEntityList= Collections.emptyList();
                return testEntityList;
            }

            @Override
            protected void onPostExecute(List<TestEntity> testEntityList) {
                super.onPostExecute(testEntityList);
                setRecyclerView();
            }

        }
        RemoveAllValues deleteTask = new RemoveAllValues();
        deleteTask.execute();
    }

    private void setRecyclerView() {
        listExampleAdapter = new RoomAdapter(testEntityList);
        exampleListRv.setLayoutManager(new LinearLayoutManager(this));
        exampleListRv.setAdapter(listExampleAdapter);
    }

    public void initializeViews(){
        welcomeRoom = findViewById(R.id.tv_welcomeRoom);
        testDatabase = TestDatabase.getInstance(this);

        name = findViewById(R.id.et_name);
        firstName = findViewById(R.id.et_firstName);
        addData = findViewById(R.id.btn_addToDatabase);
        changeContent = findViewById(R.id.btn_open);
        deleteButton = findViewById(R.id.btn_delete);
        seeDB = findViewById(R.id.btn_seeDB);

        changeContent.setVisibility(View.GONE);

        exampleListRv = findViewById(R.id.rv_my_first_list);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                getFromDatabase();
            }
        });

    }
}