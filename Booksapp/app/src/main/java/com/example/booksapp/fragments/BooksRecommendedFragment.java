package com.example.booksapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.adapters.BookReadDataAdapter;
import com.example.booksapp.adapters.BooksRecommendedAdapter;
import com.example.booksapp.adapters.GenresAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class BooksRecommendedFragment extends Fragment {
    private TextView chooseGenre_tv;
    private Button seeAllRecommendedBooks_button;
    private Spinner genres_spinner;
    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>();
    private BooksRecommendedAdapter listExampleAdapterBooks;

    private ArrayList<String> genres = new ArrayList<String>();
    private GenresAdapter genresAdapter;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books_recommended, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        seeAllRecommendedBooks_button = root.findViewById(R.id.btn_seeAllRecommendedBooks);
        chooseGenre_tv = root.findViewById(R.id.tv_choose_genre);
        genres_spinner = root.findViewById(R.id.spinner_genres);
        getGenreValuesSpinner();

        recyclerView = root.findViewById(R.id.rv_book_recommended_list);

        seeAllRecommendedBooks_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(currentUser ==null)){
                    mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            getData(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        genres_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String genre_item = (String) parent.getItemAtPosition(position);
                mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!(currentUser.getUid() == null)){
                            getDataByVariable(dataSnapshot, genre_item);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser == null) {
            seeAllRecommendedBooks_button.setVisibility(View.GONE);
            chooseGenre_tv.setVisibility(View.GONE);
            genres_spinner.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }
        else {
            mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(currentUser.getUid() == null))
                        getData(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getGenreValuesSpinner(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null)){
                    genres.removeAll(genres);
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        if( ! genres.contains(ds.child("genre").getValue().toString()) )
                            genres.add(ds.child("genre").getValue().toString());
                    }
                    if(genres!=null) {
                        genresAdapter = new GenresAdapter(getActivity(), genres);
                        genres_spinner.setAdapter(genresAdapter);
                    }
                    else
                        Toast.makeText(getActivity(), "There are no genres", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void setRecyclerView(){
        listExampleAdapterBooks = new BooksRecommendedAdapter(books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String author_name = String.valueOf(ds.child("author_name").getValue());
            String book_title = String.valueOf(ds.child("title").getValue());
            BookReadData newBook = new BookReadData(author_name, book_title);
            newBook.setId(String.valueOf(ds.getKey()));
            newBook.setGenre(ds.child("genre").getValue().toString());
            books.add(newBook);
        }
        if(!books.isEmpty())
            setRecyclerView();
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String genre_variable){
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String variable_lower_case = genre_variable.toLowerCase();
            if(String.valueOf(ds.child("genre").getValue()).toLowerCase().contains(variable_lower_case)){
                String author_name = String.valueOf(ds.child("author_name").getValue());
                String book_title = String.valueOf(ds.child("title").getValue());
                BookReadData newBook = new BookReadData(author_name, book_title);
                newBook.setId(String.valueOf(ds.getKey()));
                newBook.setGenre(ds.child("genre").getValue().toString());
                books.add(newBook);
            }
        }
        if(!books.isEmpty())
            setRecyclerView();
        else
            Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
    }

}
