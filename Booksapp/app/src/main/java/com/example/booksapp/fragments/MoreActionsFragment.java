package com.example.booksapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.booksapp.AppConstants;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.dataModels.BookReadData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class MoreActionsFragment extends Fragment {
    private Button logout_button, delete_account_button, addBookRecomm_top, addBookRecomm_bottom, addBookRecomm_cancel;
    private EditText book_author_et, book_title_et, book_genre_et;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    ArrayList<BookReadData> books_recomm = new ArrayList<BookReadData>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_more_actions, container, false);

        mAuth = FirebaseAuth.getInstance();

        logout_button = root.findViewById(R.id.btn_logout_fragment);
        delete_account_button = root.findViewById(R.id.btn_delete_account);
        addBookRecomm_top = root.findViewById(R.id.btnTop_addBook_recomm);
        addBookRecomm_bottom = root.findViewById(R.id.btnBottom_addBook_recomm);
        addBookRecomm_cancel = root.findViewById(R.id.btnBottom_addBookRecomm_cancel);
        book_author_et = root.findViewById(R.id.et_authorName_recomm);
        book_title_et = root.findViewById(R.id.et_bookTitle_recomm);
        book_genre_et = root.findViewById(R.id.et_genre_recomm);
        setAddBookUIGone();

        currentUser = mAuth.getCurrentUser();
        if( ! currentUser.getEmail().equals("admin@gmail.com") )
            addBookRecomm_top.setVisibility(View.GONE);

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        delete_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        addBookRecomm_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookRecomm_top.setVisibility(View.GONE);
                addBookRecomm_bottom.setVisibility(View.VISIBLE);
                addBookRecomm_cancel.setVisibility(View.VISIBLE);
                book_author_et.setVisibility(View.VISIBLE);
                book_title_et.setVisibility(View.VISIBLE);
                book_genre_et.setVisibility(View.VISIBLE);
            }
        });

        addBookRecomm_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToRecommendedBooksTable();
                addBookRecomm_top.setVisibility(View.VISIBLE);
                setAddBookUIGone();
            }
        });

        addBookRecomm_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookRecomm_top.setVisibility(View.VISIBLE);
                setAddBookUIGone();
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

        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null)){
                    getDataFromRecommBooks();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setAddBookUIGone(){
        addBookRecomm_bottom.setVisibility(View.GONE);
        addBookRecomm_cancel.setVisibility(View.GONE);
        book_author_et.setVisibility(View.GONE);
        book_title_et.setVisibility(View.GONE);
        book_genre_et.setVisibility(View.GONE);
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
                    mBooksReadDatabase.child(uid).removeValue();
                    mBooksPlannedDatabase.child(uid).removeValue();

                    mAuth.signOut();
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

    public void addToRecommendedBooksTable(){
        if (book_author_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name for the author", Toast.LENGTH_SHORT).show();
            return;
        }
        if (book_title_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a book title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (book_genre_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a genre", Toast.LENGTH_SHORT).show();
            return;
        }

        if(bookNotExistsInRecommBooks(book_author_et.getText().toString(), book_title_et.getText().toString())){
            BookReadData newBook = new BookReadData(book_author_et.getText().toString(), book_title_et.getText().toString(), book_genre_et.getText().toString());
            String book_id = mBooksRecommendedDatabase.push().getKey();
            mBooksRecommendedDatabase.child(book_id).setValue(newBook);
            Toast.makeText(getActivity(), "Book added successfully", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();

        book_author_et.setText(null);
        book_title_et.setText(null);
        book_genre_et.setText(null);
        addBookRecomm_top.setVisibility(View.VISIBLE);
        setAddBookUIGone();
    }

    public void getDataFromRecommBooks(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_recomm.removeAll(books_recomm);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String year = String.valueOf(ds.child("read_year").getValue());
                    String month = String.valueOf(ds.child("read_month").getValue());
                    if(month==null)
                        month = "";
                    BookReadData newBook = new BookReadData(author_name, book_title, month, year);
                    newBook.setId(String.valueOf(ds.getKey()));
                    books_recomm.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean bookNotExistsInRecommBooks(String author_name, String title){
        for(BookReadData current_book : books_recomm){
            if(current_book.getAuthor_name().toLowerCase().equals(author_name.toLowerCase())
                    && current_book.getTitle().toLowerCase().equals(title.toLowerCase()))
                return false;
        }
        return true;
    }

}
