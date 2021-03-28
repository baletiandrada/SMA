package com.example.booksapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksapp.AddBookActivity;
import com.example.booksapp.AppConstants;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.adapters.BookReadDataAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Color.*;
import static com.example.booksapp.AppConstants.MY_PREFS_NAME;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class BooksMainViewFragment<_> extends Fragment {

    private TextView seeAllBooks;
    private EditText searchForVariable_et;
    private ImageView searchForVariable_iv;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>();
    BookReadDataAdapter listExampleAdapterBooks;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    ArrayList<BookReadData> books_planned = new ArrayList<BookReadData>(), books_recommended= new ArrayList<BookReadData>();
    List<String> favourite_books = new ArrayList<String>();

    Animation scaleUp, scaleDown;

    SearchView searchView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books, container, false);
        /*if (currentUser == null) {
            addBook_buttonTop.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }*/

        initializeViews(root);
        searchView = root.findViewById(R.id.searchView_main);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!(currentUser ==null)){
                    mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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
                    mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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

        scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        getDataFromRecommendedBooks();

        getDataFromFavouriteBooks();
        mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null) && currentUser!=null){
                    getData(dataSnapshot);
                    if(books.isEmpty())
                        root.findViewById(R.id.noReadBooks).setVisibility(View.VISIBLE);
                    getDataFromPlannedBooks();
                }
                else goToLoginActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();
                bookListStorageHelper.setBooks_read_list(books);
                bookListStorageHelper.setBooks_planned_list(books_planned);
                Intent intent = new Intent(getContext(), AddBookActivity.class);
                intent.putExtra(AppConstants.PARAM_ADD_BOOK_TABLE, "Read books");
                startActivity(intent);
            }
        });

        seeAllBooks.setOnClickListener(new View.OnClickListener() {
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

        searchForVariable_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    searchForVariable_iv.startAnimation(scaleUp);
                    if(!(currentUser ==null)){
                        if (searchForVariable_et.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "Please enter an author/title/year", Toast.LENGTH_SHORT).show();
                            return false;
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
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    searchForVariable_iv.startAnimation(scaleDown);
                }
                return true;
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
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

    public boolean favContainsBook(String book_id){
        for(String id : favourite_books){
            if(id.contains(book_id))
                return true;
        }
        return false;
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.removeAll(books);
        ArrayList<BookReadData> books_recommended_aux = books_recommended;
        AppConstants.BookExistsInFav.removeAll(AppConstants.BookExistsInFav);
        for(DataSnapshot ds : dataSnapshot.getChildren()){

            String author_name = "", book_title = "", book_id=String.valueOf(ds.child("id").getValue());
            String uri="", video_path="";
            if(book_id!="null"){
                for (BookReadData book : books_recommended_aux) {
                    if (book_id.contains(book.getId())) {
                        author_name = book.getAuthor_name();
                        book_title = book.getTitle();
                        uri = book.getUri();
                        video_path = book.getVideo_path();
                        break;
                    }
                }
            }
            else{
                author_name = String.valueOf(ds.child("author_name").getValue());
                book_title = String.valueOf(ds.child("title").getValue());
            }

            String year = String.valueOf(ds.child("read_year").getValue());
            String month = String.valueOf(ds.child("read_month").getValue());
            if(month == null)
                month = "";

            BookReadData newBook = new BookReadData(author_name, book_title, month, year);
            if (!uri.isEmpty())
                newBook.setUri(uri);

            if (!video_path.isEmpty())
                newBook.setVideo_path(video_path);

            newBook.setId(String.valueOf(ds.getKey()));
            books.add(newBook);

            if(favContainsBook(String.valueOf(ds.getKey())))
                AppConstants.BookExistsInFav.add("Yes");
            else
                AppConstants.BookExistsInFav.add("No");
        }
            setRecyclerView();
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String variable) {
        books.removeAll(books);
        ArrayList<BookReadData> books_recommended_aux = books_recommended;
        //AppConstants.IMG_CAME_FROM.remove(AppConstants.IMG_CAME_FROM);
        AppConstants.BookExistsInFav.removeAll(AppConstants.BookExistsInFav);
        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            String author_name = "", book_title = "", book_id=String.valueOf(ds.child("id").getValue());
            String uri="", video_path="";
            if(book_id!="null") {
                for (BookReadData book : books_recommended_aux) {
                    if (book_id.contains(book.getId()) || book.getId().contains(book_id)) {
                        author_name = book.getAuthor_name();
                        book_title = book.getTitle();
                        uri = book.getUri();
                        video_path = book.getVideo_path();
                        break;
                    }
                }
            }
            else{
                author_name = String.valueOf(ds.child("author_name").getValue());
                book_title = String.valueOf(ds.child("title").getValue());
            }

                String read_year = String.valueOf(ds.child("read_year").getValue());
                String month = String.valueOf(ds.child("read_month").getValue());
                if (month == null)
                    month = "";

                String variable_lower_case = variable.toLowerCase();
                if (author_name.toLowerCase().contains(variable_lower_case)
                        || book_title.toLowerCase().contains(variable_lower_case)
                        || read_year.equals(variable)) {

                    BookReadData newBook = new BookReadData(author_name, book_title, month, read_year);

                    if (!uri.isEmpty())
                        newBook.setUri(uri);

                    if (!video_path.isEmpty())
                        newBook.setVideo_path(video_path);

                    newBook.setId(String.valueOf(ds.getKey()));
                    books.add(newBook);

                    if (favContainsBook(book_id))
                        AppConstants.BookExistsInFav.add("Yes");
                    else
                        AppConstants.BookExistsInFav.add("No");
                }
        }
        //if(!books.isEmpty())
        setRecyclerView();
    }


    public void getDataFromPlannedBooks(){
        mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_planned.removeAll(books_planned);
                String author=null, title=null;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    BookReadData newBook = new BookReadData();
                    newBook.setId(String.valueOf(ds.getKey()));
                    if(String.valueOf(ds.child("id").getValue())!=null) {
                        String book_id = String.valueOf(ds.child("id").getValue());
                        for(BookReadData book: books_recommended){
                            if(book_id.contains(book.getId()) || book.getId().contains(book_id)){
                                author = book.getAuthor_name();
                                title = book.getTitle();
                            }
                        }
                    }
                    else{
                        author = String.valueOf(ds.child("author_name").getValue());
                        title = String.valueOf(ds.child("title").getValue());
                    }
                    newBook.setAuthor_name(author);
                    newBook.setTitle(title);
                    books_planned.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    String video_path = String.valueOf(ds.child("video_path").getValue());
                    BookReadData newBook = new BookReadData(author_name, book_title);
                    newBook.setUri(uri);
                    newBook.setVideo_path(video_path);
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

    public void getDataFromFavouriteBooks(){
        mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favourite_books.removeAll(favourite_books);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String book_id = String.valueOf(ds.getKey());
                    favourite_books.add(book_id);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initializeViews(View root){
        seeAllBooks = root.findViewById(R.id.tv_seeAll_read);
        recyclerView = root.findViewById(R.id.rv_book_list);
        searchForVariable_et = root.findViewById(R.id.et_searchForVariable);
        searchForVariable_iv = root.findViewById(R.id.iv_search_icon);

        fab = root.findViewById(R.id.fab);
    }
}


