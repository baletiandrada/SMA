package com.example.booksapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.example.booksapp.dataModels.BookRankingModel;
import com.example.booksapp.ComputeUserBasedSimilarity;
import com.example.booksapp.MainActivity;
import com.example.booksapp.R;
import com.example.booksapp.RecyclerItemCllickListener;
import com.example.booksapp.adapters.BooksRecommendedAdapter;
import com.example.booksapp.adapters.GenreModelAdapter;
import com.example.booksapp.dataModels.BookData;
import com.example.booksapp.dataModels.GenreModel;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.helpers.BookListStorageHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.recyclerview.widget.LinearLayoutManager.*;
import static com.example.booksapp.helpers.FirebaseHelper.mBookGenresDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksPlannedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mUserDatabase;

public class BooksRecommendedFragment extends Fragment {
    private Button seeAllRecommendedBooks_button;
    private Spinner genres_spinner;
    private RecyclerView recyclerView;
    private TextView tv_seeAll, tv, tv1;
    private FloatingActionButton fab;

    private ImageView iv1;
    private RelativeLayout r1;

    private List<BookData> books = new ArrayList<BookData>();
    private BooksRecommendedAdapter listExampleAdapterBooks;

    private static boolean toast_message;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    List<String> user_read_books = new ArrayList<String>(), user_planned_books = new ArrayList<String>();  //lista contine genurile din cartile citite si din cele planificate
    List<String> user_list = new ArrayList<String>();
    ArrayList<String>  user_books = new ArrayList<String>();
    ArrayList<String> user_book_genres = new ArrayList<String>();
    
    private GenreModelAdapter genreModelAdapter;
    private RecyclerView genresRecyclerView;
    List<GenreModel> genres_from_DB = new ArrayList<GenreModel>();

    Animation scaleUp, scaleDown;

    private Context activity;

    String ratingMeanScore="0";
    ArrayList<AppreciateBookModel> book_rating_list = new ArrayList<AppreciateBookModel>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_books_recommended, container, false);

        seeAllRecommendedBooks_button = root.findViewById(R.id.btn_seeAllRecommendedBooks);

        scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        getRatings();

        setUserList();

        getTop5Ratings();
        getAllBooksFromDB();

        getUserBooks();

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
                            books.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String author_name = String.valueOf(ds.child("author_name").getValue());
                                String book_title = String.valueOf(ds.child("title").getValue());
                                String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
                                BookData newBook = new BookData(author_name, book_title, genre);
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
                                setRecyclerView(books);
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
                        if(!(currentUser.getUid() == null))
                            getDataByVariable(genreModel.getName());
                    }
                })
        );

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookListStorageHelper bookListStorageHelper = BookListStorageHelper.getInstance();
                getAllBooksFromDB();
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

    public void getUserBooks(){
        if(currentUser.getUid() != null){
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
        getDataFromRecommendedDB();
    }

    public void getValuesFromGenresDB(){
        mBookGenresDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(currentUser == null)){
                    genres_from_DB.clear();
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
        user_books.clear();
        user_books.addAll(user_read_books);
        user_books.addAll(user_planned_books);
    }

    public void getReadBookGenres(DataSnapshot dataSnapshot){
        user_read_books.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            String book_id=String.valueOf(ds.child("id").getValue());
            if(book_id!="null")
                user_read_books.add(book_id);
        }
    }

    public void getPlannedBookGenres(DataSnapshot dataSnapshot){
        user_planned_books.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            String book_id = String.valueOf(ds.child("id").getValue());
            if (book_id != "null")
                user_planned_books.add(book_id);
        }
    }

    public void getGenreValuesSpinner(List<BookData> book_list){
        user_book_genres.clear();
        for(BookData book: book_list){
            if (!user_book_genres.contains(book.getGenre()))
                user_book_genres.add(book.getGenre().toLowerCase());
        }
    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }

    public void setRecyclerView(List<BookData> book_list){
        listExampleAdapterBooks = new BooksRecommendedAdapter(book_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listExampleAdapterBooks);
    }

    public void setUserList(){
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    user_list.add(String.valueOf(ds.getKey()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean bookExistsInList(String book_id, List<BookData> book_list){
        for(BookData book : book_list){
            if(book.getId().equals(book_id))
                return true;
        }
        return false;
    }

    private List<AppreciateBookModel> all_ratings_list = new ArrayList<>();
    private void getTop5Ratings(){
        mRatingsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("book_id").getValue());

                    boolean bookExists = false;
                    for(AppreciateBookModel rating: all_ratings_list){
                        if(rating.getBook_id().equals(book_id))
                            bookExists=true;
                    }

                    if(!bookExists && !String.valueOf(ds.child("user_id").getValue()).equals(currentUser.getUid())){
                        String rating = String.valueOf(ds.child("rating").getValue());
                        AppreciateBookModel newRatingData = new AppreciateBookModel();
                        newRatingData.setBook_id(book_id);

                        computeMeanRating(book_id);
                        newRatingData.setRating(ratingMeanScore);
                        ratingMeanScore="0";
                        all_ratings_list.add(newRatingData);
                        Collections.sort(all_ratings_list, Collections.reverseOrder());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private List<BookData> all_books_from_DB = new ArrayList<>();
    private void getAllBooksFromDB(){
        mBooksRecommendedDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AppConstants.MEAN_RATING_RECOMM_FRAG.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String author_name = String.valueOf(ds.child("author_name").getValue());
                    String book_title = String.valueOf(ds.child("title").getValue());
                    String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
                    BookData newBook = new BookData(author_name, book_title);
                    newBook.setGenre(genre);

                    String video_path = String.valueOf(ds.child("video_path").getValue());
                    if(video_path!=null)
                        newBook.setVideo_path(video_path);

                    String uri = String.valueOf(ds.child("uri").getValue());
                    if(uri!=null)
                        newBook.setUri(uri);

                    String description = String.valueOf(ds.child("description").getValue());
                    if(description!="null")
                        newBook.setDescription(description);

                    newBook.setId(String.valueOf(ds.getKey()));

                    if(!bookExistsInList(ds.getKey(), all_books_from_DB))
                        all_books_from_DB.add(newBook);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private List<BookData> top5Books = new ArrayList<>();
    private void getTop5Books(){
        List<BookData> aux_list = new ArrayList<>();

        for(AppreciateBookModel rating: all_ratings_list){
            for(BookData book : all_books_from_DB)
                if(rating.getBook_id().equals(book.getId()))
                    if(!bookExistsInList(book.getId(), aux_list)
                    && !user_read_books.contains(book.getId())
                            && !user_planned_books.contains(book.getId())){
                        aux_list.add(book);
                        computeMeanRating(book.getId());
                        if(!ratingMeanScore.equals("0"))
                            AppConstants.MEAN_RATING_RECOMM_FRAG.add(ratingMeanScore);
                        else
                            AppConstants.MEAN_RATING_RECOMM_FRAG.add("0");
                        ratingMeanScore="0";
                        break;
                    }

        }

        if(aux_list.size()>5){
            List<BookData> list = new ArrayList<>();
            int i;
            for(i=0;i<5;i++){
                list.add(aux_list.get(i));
            }
        }

        top5Books.clear();
        top5Books.addAll(aux_list);
    }

    private void getData(DataSnapshot dataSnapshot) {
        books.clear();
        AppConstants.MEAN_RATING_RECOMM_FRAG.clear();

        ArrayList<BookRankingModel> rankings = ComputeUserBasedSimilarity.getRecommandations(currentUser.getUid(), user_list, user_books, book_rating_list);
        if(rankings!=null){
            for(BookRankingModel ranking : rankings){

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    if(ranking.getBook_id().equals(String.valueOf(ds.getKey()))){

                        String author_name = String.valueOf(ds.child("author_name").getValue());
                        String book_title = String.valueOf(ds.child("title").getValue());
                        String genre = String.valueOf(ds.child("genre").getValue()).toLowerCase();
                        String video_path = String.valueOf(ds.child("video_path").getValue());
                        BookData newBook = new BookData(author_name, book_title);
                        if(video_path!=null)
                            newBook.setVideo_path(video_path);
                        String uri = String.valueOf(ds.child("uri").getValue());
                        if(uri!=null)
                            newBook.setUri(uri);
                        String description = String.valueOf(ds.child("description").getValue());
                        if(description!="null")
                            newBook.setDescription(description);
                        newBook.setId(String.valueOf(ds.getKey()));
                        newBook.setGenre(genre);
                        books.add(newBook);

                        computeMeanRating(String.valueOf(ds.getKey()));
                        if(!ratingMeanScore.equals("0"))
                            AppConstants.MEAN_RATING_RECOMM_FRAG.add(ratingMeanScore);
                        else
                            AppConstants.MEAN_RATING_RECOMM_FRAG.add("0");
                        ratingMeanScore="0";
                    }

                }
            }
        }

        else{
            getTop5Books();
            books.clear();
            books.addAll(top5Books);
            int i=0;
        }

        if(!books.isEmpty()){
            setRecyclerView(books);

            getGenreValuesSpinner(books);
            getValuesFromGenresDB();

        }

        else if(!toast_message){
            toast_message = true;
        }

    }

    private void getDataByVariable(String genre_variable){
        List<BookData> aux_book_list = new ArrayList<>();
        AppConstants.MEAN_RATING_RECOMM_FRAG.clear();

        for(BookData book : books){
            if(book.getGenre().toLowerCase().contains(genre_variable.toLowerCase())){

                aux_book_list.add(book);

                computeMeanRating(String.valueOf(book.getId()));
                if(!ratingMeanScore.equals("0"))
                    AppConstants.MEAN_RATING_RECOMM_FRAG.add(ratingMeanScore);
                else
                    AppConstants.MEAN_RATING_RECOMM_FRAG.add("0");
                ratingMeanScore="0";
            }
        }

        if(!aux_book_list.isEmpty())
            setRecyclerView(aux_book_list);
        else
            Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
    }

    public void getRatings(){
        mRatingsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                book_rating_list.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("book_id").getValue());
                    String user_id = String.valueOf(ds.child("user_id").getValue());
                    String rating = String.valueOf(ds.child("rating").getValue());
                    AppreciateBookModel ratingData = new AppreciateBookModel();
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
            int number_of_ratings=0;
            for(AppreciateBookModel model : book_rating_list){
                if(model.getBook_id().equals(book_id)){
                    rating_sum+=Integer.parseInt(model.getRating());
                    number_of_ratings++;
                }

            }
            if(rating_sum!=0){
                double meanScore= (double)rating_sum/number_of_ratings;
                ratingMeanScore=String.format("%.1f", meanScore);
            }
        }
    }

}
