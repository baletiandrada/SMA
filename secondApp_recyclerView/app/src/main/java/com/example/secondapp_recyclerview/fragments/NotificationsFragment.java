package com.example.secondapp_recyclerview.fragments;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondapp_recyclerview.AppConstants;
import com.example.secondapp_recyclerview.FirebaseActivity;
import com.example.secondapp_recyclerview.NotificationAdapter;
import com.example.secondapp_recyclerview.NotificationModel;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.RoomAdapter;
import com.example.secondapp_recyclerview.TestEntity;
import com.example.secondapp_recyclerview.ui.notifications.NotificationsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;
import static com.example.secondapp_recyclerview.FirebaseHelper.mBooksReadDatabase;
import static com.example.secondapp_recyclerview.FirebaseHelper.mUserDatabase;

public class NotificationsFragment extends Fragment {

    private Button logout_button, delete_account_button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logout, container, false);

        logout_button = root.findViewById(R.id.btn_logout_fragment);
        delete_account_button = root.findViewById(R.id.btn_delete_account);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                final FirebaseUser currentUser = mAuth.getCurrentUser();

                SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(AppConstants.EMAIL, currentUser.getEmail());
                editor.apply();

                mAuth.signOut();
                goToLoginActivity();
                Toast.makeText( getActivity() , "Log out successfully", Toast.LENGTH_LONG).show();
            }
        });

        delete_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        return root;
    }

    public void deleteAccount(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mUserDatabase.child(uid).removeValue();
                    mAuth.signOut();
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(AppConstants.MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(AppConstants.EMAIL, "");
                    editor.apply();
                    Toast.makeText( getActivity() , "Account deleted successfully", Toast.LENGTH_LONG).show();
                    goToLoginActivity();
                }
                else {
                    Toast.makeText( getActivity() , "Something went wrong at deleting your profile", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), FirebaseActivity.class);
        startActivity(intent);
    }
}