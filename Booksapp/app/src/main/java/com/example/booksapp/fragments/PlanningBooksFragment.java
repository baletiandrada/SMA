package com.example.booksapp.fragments;

import android.content.Intent;
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
import com.example.booksapp.adapters.BooksPlannedAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class PlanningBooksFragment extends Fragment {

    private TextView seeAllBooks;
    private Button seeAllBooks_button;
    private EditText searchForVariable_et;
    private ImageView searchForVariable_iv;

    private RecyclerView recyclerView;
    private List<BookReadData> books = new ArrayList<BookReadData>(), books_recommended= new ArrayList<BookReadData>();
    BooksPlannedAdapter listExampleAdapterBooks;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    ArrayList<BookReadData> books_read = new ArrayList<BookReadData>();

    Animation scaleUp, scaleDown;
    private FloatingActionButton fab;
    SearchView searchView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_planning_books, container, false);

        initializeViews(root);
        searchView = root.findViewById(R.id.searchView_plan);

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
                    mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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
        mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    getData(dataSnapshot);
                    if(books.isEmpty())
                        root.findViewById(R.id.noPlannedBooks).setVisibility(View.VISIBLE);
                    getDataFromReadBooks();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();
                bookListStorageHelper.setBooks_planned_list(books);
                bookListStorageHelper.setBooks_read_list(books_read);
                Intent intent = new Intent(getContext(), AddBookActivity.class);
                intent.putExtra(AppConstants.PARAM_ADD_BOOK_TABLE, "Planned books");
                startActivity(intent);
            }
        });

        seeAllBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(currentUser==null)){
                    mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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

        /*searchForVariable_iv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    searchForVariable_iv.startAnimation(scaleUp);
                    if(!(currentUser ==null)){
                        if (searchForVariable_et.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "Please enter an author/title/year", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        mBooksPlannedDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
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
        });*/

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

    public void getDataFromRecommendedBooks(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_recommended.removeAll(books_recommended);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String uri = String.valueOf(ds.child("uri").getValue());
                    BookReadData newBook = new BookReadData(author_name, book_title);
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

    public void setRecyclerView(){
        listExampleAdapterBooks = new BooksPlannedAdapter(books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.removeAll(books);
        AppConstants.IMG_PLAN_CAME_FROM.removeAll(AppConstants.IMG_CAME_FROM);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            //Toast.makeText(getApplicationContext(), String.valueOf(ds.child("author_name").getValue()), Toast.LENGTH_LONG).show();
            String author_name = String.valueOf(ds.child("author_name").getValue());
            String book_title = String.valueOf(ds.child("title").getValue());
            String year = String.valueOf(ds.child("read_year").getValue());
            String month = String.valueOf(ds.child("read_month").getValue());
            if(month==null)
                month = "";

            String uri;
            uri = String.valueOf(ds.child("uri").getValue());
            if(uri.equals("null") || uri==null || uri.isEmpty()){
                uri = getUriFromRecommended(author_name, book_title);
                AppConstants.IMG_PLAN_CAME_FROM.add("Admin");
            }
            else
                AppConstants.IMG_PLAN_CAME_FROM.add("User");

            BookReadData newBook = new BookReadData(author_name, book_title, month, year, uri);
            newBook.setId(String.valueOf(ds.getKey()));
            books.add(newBook);
        }
        //if(!books.isEmpty())
            setRecyclerView();
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String variable){
        books.removeAll(books);
        AppConstants.IMG_PLAN_CAME_FROM.removeAll(AppConstants.IMG_CAME_FROM);
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
                if(uri.equals("null") || uri==null || uri.isEmpty()){
                    uri = getUriFromRecommended(author_name, book_title);
                    AppConstants.IMG_PLAN_CAME_FROM.add("Admin");
                }
                else
                    AppConstants.IMG_PLAN_CAME_FROM.add("User");

                newBook.setUri(uri);
                newBook.setId(String.valueOf(ds.getKey()));
                books.add(newBook);
            }
        }
        //if(!books.isEmpty())
            setRecyclerView();
    }

    public void getDataFromReadBooks(){
        mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_read.removeAll(books_read);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String year = String.valueOf(ds.child("read_year").getValue());
                    String month = String.valueOf(ds.child("read_month").getValue());
                    if(month==null)
                        month = "";
                    BookReadData newBook = new BookReadData(author_name, book_title, month, year);
                    newBook.setId(String.valueOf(ds.getKey()));
                    books_read.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUriFromRecommended(String author, String title) {
        String uri = null;
        for(BookReadData book : books_recommended){
            if( (book.getAuthor_name().toLowerCase().contains(author.toLowerCase())||author.toLowerCase().contains(book.getAuthor_name().toLowerCase()))
                    && (book.getTitle().toLowerCase().contains(title.toLowerCase()) || title.toLowerCase().contains(book.getTitle().toLowerCase())) )
                uri = book.getUri();
        }
        return uri;
    }



    public void initializeViews(View root){
        seeAllBooks = root.findViewById(R.id.tv_seeAll_planned);
        seeAllBooks_button = root.findViewById(R.id.btn_plan_seeAllBooks);
        searchForVariable_et = root.findViewById(R.id.et_plan_searchForVariable);
        searchForVariable_iv = root.findViewById(R.id.iv_plan_search_icon);
        recyclerView = root.findViewById(R.id.rv_book_listPlanning);
        fab = root.findViewById(R.id.fab_planning);
    }
}
