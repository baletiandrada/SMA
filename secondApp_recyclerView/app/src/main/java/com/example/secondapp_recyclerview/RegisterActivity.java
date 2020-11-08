package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    private TextView register_tv;
    private EditText email_et, age_et, username_et, passwd_et;
    private Button register_btn;

    private TestDatabase testDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeViews();

        testDatabase = TestDatabase.getInstance(this);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(email_et.getText().toString().isEmpty())
                {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter your email", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(age_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter your age", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(username_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter a username", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }
                if(passwd_et.getText().toString().isEmpty()) {
                    Toast toast = Toast.makeText( getApplicationContext() , "Please enter a password", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -220);
                    toast.show();
                    return;
                }

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        insertToDatabase(email_et.getText().toString(), age_et.getText().toString(), username_et.getText().toString(), passwd_et.getText().toString());
                    }
                });

            }
        });
    }

    private void goToLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void insertToDatabase(final String email, final String age, final String username, final String password) {
        class InsertValue extends AsyncTask<Void, Void, TestEntity> {

            @Override
            protected TestEntity doInBackground(Void... voids) {
                TestEntity testEntity = new TestEntity(email, age, username, password);
                testDatabase.testDAO().insertAll(testEntity);
                return testEntity;
            }

            @Override
            protected void onPostExecute(TestEntity testEntity) {
                super.onPostExecute(testEntity);
                goToLoginActivity();
            }
        }
        InsertValue insertTask = new InsertValue();
        insertTask.execute();
    }

    private void initializeViews() {
        register_tv = findViewById(R.id.tv_register_text);
        email_et = findViewById(R.id.et_email);
        age_et = findViewById(R.id.et_age);
        username_et = findViewById(R.id.et_username);
        passwd_et = findViewById(R.id.et_passwd);
        register_btn = findViewById(R.id.register_button);
    }
}