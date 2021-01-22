package com.example.booksapp.fragments;

import android.annotation.SuppressLint;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AppConstants;
import com.example.booksapp.ChangePasswordActivity;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.adapters.BookReadDataAdapter;
import com.example.booksapp.adapters.FavouriteBooksAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.FirebaseHelper;
import com.example.booksapp.helpers.StorageHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;

public class EditUserDataFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>(), books_recommended= new ArrayList<BookReadData>();
    FavouriteBooksAdapter listExampleAdapterBooks;
    private SearchView searchView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        initializeViews(root);

        getDataFromRecommendedBooks();
        mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null))
                    getData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!(currentUser ==null)){
                    mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            getDataByVariable(dataSnapshot, query);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!(currentUser ==null)){
                    mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            getDataByVariable(dataSnapshot, newText);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return false;
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser == null) {
            goToLoginActivity();
        }
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String variable){
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String variable_lower_case = variable.toLowerCase();
            if(String.valueOf(ds.child("author_name").getValue()).toLowerCase().contains(variable_lower_case)
                    || String.valueOf(ds.child("title").getValue()).toLowerCase().contains(variable_lower_case)
                    || String.valueOf(ds.child("read_year").getValue()).equals(variable)){
                String author_name = String.valueOf(ds.child("author_name").getValue());
                String book_title = String.valueOf(ds.child("title").getValue());
                String read_year = String.valueOf(ds.child("read_year").getValue());
                String month = String.valueOf(ds.child("read_month").getValue());
                if(month == null)
                    month = "";
                BookReadData newBook = new BookReadData(author_name, book_title, month, read_year);
                String uri;
                uri = String.valueOf(ds.child("uri").getValue());

                if(uri.equals("null") || uri==null || uri.isEmpty())
                    uri = getUriFromRecommended(author_name, book_title);

                newBook.setUri(uri);
                newBook.setId(String.valueOf(ds.getKey()));
                books.add(newBook);
            }
        }
        setRecyclerView();
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String author_name = String.valueOf(ds.child("author_name").getValue());
            String book_title = String.valueOf(ds.child("title").getValue());
            String year = String.valueOf(ds.child("read_year").getValue());
            String month = String.valueOf(ds.child("read_month").getValue());
            if(month == null)
                month = "";
            BookReadData newBook = new BookReadData(author_name, book_title, month, year);
            String uri;
            uri = String.valueOf(ds.child("uri").getValue());

            if(uri.equals("null") || uri==null || uri.isEmpty()){
                uri = getUriFromRecommended(author_name, book_title);
            }

            newBook.setUri(uri);
            newBook.setId(String.valueOf(ds.getKey()));
            books.add(newBook);
        }
        //if(!books.isEmpty())
            setRecyclerView();
    }

    public void setRecyclerView(){
        listExampleAdapterBooks = new FavouriteBooksAdapter(books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
    }

    private String getUriFromRecommended(String author, String title) {
        String uri = null;
        for(BookReadData book : books_recommended){
            if( (book.getAuthor_name().contains(author)||author.contains(book.getAuthor_name()))
                    && (book.getTitle().contains(title) || title.contains(book.getTitle())) )
                uri = book.getUri();
        }
        return uri;
    }

    public void getDataFromRecommendedBooks(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_recommended.removeAll(books_recommended);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String uri = String.valueOf(ds.child("uri").getValue());
                    BookReadData newBook = new BookReadData(author_name,book_title);
                    newBook.setUri(uri);
                    newBook.setId(String.valueOf(ds.getKey()));
                    books_recommended.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }


    public void initializeViews(View root){
        searchView = root.findViewById(R.id.searchView_fav);
        recyclerView = root.findViewById(R.id.rv_fav_book_list);
    }
}
