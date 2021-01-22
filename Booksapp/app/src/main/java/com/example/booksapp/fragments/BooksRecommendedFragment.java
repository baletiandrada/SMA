package com.example.booksapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.example.booksapp.RecyclerItemCllickListener;
import com.example.booksapp.adapters.BooksRecommendedAdapter;
import com.example.booksapp.adapters.GenreModelAdapter;
import com.example.booksapp.adapters.GenresAdapter;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.dataModels.GenreModel;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static androidx.recyclerview.widget.LinearLayoutManager.*;
import static com.example.booksapp.AppConstants.MY_PREFS_NAME;
import static com.example.booksapp.helpers.FirebaseHelper.mBookGenresDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;

public class BooksRecommendedFragment extends Fragment {
    private Button seeAllRecommendedBooks_button;
    private Spinner genres_spinner;
    private RecyclerView recyclerView;
    private TextView tv_seeAll, tv, tv1;
    private FloatingActionButton fab;

    private ImageView iv1;
    private RelativeLayout r1;

    private List<BookReadData> books = new ArrayList<BookReadData>();
    private BooksRecommendedAdapter listExampleAdapterBooks;

    private GenresAdapter genresAdapter;
    private static boolean toast_message;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    List<BookReadData> user_read_books = new ArrayList<BookReadData>(), user_planned_books = new ArrayList<BookReadData>();  //lista contine genurile din cartile citite si din cele planificate
    List<BookReadData>  user_books = new ArrayList<BookReadData>();
    ArrayList<String> user_book_genres = new ArrayList<String>();
    
    private GenreModelAdapter genreModelAdapter;
    private RecyclerView genresRecyclerView;
    List<GenreModel> genres_from_DB = new ArrayList<GenreModel>();

    Animation scaleUp, scaleDown;

    private Context activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books_recommended, container, false);

        seeAllRecommendedBooks_button = root.findViewById(R.id.btn_seeAllRecommendedBooks);

        scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        getUserBooks();   ////////////*****/////////////
        //GenreModel gm = new GenreModel("fictiune", "https://firebasestorage.googleapis.com/v0/b/smatest-330c4.appspot.com/o/Genres%20icons%2Ffiction.png?alt=media&token=7c5632f6-8106-470b-843d-23aac93b9c35");
        //genres_from_DB.add(gm);
        iv1 = root.findViewById(R.id.gone1);
        tv1 = root.findViewById(R.id.gone2);
        r1 = root.findViewById(R.id.gone3);
        tv = root.findViewById(R.id.noRecommBooks);

        tv_seeAll = root.findViewById(R.id.tv_seeAll);
        genresRecyclerView = root.findViewById(R.id.rv_genre_list);
        recyclerView = root.findViewById(R.id.rv_book_recommended_list);
        fab = root.findViewById(R.id.fab_recommended);
        if(currentUser.getEmail().equals("admin@gmail.com")){
            //seeAllRecommendedBooks_button.setText("See all recommendations from database");
            fab.setVisibility(View.VISIBLE);
        }


        tv_seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tv_seeAll.setTextColor(Color.parseColor("#2E6B79"));
                if(currentUser.getEmail().equals("admin@gmail.com")){
                    mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            books.removeAll(books);
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String author_name = String.valueOf(ds.child("author_name").getValue());
                                String book_title = String.valueOf(ds.child("title").getValue());
                                String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
                                BookReadData newBook = new BookReadData(author_name, book_title, genre);
                                String video_path = String.valueOf(ds.child("video_path").getValue());
                                if (video_path != null)
                                    newBook.setVideo_path(video_path);

                                String uri = String.valueOf(ds.child("uri").getValue());
                                if (uri != null)
                                    newBook.setUri(uri);

                                newBook.setId(String.valueOf(ds.getKey()));
                                books.add(newBook);
                            }
                            if (!books.isEmpty()){
                                setRecyclerView();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    getDataFromRecommendedDB();
            }
        });

        genresRecyclerView.addOnItemTouchListener(
                new RecyclerItemCllickListener(getActivity(), new RecyclerItemCllickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        GenreModel genreModel = genres_from_DB.get(position);
                        //Toast.makeText(getActivity(), genreModel.getName(), Toast.LENGTH_SHORT).show();
                        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(!(currentUser.getUid() == null))
                                    getDataByVariable(dataSnapshot, genreModel.getName());
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
        );

        /*genres_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String genre_item = (String) parent.getItemAtPosition(position);
                mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!(currentUser.getUid() == null)){
                            if( ! genre_item.equals("Select genre") )
                                getDataByVariable(dataSnapshot, genre_item);
                            else
                                getData(dataSnapshot);
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
        });*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();
                getAllDataFromRecommendedDB();
                bookListStorageHelper.setBooks_recommended_list(books);
                Intent intent = new Intent(getContext(), AddBookActivity.class);
                intent.putExtra(AppConstants.PARAM_ADD_BOOK_TABLE, "Recommended books");
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        toast_message=false;
        if (currentUser == null) {
            seeAllRecommendedBooks_button.setVisibility(View.GONE);
            genres_spinner.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        toast_message=false;
        if (currentUser == null) {
            seeAllRecommendedBooks_button.setVisibility(View.GONE);
            genres_spinner.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            goToLoginActivity();
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            activity = context;

        }catch(Exception e){
            throw new ClassCastException(context.toString());

        }
    }
    public void getDataFromRecommendedDB(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null)){
                    getData(dataSnapshot);
                    if(books.isEmpty()){
                        tv.setVisibility(View.VISIBLE);
                        iv1.setVisibility(View.GONE);
                        tv1.setVisibility(View.GONE);
                        r1.setVisibility(View.GONE);
                    }

                    else{
                        tv.setVisibility(View.GONE);
                        iv1.setVisibility(View.VISIBLE);
                        tv1.setVisibility(View.VISIBLE);
                        r1.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getAllDataFromRecommendedDB(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser.getUid() == null))
                    books.removeAll(books);
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String author_name = String.valueOf(ds.child("author_name").getValue());
                        String book_title = String.valueOf(ds.child("title").getValue());
                        String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
                        String video_path = String.valueOf(ds.child("video_path").getValue());
                        BookReadData newBook = new BookReadData(author_name, book_title, genre);
                        if (video_path != null)
                            newBook.setVideo_path(video_path);
                        newBook.setId(String.valueOf(ds.getKey()));
                        books.add(newBook);
                    }
                /*if(!books.isEmpty())
                    setRecyclerView();
                else if(!toast_message){
                    //Toast.makeText(getActivity(), "Sorry, there are no recommendations for you", Toast.LENGTH_SHORT).show();
                    toast_message = true;
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void updateBooks(){
        setUserBooks();
        getGenreValuesSpinner();
        getValuesFromGenresDB();
        getDataFromRecommendedDB();
    }

    public void getValuesFromGenresDB(){
        mBookGenresDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser == null)){
                    genres_from_DB.removeAll(genres_from_DB);
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String genre_name = String.valueOf(ds.child("name").getValue());
                        String genre_src = String.valueOf(ds.child("src").getValue());
                        for( String userBookGenre : user_book_genres){
                            if( userBookGenre.contains(genre_name.toLowerCase()) )
                                if(!containsGenresDB(genre_name.toLowerCase())){
                                    GenreModel genreModel = new GenreModel(genre_name, genre_src);
                                    genres_from_DB.add(genreModel);
                                }
                        }
                    }
                    //Toast.makeText(getActivity(), String.valueOf(genres_from_DB.size()), Toast.LENGTH_SHORT).show();
                    setGenreRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setGenreRecyclerView(){
        genreModelAdapter = new GenreModelAdapter(activity, genres_from_DB);
        genresRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), HORIZONTAL, false));
        genresRecyclerView.setAdapter(genreModelAdapter);
    }

    public boolean containsGenresDB(String genre){
        for(GenreModel genreModel: genres_from_DB)
            if(genreModel.getName().toLowerCase().contains(genre))
                return true;
        return false;
    }

    public void getAllRecommendationsREAD(DataSnapshot dataSnapshot){
        getReadBookGenres(dataSnapshot);
        updateBooks();
    }

    public void getAllRecommendationsPLAN(DataSnapshot dataSnapshot){
        getPlannedBookGenres(dataSnapshot);
        updateBooks();
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
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String author_name = String.valueOf(ds.child("author_name").getValue()).toLowerCase();
                        String book_title = String.valueOf(ds.child("title").getValue()).toLowerCase();
                        BookReadData newBook = new BookReadData(author_name, book_title);
                        for (BookReadData bookInUserBooks : user_books) {
                            if ((bookInUserBooks.getAuthor_name().contains(newBook.getAuthor_name())
                                    || newBook.getAuthor_name().contains(bookInUserBooks.getAuthor_name()))
                                    && (bookInUserBooks.getTitle().contains(newBook.getTitle())
                                    || newBook.getTitle().contains(bookInUserBooks.getTitle()))) {

                                if (!user_book_genres.contains(String.valueOf(ds.child("genre").getValue())))
                                    user_book_genres.add(String.valueOf(ds.child("genre").getValue()).toLowerCase());
                            }
                        }
                    }

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
            String video_path = String.valueOf(ds.child("video_path").getValue());
            BookReadData newBook = new BookReadData(author_name, book_title);
            if(video_path!=null)
                newBook.setVideo_path(video_path);
            //String description = ds.child("description").getValue().toString();
            //if(description!=null)
                //newBook.setDescription(description);
            String uri = String.valueOf(ds.child("uri").getValue());
            if(uri!=null)
                newBook.setUri(uri);

            String description = String.valueOf(ds.child("description").getValue());
            if(description!="null")
                newBook.setDescription(description);

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
        else if(!toast_message){
            //Toast.makeText(getActivity(), "Sorry, there are no recommendations for you", Toast.LENGTH_SHORT).show();
            toast_message = true;
        }

    }

    private void getDataByVariable(DataSnapshot dataSnapshot, String genre_variable){
        books.removeAll(books);
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String variable_lower_case = genre_variable.toLowerCase();
            if(String.valueOf(ds.child("genre").getValue()).toLowerCase().contains(variable_lower_case)){
                String author_name = String.valueOf(ds.child("author_name").getValue());
                String book_title = String.valueOf(ds.child("title").getValue());
                String video_path = String.valueOf(ds.child("video_path").getValue());
                BookReadData newBook = new BookReadData(author_name, book_title);
                if(video_path!=null)
                    newBook.setVideo_path(video_path);
                String uri = String.valueOf(ds.child("uri").getValue());
                if(uri!=null)
                    newBook.setUri(uri);
                String description = String.valueOf(ds.child("description").getValue());
                if(description!="null")
                    newBook.setDescription(description);
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
