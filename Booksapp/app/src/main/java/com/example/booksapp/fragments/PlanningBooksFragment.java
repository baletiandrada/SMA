package com.example.booksapp.fragments;

import android.annotation.SuppressLint;
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
import com.example.booksapp.dataModels.ReviewModel;
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
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;

public class PlanningBooksFragment extends Fragment {

    private TextView seeAllBooks;

    private RecyclerView recyclerView;
    private ArrayList<BookReadData> books = new ArrayList<BookReadData>(), books_recommended= new ArrayList<BookReadData>();
    BooksPlannedAdapter listExampleAdapterBooks;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    ArrayList<BookReadData> books_read = new ArrayList<BookReadData>();

    Animation scaleUp, scaleDown;
    private FloatingActionButton fab;
    SearchView searchView;

    String ratingMeanScore="0";
    ArrayList<ReviewModel> book_rating_list = new ArrayList<ReviewModel>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_planning_books, container, false);

        initializeViews(root);
        searchView = root.findViewById(R.id.searchView_plan);
        getDataFromRecommendedBooks();

        getRatings();

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
        ArrayList<BookReadData> books_recommended_aux = books_recommended;
        AppConstants.MEAN_RATING_PLAN_FRAG.removeAll(AppConstants.MEAN_RATING_PLAN_FRAG);
        for(DataSnapshot ds : dataSnapshot.getChildren()){

            String author_name = "", book_title = "";
            String book_id=String.valueOf(ds.child("id").getValue());
            String uri="";
            if(book_id!="null"){
                for (BookReadData book : books_recommended_aux) {
                    if (book_id.equals(book.getId())) {
                        author_name = book.getAuthor_name();
                        book_title = book.getTitle();
                        uri = book.getUri();
                        break;
                    }
                }
            }

            String year = String.valueOf(ds.child("read_year").getValue());
            String month = String.valueOf(ds.child("read_month").getValue());
            if(month == null)
                month = "";

            BookReadData newBook = new BookReadData(author_name, book_title, month, year);
            if (!uri.isEmpty())
                newBook.setUri(uri);

            newBook.setId(String.valueOf(ds.getKey()));
            newBook.setId_from_big_db(book_id);
            books.add(newBook);

            computeMeanRating(String.valueOf(ds.getKey()));
            if(!ratingMeanScore.equals("0"))
                AppConstants.MEAN_RATING_PLAN_FRAG.add(ratingMeanScore);
            else
                AppConstants.MEAN_RATING_PLAN_FRAG.add("0");
            ratingMeanScore="0";

        }
            setRecyclerView();
    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String variable){
        books.removeAll(books);
        ArrayList<BookReadData> books_recommended_aux = books_recommended;
        AppConstants.MEAN_RATING_PLAN_FRAG.removeAll(AppConstants.MEAN_RATING_PLAN_FRAG);
        for(DataSnapshot ds : dataSnapshot.getChildren()){

            String author_name = "", book_title = "";
            String book_id=String.valueOf(ds.child("id").getValue());
            String uri="";
            if(book_id!="null") {
                for (BookReadData book : books_recommended_aux) {
                    if (book.getId().equals(book_id)) {
                        author_name = book.getAuthor_name();
                        book_title = book.getTitle();
                        uri = book.getUri();
                        break;
                    }
                }
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

                newBook.setId(String.valueOf(ds.getKey()));
                newBook.setId_from_big_db(book_id);
                books.add(newBook);

                computeMeanRating(String.valueOf(ds.getKey()));
                if(!ratingMeanScore.equals("0"))
                    AppConstants.MEAN_RATING_PLAN_FRAG.add(ratingMeanScore);
                else
                    AppConstants.MEAN_RATING_PLAN_FRAG.add("0");
                ratingMeanScore="0";

            }
        }
            setRecyclerView();
    }

    public void getDataFromReadBooks(){
        mBooksReadDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                books_read.removeAll(books_read);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    BookReadData newBook = new BookReadData();
                    newBook.setId(String.valueOf(ds.getKey()));
                    String author=null, title=null;
                    String book_id = String.valueOf(ds.child("id").getValue());
                    if(book_id!="null") {
                        for(BookReadData book: books_recommended){
                            if(book_id.equals(book.getId())){
                                author = book.getAuthor_name();
                                title = book.getTitle();
                            }
                        }
                    }
                    newBook.setAuthor_name(author);
                    newBook.setTitle(title);
                    newBook.setId_from_big_db(book_id);
                    books_read.add(newBook);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getRatings(){
        mRatingsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                book_rating_list.removeAll(book_rating_list);
                for(DataSnapshot ds: snapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("book_id").getValue());
                    String user_id = String.valueOf(ds.child("user_id").getValue());
                    String rating = String.valueOf(ds.child("rating").getValue());
                    ReviewModel ratingData = new ReviewModel();
                    ratingData.setUser_id(user_id);
                    ratingData.setBook_id(book_id);
                    ratingData.setRating(rating);
                    book_rating_list.add(ratingData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void computeMeanRating(String book_id){
        if(book_rating_list.size()!=0){
            int rating_sum=0;
            for(ReviewModel model : book_rating_list){
                if(model.getBook_id().equals(book_id))
                    rating_sum+=Integer.parseInt(model.getRating());
            }
            if(rating_sum!=0){
                double meanScore= (double)rating_sum/(book_rating_list.size());
                ratingMeanScore=String.format("%.1f", meanScore);
            }
        }
    }


    public void initializeViews(View root){
        seeAllBooks = root.findViewById(R.id.tv_seeAll_planned);
        recyclerView = root.findViewById(R.id.rv_book_listPlanning);
        fab = root.findViewById(R.id.fab_planning);
    }
}
