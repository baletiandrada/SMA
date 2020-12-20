package com.example.booksapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AppConstants;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.adapters.BookReadDataAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class BooksMainViewFragment extends Fragment {

    private Button addBook_buttonTop, addBook_buttonBottom, cancel_addBook_buttonBottom, seeAllBooks_button;
    private EditText authorName_et, bookTitle_et, month_et, year_et, searchForVariable_et;
    private ImageView searchForVariable_iv;

    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>();
    BookReadDataAdapter listExampleAdapterBooks;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ArrayList<BookReadData> books_planned = new ArrayList<BookReadData>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        addBook_buttonTop = root.findViewById(R.id.btnTop_addBook);

        addBook_buttonBottom = root.findViewById(R.id.btnBottom_addBook);
        cancel_addBook_buttonBottom = root.findViewById(R.id.btnBottom_cancel);
        authorName_et = root.findViewById(R.id.et_authorName);
        bookTitle_et = root.findViewById(R.id.et_bookTitle);
        month_et = root.findViewById(R.id.et_readingMonth);
        year_et = root.findViewById(R.id.et_readingYear);
        recyclerView = root.findViewById(R.id.rv_book_list);
        seeAllBooks_button = root.findViewById(R.id.btn_seeAllBooks);
        searchForVariable_et = root.findViewById(R.id.et_searchForVariable);
        searchForVariable_iv = root.findViewById(R.id.iv_search_icon);

        setViewsGone();

        addBook_buttonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook_buttonTop.setVisibility(View.GONE);
                addBook_buttonBottom.setVisibility(View.VISIBLE);
                cancel_addBook_buttonBottom.setVisibility(View.VISIBLE);
                authorName_et.setVisibility(View.VISIBLE);
                bookTitle_et.setVisibility(View.VISIBLE);
                month_et.setVisibility(View.VISIBLE);
                year_et.setVisibility(View.VISIBLE);
            }
        });

        addBook_buttonBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookToFirebase();
                setRecyclerView();
            }
        });

        cancel_addBook_buttonBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewsGone();
                addBook_buttonTop.setVisibility(View.VISIBLE);
                authorName_et.setText(null);
                bookTitle_et.setText(null);
                month_et.setText(null);
                year_et.setText(null);
            }
        });

        seeAllBooks_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(currentUser ==null)){
                    mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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

        searchForVariable_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(currentUser ==null)){
                    if (searchForVariable_et.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Please enter an author/title/year", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                getDataByVariable(dataSnapshot, searchForVariable_et.getText().toString());
                                searchForVariable_et.setText(null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser == null) {
            addBook_buttonTop.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }
        else {
            mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(currentUser.getUid() == null)){
                        getData(dataSnapshot);
                        getDataFromPlannedBooks();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void setRecyclerView(){
        listExampleAdapterBooks = new BookReadDataAdapter(books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
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
            newBook.setId(String.valueOf(ds.getKey()));
            books.add(newBook);
        }
        if(!books.isEmpty())
            setRecyclerView();
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
                newBook.setId(String.valueOf(ds.getKey()));
                books.add(newBook);
            }
        }
        if(!books.isEmpty())
            setRecyclerView();
        else
            if(!searchForVariable_et.getText().toString().isEmpty())
                Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
    }

    public void setViewsGone(){
        addBook_buttonBottom.setVisibility(View.GONE);
        cancel_addBook_buttonBottom.setVisibility(View.GONE);
        authorName_et.setVisibility(View.GONE);
        bookTitle_et.setVisibility(View.GONE);
        month_et.setVisibility(View.GONE);
        year_et.setVisibility(View.GONE);
    }

    public void addBookToFirebase() {
        if (authorName_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name for the author", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bookTitle_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a book title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (year_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a year", Toast.LENGTH_SHORT).show();
            return;
        }

        String month;
        if(month_et.getText().toString().isEmpty())
            month = "";
        else {
            month = month_et.getText().toString();
            switch (month){
                case "ianuarie": {
                    month = "ian";
                    break;
                }
                case "februarie": {
                    month = "feb";
                    break;
                }
                case "martie": break;
                case "aprilie": break;
                case "mai": break;
                case "iunie": break;
                case "iulie": break;
                case "august": break;
                case "septembrie": {
                    month = "sept";
                    break;
                }
                case "octombrie": {
                    month = "oct";
                    break;
                }
                case "noiembrie": {
                    month = "nov";
                    break;
                }
                case "decembrie": {
                    month = "dec";
                    break;
                }
                default:{
                    Toast.makeText(getActivity(), "Please enter a valid month (not a number)", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }

        if(bookNotExists(authorName_et.getText().toString(), bookTitle_et.getText().toString())
                && bookNotExistsInPlannedBooks(authorName_et.getText().toString(), bookTitle_et.getText().toString())){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            BookReadData newBook = new BookReadData(authorName_et.getText().toString(), bookTitle_et.getText().toString(), month, year_et.getText().toString());
            String book_id = mBooksReadDatabase.child(currentUser.getUid()).push().getKey();
            mBooksReadDatabase.child(currentUser.getUid()).child(book_id).setValue(newBook);
            Toast.makeText(getActivity(), "Book added successfully", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getActivity(), "The book already exists (you read it or you planned it)", Toast.LENGTH_LONG).show();

        authorName_et.setText(null);
        bookTitle_et.setText(null);
        month_et.setText(null);
        year_et.setText(null);
        addBook_buttonTop.setVisibility(View.VISIBLE);
        setViewsGone();
    }

    public boolean bookNotExists(String author_name, String title){
        for(BookReadData current_book : books){
            if(current_book.getAuthor_name().toLowerCase().equals(author_name.toLowerCase())
                    && current_book.getTitle().toLowerCase().equals(title.toLowerCase()))
                return false;
        }
        return true;
    }

    public void getDataFromPlannedBooks(){
        mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_planned.removeAll(books_planned);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String year = String.valueOf(ds.child("read_year").getValue());
                    String month = String.valueOf(ds.child("read_month").getValue());
                    if(month==null)
                        month = "";
                    BookReadData newBook = new BookReadData(author_name, book_title, month, year);
                    newBook.setId(String.valueOf(ds.getKey()));
                    books_planned.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean bookNotExistsInPlannedBooks(String author_name, String title){
        for(BookReadData current_book : books_planned){
            if(current_book.getAuthor_name().toLowerCase().equals(author_name.toLowerCase())
                    && current_book.getTitle().toLowerCase().equals(title.toLowerCase()))
                return false;
        }
        return true;
    }

}
