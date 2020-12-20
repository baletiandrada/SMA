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
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class BooksRecommendedFragment extends Fragment {
    private Button seeAllRecommendedBooks_button;
    private Spinner genres_spinner;
    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>();
    private BooksRecommendedAdapter listExampleAdapterBooks;

    private GenresAdapter genresAdapter;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    List<BookReadData> user_read_books = new ArrayList<BookReadData>(), user_planned_books = new ArrayList<BookReadData>();  //lista contine genurile din cartile citite si din cele planificate
    List<BookReadData>  user_books = new ArrayList<BookReadData>();
    ArrayList<String> user_book_genres = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books_recommended, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        seeAllRecommendedBooks_button = root.findViewById(R.id.btn_seeAllRecommendedBooks);
        genres_spinner = root.findViewById(R.id.spinner_genres);

        getUserBooks();

        recyclerView = root.findViewById(R.id.rv_book_recommended_list);

        seeAllRecommendedBooks_button.setOnClickListener(new View.OnClickListener() {   //afisez lista, nu parcurg iar baza de date
            @Override
            public void onClick(View v) {
                if(!(currentUser ==null)){
                    getDataFromRecommendedDB();
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
                            if( ! genre_item.equals("Select genre") )
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
            genres_spinner.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }
    }

    public void getDataFromRecommendedDB(){
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

    public void getUserBooks(){
        if(!(currentUser.getUid() == null)){
            mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    getAllRecommendationsREAD(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
            mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    getAllRecommendationsPLAN(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getAllRecommendationsREAD(DataSnapshot dataSnapshot){
        getReadBookGenres(dataSnapshot);
        setUserBooks();
        getGenreValuesSpinner();
        getDataFromRecommendedDB();
    }

    public void getAllRecommendationsPLAN(DataSnapshot dataSnapshot){
        getPlannedBookGenres(dataSnapshot);
        setUserBooks();
        getGenreValuesSpinner();
        getDataFromRecommendedDB();
    }

    public void setUserBooks(){
        user_books.removeAll(user_books);
        user_books.addAll(user_read_books);
        user_books.addAll(user_planned_books);
    }

    public void getReadBookGenres(DataSnapshot dataSnapshot){
        user_read_books.removeAll(user_read_books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String author_name = String.valueOf(ds.child("author_name").getValue()).toLowerCase();
            String title = String.valueOf(ds.child("title").getValue()).toLowerCase();
            BookReadData currentBook = new BookReadData(author_name, title);
            user_read_books.add(currentBook);
        }
    }

    public void getPlannedBookGenres(DataSnapshot dataSnapshot){
        user_planned_books.removeAll(user_planned_books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String author_name = String.valueOf(ds.child("author_name").getValue()).toLowerCase();
            String title = String.valueOf(ds.child("title").getValue()).toLowerCase();
            BookReadData currentBook = new BookReadData(author_name, title);
            user_planned_books.add(currentBook);
        }
    }

    public void getGenreValuesSpinner(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null)){
                    user_book_genres.removeAll(user_book_genres);
                    user_book_genres.add("Select genre");
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String author_name = String.valueOf(ds.child("author_name").getValue()).toLowerCase();
                        String book_title = String.valueOf(ds.child("title").getValue()).toLowerCase();
                        BookReadData newBook = new BookReadData(author_name, book_title);
                        for(BookReadData bookInUserBooks : user_books){
                            if( (bookInUserBooks.getAuthor_name().contains(newBook.getAuthor_name())
                                    || newBook.getAuthor_name().contains(bookInUserBooks.getAuthor_name()))
                                    && (bookInUserBooks.getTitle().contains(newBook.getTitle())
                                    || newBook.getTitle().contains(bookInUserBooks.getTitle())) ){

                                if(!user_book_genres.contains(String.valueOf(ds.child("genre").getValue())))
                                    user_book_genres.add(String.valueOf(ds.child("genre").getValue()).toLowerCase());
                            }
                        }
                    }
                    if(user_book_genres!=null) {
                        genresAdapter = new GenresAdapter(getActivity(), user_book_genres);
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
            String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
            BookReadData newBook = new BookReadData(author_name, book_title);
            boolean listContainsGenreFromDB = false;

            for(String genre_item : user_book_genres)
                if(genre.contains(genre_item)){
                    listContainsGenreFromDB = true;
                    break;
                }

            boolean userBooksContainNewBook = false;
            for(BookReadData bookInUserBooks:user_books){
                if( (bookInUserBooks.getAuthor_name().contains(newBook.getAuthor_name().toLowerCase())
                        || newBook.getAuthor_name().toLowerCase().contains(bookInUserBooks.getAuthor_name()))
                        && (bookInUserBooks.getTitle().contains(newBook.getTitle().toLowerCase())
                        || newBook.getTitle().toLowerCase().contains(bookInUserBooks.getTitle())) ){

                    userBooksContainNewBook = true;
                    break;
                }
            }

            if( !userBooksContainNewBook && (user_book_genres.contains(genre) || listContainsGenreFromDB) )
            {
                newBook.setId(String.valueOf(ds.getKey()));
                newBook.setGenre(ds.child("genre").getValue().toString());
                books.add(newBook);
            }
        }
        if(!books.isEmpty())
            setRecyclerView();
        else
            Toast.makeText(getActivity(), "Sorry, there are no recommendations for you", Toast.LENGTH_SHORT).show();
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String genre_variable){
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String variable_lower_case = genre_variable.toLowerCase();
            if(String.valueOf(ds.child("genre").getValue()).toLowerCase().contains(variable_lower_case)){
                String author_name = String.valueOf(ds.child("author_name").getValue());
                String book_title = String.valueOf(ds.child("title").getValue());
                BookReadData newBook = new BookReadData(author_name, book_title);

                boolean userBooksContainNewBook = false;
                for(BookReadData bookInUserBooks:user_books){
                    if( (bookInUserBooks.getAuthor_name().contains(newBook.getAuthor_name().toLowerCase())
                            || newBook.getAuthor_name().toLowerCase().contains(bookInUserBooks.getAuthor_name()))
                            && (bookInUserBooks.getTitle().contains(newBook.getTitle().toLowerCase())
                            || newBook.getTitle().toLowerCase().contains(bookInUserBooks.getTitle())) ){

                        userBooksContainNewBook = true;
                        break;
                    }
                }

                if(!userBooksContainNewBook){
                    newBook.setId(String.valueOf(ds.getKey()));
                    newBook.setGenre(ds.child("genre").getValue().toString());
                    books.add(newBook);
                }
            }
        }
        if(!books.isEmpty())
            setRecyclerView();
        else
            Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
    }
}
