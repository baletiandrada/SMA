package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.booksapp.helpers.FirebaseHelper;
import com.example.booksapp.helpers.UserStorageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText old_password_et, new_password_et, confirm_new_password_et;
    private Button changePasswordBottom_button, cancelChangePassword_button;

    UserStorageHelper userData = UserStorageHelper.getInstance();

    Animation scaleUp, scaleDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        changePasswordBottom_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    changePasswordBottom_button.startAnimation(scaleUp);
                    changePassword();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    changePasswordBottom_button.startAnimation(scaleDown);
                }
                return true;
            }
        });
        /*changePasswordBottom_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });*/

        cancelChangePassword_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    cancelChangePassword_button.startAnimation(scaleUp);
                    goToBottomNavigationActivity();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    cancelChangePassword_button.startAnimation(scaleDown);
                }
                return true;
            }
        });
        /*cancelChangePassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPasswordEtGone();
                changePasswordTop_button.setVisibility(View.VISIBLE);
            }
        });*/
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLoginActivity();
        }
    }


    public void goToLoginActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToBottomNavigationActivity(){
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
    }

    public void changePassword(){
        if(old_password_et.getText().toString().isEmpty()){
            Toast.makeText( this, "Please enter your old password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(new_password_et.getText().toString().isEmpty()){
            Toast.makeText( this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(confirm_new_password_et.getText().toString().isEmpty()){
            Toast.makeText( this, "Please confirm the new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if(userData.password.equals(new_password_et.getText().toString())){
            Toast.makeText(getApplicationContext(), "Please enter a password you didn't use before", Toast.LENGTH_LONG).show();
            return;
        }

        if(old_password_et.getText().toString().equals(userData.password)){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (new_password_et.getText().toString().equals(confirm_new_password_et.getText().toString())){
                if(new_password_et.getText().toString().length()>=6){
                    if(user==null){
                        Toast.makeText(getApplicationContext(), "Please login again", Toast.LENGTH_SHORT).show();
                        goToLoginActivity();
                    }
                    assert user != null;
                    user.updatePassword(new_password_et.getText().toString());
                    FirebaseHelper.mUserDatabase.child(user.getUid()).child("password").setValue(new_password_et.getText().toString());
                    Toast.makeText(getApplicationContext(), "Your password updated successfully", Toast.LENGTH_SHORT).show();
                    goToBottomNavigationActivity();
                }
                else{
                    Toast.makeText(this, "Your password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            else{
                Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            Toast.makeText(this, "You entered the wrong old password", Toast.LENGTH_SHORT).show();
            return;
        }
        old_password_et.setText(null);
        new_password_et.setText(null);
        confirm_new_password_et.setText(null);
    }


    public void initializeViews(){
        old_password_et = findViewById(R.id.et_old_password);
        new_password_et = findViewById(R.id.et_new_password);
        confirm_new_password_et = findViewById(R.id.et_confirm_new_password);
        changePasswordBottom_button = findViewById(R.id.btn_changePasswordBottom);
        cancelChangePassword_button = findViewById(R.id.btn_cancelChangePasswordBottom);

    }
}