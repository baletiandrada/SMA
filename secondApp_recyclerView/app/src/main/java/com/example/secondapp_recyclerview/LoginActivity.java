package com.example.secondapp_recyclerview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private TextView login_tv;
    private EditText username_et, passwd_et;
    private CheckBox save_checkBox;
    private Button login_btn, register_btn, navBar_btn;
    boolean saveToSharedPref=false;

    private TestEntity foundTestEntity = null;
    private TestDatabase testDatabase;

    StorageHelper received_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();

        testDatabase = TestDatabase.getInstance(this);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username_et.getText().toString().isEmpty())
                {
                    Toast.makeText( getApplicationContext() , "Please enter your username", Toast.LENGTH_LONG).show();
                    return;
                }
                if(passwd_et.getText().toString().isEmpty()) {
                    Toast.makeText( getApplicationContext() , "Please enter your password", Toast.LENGTH_LONG).show();
                    return;
                }

                getFromDatabase(username_et.getText().toString(), passwd_et.getText().toString());

            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });

        navBar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNavBarActivity();
            }
        });
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        if(view.getId() == R.id.checkbox_save)
            if(checked){
                saveToSharedPref=true;
            }
    }

    public void getFromDatabase(String username, String password) {
        class GetValue extends AsyncTask<Void, Void, TestEntity> {

            @Override
            protected TestEntity doInBackground(Void... voids) {
                //@SuppressLint("WrongThread") TestEntity testEntity = new TestEntity("Baleti", "Andrada");
                foundTestEntity = testDatabase.testDAO().findByUsername(username, password);
                return foundTestEntity;
            }

            @Override
            protected void onPostExecute(TestEntity testEntity) {
                super.onPostExecute(testEntity);
                if(foundTestEntity == null)
                {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Your credentials is wrong.", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
                }
                else{
                    received_data=StorageHelper.getInstance();
                    received_data.setId(foundTestEntity.getId());
                    received_data.setEmail(foundTestEntity.getEmail());
                    received_data.setAge(foundTestEntity.getAge());
                    received_data.setUsername(foundTestEntity.getUsername());
                    received_data.setPassword(foundTestEntity.getPassword());

                    if(saveToSharedPref==true)
                        writeToSharedPref();

                    goToNavBarActivity();
                    Toast.makeText(getApplicationContext(), "Welcome " + username + '!', Toast.LENGTH_LONG).show();
                }
            }

        }
        GetValue getTask = new GetValue();
        getTask.execute();
    }

    private void goToNavBarActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    private void goToRegisterActivity(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void readSharedPref(){
        SharedPreferences prefs = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(AppConstants.KEY_username, "No name defined");
        String password = prefs.getString(AppConstants.KEY_password, "No name defined");
        username_et.setText(username);
        passwd_et.setText(password);
    }

    public void writeToSharedPref(){
        SharedPreferences.Editor editor = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(AppConstants.KEY_username, foundTestEntity.getUsername());
        editor.putString(AppConstants.KEY_password, foundTestEntity.getPassword());
        editor.apply();
    }

    private void initializeViews() {
        login_tv = findViewById(R.id.tv_login_text);
        username_et = findViewById(R.id.et_username);
        passwd_et = findViewById(R.id.et_passwd);
        save_checkBox = findViewById(R.id.checkbox_save);
        login_btn = findViewById(R.id.login_button);
        register_btn = findViewById(R.id.register_button);
        navBar_btn = findViewById(R.id.navBar_button);

        readSharedPref();
    }
}