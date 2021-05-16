package com.example.booksapp.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.booksapp.AppConstants;
import com.example.booksapp.ChangePasswordActivity;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.helpers.FirebaseHelper;
import com.example.booksapp.helpers.UserStorageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class AccountSettingsFragment extends Fragment {
    private Button logout_button, delete_account_button;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ImageView arrow_down, arrow_up;

    FirebaseUser currentUser= mAuth.getCurrentUser();;

    private EditText username_et, age_et, email_et;
    private Button updateUserData_button, changePasswordTop_button;
    UserStorageHelper userData = UserStorageHelper.getInstance();

    List<String> favourite_books = new ArrayList<String>();


    Animation scaleUp, scaleDown;

    private FloatingActionButton fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_settings, container, false);
        initializeViews(root);

        scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        getDataFromStorageHelper();


        arrow_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrow_down.setVisibility(View.GONE);
                arrow_up.setVisibility(View.VISIBLE);
                changePasswordTop_button.setVisibility(View.VISIBLE);
                delete_account_button.setVisibility(View.VISIBLE);
                root.findViewById(R.id.layout_et_username).setVisibility(View.GONE);
                root.findViewById(R.id.layout_et_age).setVisibility(View.GONE);
                root.findViewById(R.id.layout_et_email).setVisibility(View.GONE);
                updateUserData_button.setVisibility(View.GONE);
            }
        });


        arrow_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrow_down.setVisibility(View.VISIBLE);
                arrow_up.setVisibility(View.GONE);
                changePasswordTop_button.setVisibility(View.GONE);
                delete_account_button.setVisibility(View.GONE);
                root.findViewById(R.id.layout_et_username).setVisibility(View.VISIBLE);
                root.findViewById(R.id.layout_et_age).setVisibility(View.VISIBLE);
                root.findViewById(R.id.layout_et_email).setVisibility(View.VISIBLE);
                updateUserData_button.setVisibility(View.VISIBLE);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to log out?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                final FirebaseUser currentUser = mAuth.getCurrentUser();

                                SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString(AppConstants.EMAIL, currentUser.getEmail());
                                editor.apply();

                                mAuth.signOut();
                                goToLoginActivity();
                                Toast.makeText( getActivity() , "Log out successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        updateUserData_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    updateUserData_button.startAnimation(scaleUp);
                    updateUserData();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    updateUserData_button.startAnimation(scaleDown);
                }
                return true;
            }
        });

        changePasswordTop_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    changePasswordTop_button.startAnimation(scaleUp);
                    goToChangePasswordActivity();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    changePasswordTop_button.startAnimation(scaleDown);
                }
                return true;
            }
        });

        delete_account_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    delete_account_button.startAnimation(scaleUp);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure you want to delete your account?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteAccount();
                                }
                            }).setNegativeButton("CANCEL", null);

                    AlertDialog alert = builder.create();
                    alert.show();

                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    delete_account_button.startAnimation(scaleDown);
                }
                return true;
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser == null) {
            delete_account_button.setVisibility(View.GONE);
            logout_button.setVisibility(View.GONE);
            goToLoginActivity();
            return;
        }
    }

    public void goToChangePasswordActivity(){
        Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
        startActivity(intent);
    }

    public void updateUserData(){

        if(username_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(age_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter an age", Toast.LENGTH_SHORT).show();
            return;
        }

        if(email_et.getText().toString().isEmpty()){
            Toast.makeText( getActivity(), "Please enter a email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!username_et.getText().toString().equals(userData.username) ||
                !age_et.getText().toString().equals(userData.age) ||
                !email_et.getText().toString().equals(userData.email)) {

            FirebaseUser user = mAuth.getCurrentUser();
            String new_email = email_et.getText().toString();

            assert user != null;
            if (!new_email.equals(user.getEmail())) {
                final Task<Void> voidTask = user.updateEmail(email_et.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                if(!voidTask.isSuccessful())
                    return;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username_et.getText().toString());
            map.put("age", age_et.getText().toString());
            map.put("email", email_et.getText().toString());
            map.put("password", userData.getPassword());

            String node_name = userData.idUserFirebase;
            FirebaseHelper.mUserDatabase.child(node_name).updateChildren(map);

            Toast.makeText(getActivity(), "Your profile updated successfully", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "Please enter new values in the above fields", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void getDataFromStorageHelper(){
        if(userData.username == null && userData.age == null && userData.email == null && userData.password == null)
        {
            mAuth.signOut();
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(AppConstants.EMAIL, "");
            editor.apply();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
        else{
            if(!userData.username.equals("") && !userData.age.equals("") && !userData.email.equals("") && !userData.password.equals(""))
            {
                username_et.setText(userData.getUsername());
                age_et.setText(userData.getAge());
            }
            email_et.setText(userData.getEmail());
        }
    }

    public void deleteAccount(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mBooksReadDatabase.child(uid).removeValue();
                    mBooksPlannedDatabase.child(uid).removeValue();

                    mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            favourite_books.clear();
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                favourite_books.add(ds.getKey());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    for(String book: favourite_books){
                        mQuotesDatabase.child(book).removeValue();
                    }

                    mFavouriteBooksDatabase.child(uid).removeValue();
                    mUserDatabase.child(uid).removeValue();

                    mAuth.signOut();

                    currentUser.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "User account deleted.", Toast.LENGTH_SHORT);
                                    }
                                }
                            });

                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(AppConstants.EMAIL, "");
                    editor.apply();
                    Toast.makeText( getActivity() , "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    goToLoginActivity();
                }
                else {
                    Toast.makeText( getActivity() , "Something went wrong at deleting your profile. Please login again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void initializeViews(View root){
        username_et = root.findViewById(R.id.et_username);
        age_et = root.findViewById(R.id.et_age);
        email_et = root.findViewById(R.id.et_email);
        updateUserData_button = root.findViewById(R.id.btn_updateUserData);

        changePasswordTop_button = root.findViewById(R.id.btn_changePasswordTop);
        changePasswordTop_button.setVisibility(View.GONE);
        delete_account_button = root.findViewById(R.id.btn_delete_account);
        delete_account_button.setVisibility(View.GONE);

        arrow_down = root.findViewById(R.id.iv_arrow_down_more);
        arrow_up = root.findViewById(R.id.iv_arrow_up_more);
        arrow_up.setVisibility(View.GONE);

        fab = root.findViewById(R.id.fab_settings);
    }

}
