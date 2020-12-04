package com.example.secondapp_recyclerview.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.secondapp_recyclerview.BookReadData;
import com.example.secondapp_recyclerview.BookReadDataAdapter;
import com.example.secondapp_recyclerview.ImageActivity;
import com.example.secondapp_recyclerview.R;
import com.example.secondapp_recyclerview.StorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.secondapp_recyclerview.FirebaseHelper.mBooksReadDatabase;

public class HomeFragment extends Fragment {

    private Button addBook_buttonTop, addBook_buttonBottom, cancel_addBook_buttonBottom;
    private EditText authorName_et, bookTitle_et, year_et;

    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>();
    BookReadDataAdapter listExampleAdapterBooks;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books, container, false);

        addBook_buttonTop = root.findViewById(R.id.btnTop_addBook);

        addBook_buttonBottom = root.findViewById(R.id.btnBottom_addBook);
        cancel_addBook_buttonBottom = root.findViewById(R.id.btnBottom_cancel);
        authorName_et = root.findViewById(R.id.et_authorName);
        bookTitle_et = root.findViewById(R.id.et_bookTitle);
        year_et = root.findViewById(R.id.et_readingYear);
        recyclerView = root.findViewById(R.id.rv_book_list);
        setViewsGone();

        addBook_buttonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook_buttonTop.setVisibility(View.GONE);
                addBook_buttonBottom.setVisibility(View.VISIBLE);
                cancel_addBook_buttonBottom.setVisibility(View.VISIBLE);
                authorName_et.setVisibility(View.VISIBLE);
                bookTitle_et.setVisibility(View.VISIBLE);
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
                year_et.setText(null);
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBooksReadDatabase.addValueEventListener(new ValueEventListener() {
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

    public void setRecyclerView(){
        listExampleAdapterBooks = new BookReadDataAdapter(books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            //Toast.makeText(getApplicationContext(), String.valueOf(ds.child("author_name").getValue()), Toast.LENGTH_LONG).show();
            String author_name = String.valueOf(ds.child("author_name").getValue());
            String book_title = String.valueOf(ds.child("title").getValue());
            String year = String.valueOf(ds.child("read_year").getValue());
            BookReadData newBook = new BookReadData(author_name, book_title, year);
            newBook.setId(String.valueOf(ds.getKey()));
            books.add(newBook);
        }
        if(books.isEmpty())
            Toast.makeText(getActivity(), "There are no books added by you", Toast.LENGTH_LONG).show();
        else setRecyclerView();
    }

    public void setViewsGone(){
        addBook_buttonBottom.setVisibility(View.GONE);
        cancel_addBook_buttonBottom.setVisibility(View.GONE);
        authorName_et.setVisibility(View.GONE);
        bookTitle_et.setVisibility(View.GONE);
        year_et.setVisibility(View.GONE);
    }

    public void addBookToFirebase() {
        if (authorName_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name for the author", Toast.LENGTH_LONG).show();
            return;
        }
        if (bookTitle_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a book title", Toast.LENGTH_LONG).show();
            return;
        }
        if (year_et.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a year", Toast.LENGTH_LONG).show();
            return;
        }

        BookReadData newBook = new BookReadData(authorName_et.getText().toString(), bookTitle_et.getText().toString(), year_et.getText().toString());

        String book_id = mBooksReadDatabase.push().getKey();
        mBooksReadDatabase.child(book_id).setValue(newBook);

        Toast.makeText(getActivity(), "Book added successfully", Toast.LENGTH_LONG).show();

        authorName_et.setText(null);
        bookTitle_et.setText(null);
        year_et.setText(null);
        addBook_buttonTop.setVisibility(View.VISIBLE);
        setViewsGone();
    }

}