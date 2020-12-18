package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.booksapp.dataModels.UserDetailsModel;
import com.example.booksapp.helpers.StorageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email_login, password_login, username, age, email_register, password_register, confirm_password_register;
    private Button login_button, register_open_button, register_button, addImage_button;
    StorageHelper userData = StorageHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();

        addImage_button = findViewById(R.id.btn_addImage);

        mAuth = FirebaseAuth.getInstance();

        register_open_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterUI();
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerMethod();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginMethod();
            }
        });

        addImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToImageActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            SharedPreferences prefs = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE);
            String mail = prefs.getString(AppConstants.EMAIL, "");
            email_login.setText(mail);
        }
        else{
            setGone2();
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user != null){
                        getData(dataSnapshot, user);
                        goToUserActivity();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void goToImageActivity(){
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
        startActivity(intent);
    }

    public void openRegisterUI(){
        email_login.setText(null);
        password_login.setText(null);
        setGone2();
        username.setVisibility(View.VISIBLE);
        age.setVisibility(View.VISIBLE);
        email_register.setVisibility(View.VISIBLE);
        password_register.setVisibility(View.VISIBLE);
        confirm_password_register.setVisibility(View.VISIBLE);
        register_button.setVisibility(View.VISIBLE);
    }

    public void registerMethod(){

        if(username.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }
        String usernameAux = username.getText().toString();

        if(age.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter an age", Toast.LENGTH_SHORT).show();
            return;
        }
        String ageAux = age.getText().toString();

        if(email_register.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a email", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailAux = email_register.getText().toString();

        if(password_register.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }
        String passwordAux = password_register.getText().toString();

        if(confirm_password_register.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }
        String confirmPasswordAux = confirm_password_register.getText().toString();

        if(!passwordAux.equals(confirmPasswordAux)){
            Toast.makeText( getApplicationContext() , "Your password do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email_register.getText().toString(), password_register.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user == null)
                                return;

                            UserDetailsModel userModel = new UserDetailsModel(usernameAux, ageAux, emailAux, passwordAux);
                            mUserDatabase.child(user.getUid()).setValue(userModel);

                            Toast.makeText(getApplicationContext(), "Sign up with succes.", Toast.LENGTH_SHORT).show();

                            username.setText(null);
                            age.setText(null);
                            email_register.setText(null);
                            password_register.setText(null);
                            confirm_password_register.setText(null);
                            setGone();

                            email_login.setVisibility(View.VISIBLE);
                            password_login.setVisibility(View.VISIBLE);
                            login_button.setVisibility(View.VISIBLE);
                            register_open_button.setVisibility(View.VISIBLE);
                            addImage_button.setVisibility(View.VISIBLE);

                            SharedPreferences.Editor editor = getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString(AppConstants.EMAIL, user.getEmail());
                            editor.apply();

                            email_login.setText(user.getEmail());

                            mAuth.signOut();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginMethod(){
        if(email_login.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password_login.getText().toString().isEmpty()){
            Toast.makeText( getApplicationContext() , "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email_login.getText().toString(), password_login.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            mUserDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if(user != null){
                                        getData(dataSnapshot, user);
                                        goToUserActivity();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void goToUserActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    private void getData(DataSnapshot dataSnapshot, FirebaseUser user) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            if(ds.getKey().equals(user.getUid()))
            {
                userData.setIdUserFirebase(String.valueOf(ds.getKey()));
                userData.setUsername(String.valueOf(ds.child("username").getValue()));
                userData.setAge(String.valueOf(ds.child("age").getValue()));
                userData.setEmail(String.valueOf(ds.child("email").getValue()));
                userData.setPassword(String.valueOf(ds.child("password").getValue()));
                break;
            }
        }
    }

    public void setGone2(){
        email_login.setVisibility(View.GONE);
        password_login.setVisibility(View.GONE);
        login_button.setVisibility(View.GONE);
        register_open_button.setVisibility(View.GONE);
        addImage_button.setVisibility(View.GONE);
    }

    public void setGone(){
        username.setVisibility(View.GONE);
        age.setVisibility(View.GONE);
        email_register.setVisibility(View.GONE);
        password_register.setVisibility(View.GONE);
        confirm_password_register.setVisibility(View.GONE);
        register_button.setVisibility(View.GONE);
    }

    public void initializeViews(){
        email_login = findViewById(R.id.et_login_email);
        password_login = findViewById(R.id.et_login_passwd);
        username = findViewById(R.id.et_firebase_username);
        age = findViewById(R.id.et_firebase_age);
        email_register = findViewById(R.id.et_register_email);
        password_register = findViewById(R.id.et_register_passwd);
        confirm_password_register = findViewById(R.id.et_confirm_register_passwd);

        login_button = findViewById(R.id.login_firebase_button);
        register_open_button = findViewById(R.id.register_open_button);
        register_button = findViewById(R.id.register_firebase_button);

        setGone();
    }

}