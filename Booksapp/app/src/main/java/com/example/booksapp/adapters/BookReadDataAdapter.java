package com.example.booksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.AppConstants;
import com.example.booksapp.EditBookActivity;
import com.example.booksapp.BookReviewsActivity;
import com.example.booksapp.VideoPopUpActivity;
import com.example.booksapp.dataModels.BookData;
import com.example.booksapp.dataModels.AppreciateBookModel;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.R;
import com.example.booksapp.helpers.AppreciateBookStorageHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.booksapp.AppConstants.ADD_REVIEW_ENABLED;
import static com.example.booksapp.AppConstants.BOOK_ID_LIST_READ;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mRatingsDatabase;

public class BookReadDataAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{
    private List<BookData> choicesList;
    private Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    ArrayList<AppreciateBookModel> book_rating_list = new ArrayList<AppreciateBookModel>();
    String ratingMeanScore="0";

    public BookReadDataAdapter(List<BookData> bookList){
        this.choicesList = bookList;
    }

    @NonNull
    @Override
    public BookReadDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_book_data, parent, false);
        BookReadDataViewHolder viewHolder = new BookReadDataViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull BookReadDataViewHolder holder, int position) {
        BookData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_done_text).setVisibility(View.GONE);

        if(AppConstants.BookExistsInFav.get(position).equals("Yes"))
            holder.itemView.findViewById(R.id.iv_heart_colored_icon).setVisibility(View.VISIBLE);
        else
            holder.itemView.findViewById(R.id.iv_heart_discolored_icon).setVisibility(View.VISIBLE);
        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());

        if(!(bookModel.getUri()==null) && !bookModel.getUri().isEmpty() && !bookModel.getUri().equals("null"))
            Glide.with(context).load(bookModel.getUri()).placeholder(R.mipmap.ic_launcher).into(holder.iv_book_image);

        holder.itemView.findViewById(R.id.tv_see_reviews).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.tv_see_reviews).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookReviewsActivity.class);
                AppreciateBookStorageHelper appreciateBookStorageHelper = AppreciateBookStorageHelper.getInstance();
                appreciateBookStorageHelper.setUser_id(currentUser.getUid());
                appreciateBookStorageHelper.setBook_id(BOOK_ID_LIST_READ.get(position));
                appreciateBookStorageHelper.setAuthor_name(bookModel.getAuthor_name());
                appreciateBookStorageHelper.setBook_title(bookModel.getTitle());
                intent.putExtra(ADD_REVIEW_ENABLED, "YES");
                context.startActivity(intent);
            }
        });

        //rating UI
        holder.itemView.findViewById(R.id.layout_users_rating).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.GONE);


        final String[] current_rating_id = {""};

        mRatingsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                book_rating_list.removeAll(book_rating_list);
                for(DataSnapshot ds: snapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("book_id").getValue());
                    String user_id = String.valueOf(ds.child("user_id").getValue());
                    if(book_id.equals(BOOK_ID_LIST_READ.get(position)) && user_id.equals(currentUser.getUid()))
                        current_rating_id[0] = String.valueOf(ds.getKey());

                    String rating = String.valueOf(ds.child("rating").getValue());
                    AppreciateBookModel ratingModel = new AppreciateBookModel();
                    ratingModel.setBook_id(book_id);
                    ratingModel.setUser_id(user_id);
                    ratingModel.setRating(rating);
                    book_rating_list.add(ratingModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.findViewById(R.id.iv_user_rating_discolored).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextInputEditText inputEditText;
                TextInputLayout textInputLayout;
                androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(context);
                new androidx.appcompat.app.AlertDialog.Builder(context, R.style.InputDialogTheme);
                View viewInflated = LayoutInflater.from(context).inflate(R.layout.view_input_dialog, (ViewGroup) holder.itemView.findViewById(R.id.et_input_dialog) , false);
                inputEditText = viewInflated.findViewById(R.id.et_input_dialog);
                textInputLayout = viewInflated.findViewById(R.id.til_input_dialog);
                alert.setView(viewInflated);
                alert.setTitle("Rate this book");
                textInputLayout.setHint("From 1 to 5");

                alert.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (inputEditText.getText() == null || inputEditText.getText().toString().isEmpty())
                        {
                            Toast.makeText(context, "Rating field is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] numbers=new String[]{"1", "2", "3", "4", "5"};
                        List<String> list_of_numbers = Arrays.asList(numbers);
                        if(!(list_of_numbers.contains(inputEditText.getText().toString()))){
                            Toast.makeText(context, "You have to introduce a number between 1 and 5", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        holder.itemView.findViewById(R.id.iv_user_rating_discolored).setVisibility(View.GONE);
                        holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.VISIBLE);
                        holder.user_rating.setText(inputEditText.getText().toString());

                        AppreciateBookModel newRating = new AppreciateBookModel();
                        newRating.setBook_id(BOOK_ID_LIST_READ.get(position));
                        newRating.setUser_id(currentUser.getUid());
                        newRating.setRating(inputEditText.getText().toString());
                        String rating_id = mRatingsDatabase.push().getKey();
                        mRatingsDatabase.child(rating_id).setValue(newRating);
                        Toast.makeText(context, "Rating added successfully", Toast.LENGTH_SHORT).show();

                        book_rating_list.add(newRating);
                        computeMeanRating(BOOK_ID_LIST_READ.get(position));
                        if(!ratingMeanScore.equals("0")){
                            holder.mean_rating.setText(ratingMeanScore + "/5");
                        }
                        holder.itemView.findViewById(R.id.iv_users_rating_discolored).setVisibility(View.GONE);
                        holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.VISIBLE);
                        ratingMeanScore="0";

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();
            }
        });

        if(!AppConstants.USER_RATING.get(position).equals("0")){
            holder.user_rating.setText(String.valueOf(AppConstants.USER_RATING.get(position)));
            holder.itemView.findViewById(R.id.iv_user_rating_discolored).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.VISIBLE);
        }

        if(!AppConstants.MEAN_RATING.get(position).equals("0")){
            holder.mean_rating.setText(AppConstants.MEAN_RATING.get(position) + "/5");
            holder.itemView.findViewById(R.id.iv_users_rating_discolored).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.VISIBLE);
        }

        holder.itemView.findViewById(R.id.iv_user_rating_colored).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextInputEditText inputEditText;
                TextInputLayout textInputLayout;
                androidx.appcompat.app.AlertDialog.Builder alert = new androidx.appcompat.app.AlertDialog.Builder(context);
                new androidx.appcompat.app.AlertDialog.Builder(context, R.style.InputDialogTheme);
                View viewInflated = LayoutInflater.from(context).inflate(R.layout.view_input_dialog, (ViewGroup) holder.itemView.findViewById(R.id.et_input_dialog) , false);
                inputEditText = viewInflated.findViewById(R.id.et_input_dialog);
                textInputLayout = viewInflated.findViewById(R.id.til_input_dialog);
                alert.setView(viewInflated);
                alert.setTitle("Edit rating");
                textInputLayout.setHint("From 1 to 5");

                alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (inputEditText.getText() == null || inputEditText.getText().toString().isEmpty())
                        {
                            Toast.makeText(context, "Rating field is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] numbers=new String[]{"1", "2", "3", "4", "5"};
                        List<String> list_of_numbers = Arrays.asList(numbers);
                        if(!(list_of_numbers.contains(inputEditText.getText().toString()))){
                            Toast.makeText(context, "You have to introduce a number between 1 and 5", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        if(BOOK_ID_LIST_READ.get(position)!="null" && BOOK_ID_LIST_READ.get(position)!=null){
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("book_id", BOOK_ID_LIST_READ.get(position));
                            map.put("user_id", currentUser.getUid());
                            map.put("rating", inputEditText.getText().toString());
                            mRatingsDatabase.child(current_rating_id[0]).updateChildren(map);
                            Toast.makeText(context, "Rating updated successfully", Toast.LENGTH_SHORT).show();

                            holder.user_rating.setText(inputEditText.getText().toString());

                            for(AppreciateBookModel model: book_rating_list){
                                if(model.getBook_id().equals(BOOK_ID_LIST_READ.get(position)) && model.getUser_id().equals(currentUser.getUid())){
                                    model.setRating(inputEditText.getText().toString());
                                    break;
                                }
                            }
                            computeMeanRating(BOOK_ID_LIST_READ.get(position));
                            if(!ratingMeanScore.equals("0")){
                                holder.mean_rating.setText(ratingMeanScore + "/5");
                            }
                            ratingMeanScore="0";
                        }
                        else
                            Toast.makeText(context, "Books id is NULL", Toast.LENGTH_SHORT).show();
                    }
                });

                alert.setNegativeButton("Delete rating", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mRatingsDatabase.child(current_rating_id[0]).removeValue();
                        holder.user_rating.setText("Rate");
                        holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.GONE);
                        holder.itemView.findViewById(R.id.iv_user_rating_discolored).setVisibility(View.VISIBLE);

                        for(AppreciateBookModel model: book_rating_list){
                            if(model.getBook_id().equals(BOOK_ID_LIST_READ.get(position)) && model.getUser_id().equals(currentUser.getUid())){
                                book_rating_list.remove(model);
                                break;
                            }
                        }

                        computeMeanRating(BOOK_ID_LIST_READ.get(position));
                        if(!ratingMeanScore.equals("0")){
                            holder.mean_rating.setText(ratingMeanScore + "/5");
                        }
                        else{
                            holder.mean_rating.setText("No rating");
                            holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.iv_users_rating_discolored).setVisibility(View.VISIBLE);
                        }
                    }
                });
                alert.show();
            }
        });

        holder.itemView.findViewById(R.id.iv_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this book?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(choicesList==null)) {
                                    Toast.makeText(context, "Book deleted successfully", Toast.LENGTH_SHORT).show();
                                    if (currentUser != null) {
                                        mBooksReadDatabase.child(currentUser.getUid()).child(bookModel.getId()).removeValue();
                                        mFavouriteBooksDatabase.child(currentUser.getUid()).child(bookModel.getId()).removeValue();
                                        mQuotesDatabase.child(bookModel.getId()).removeValue();
                                        choicesList.remove(bookModel);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, choicesList.size());
                                    }

                                }
                            }
                        }).setNegativeButton("CANCEL", null);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        holder.itemView.findViewById(R.id.iv_edit_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();
                bookStorageHelper.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
                bookStorageHelper.setId_book(bookModel.getId());
                Intent intent = new Intent(context, EditBookActivity.class);
                String param_bookTable_value = "Read books";
                intent.putExtra(AppConstants.PARAM_EDIT_BOOK_TABLE, param_bookTable_value);
                context.startActivity(intent);
            }
        });

        getDataFromFavouriteBooks();

        holder.itemView.findViewById(R.id.iv_heart_colored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String book_id = bookModel.getId();
                mFavouriteBooksDatabase.child(currentUser.getUid()).child(getFavBookKey(book_id)).removeValue();
                holder.itemView.findViewById(R.id.iv_heart_colored_icon).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.iv_heart_discolored_icon).setVisibility(View.VISIBLE);
            }
        });

        holder.itemView.findViewById(R.id.iv_heart_discolored_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = bookModel.getId();
                BookData newbook = new BookData();
                newbook.setId(id);
                String entry_id = mFavouriteBooksDatabase.child(currentUser.getUid()).push().getKey();
                mFavouriteBooksDatabase.child(currentUser.getUid()).child(entry_id).setValue(newbook);
                Toast.makeText(context, "Book added to your favourite ones", Toast.LENGTH_SHORT).show();
                holder.itemView.findViewById(R.id.iv_heart_discolored_icon).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.iv_heart_colored_icon).setVisibility(View.VISIBLE);
            }
        });


        if(!(bookModel.getVideo_path()=="null" || bookModel.getVideo_path()=="" || bookModel.getVideo_path()==null))
        {
            holder.itemView.findViewById(R.id.tv_watch_trailer).setVisibility(View.VISIBLE);
            if(bookModel.getYouTubePlayer()==null) {
                bookModel.setYouTubePlayer(holder.itemView.findViewById(R.id.youtube_player));
                YouTubePlayerView youTubePlayerView = bookModel.getYouTubePlayer();
                youTubePlayerView.setEnableAutomaticInitialization(false);
                youTubePlayerView.initialize(new YouTubePlayerListener() {
                    @Override
                    public void onReady(@NotNull YouTubePlayer youTubePlayer) {
                        youTubePlayer.cueVideo(bookModel.getVideo_path(), 0);
                    }

                    @Override
                    public void onStateChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerState playerState) {

                    }

                    @Override
                    public void onPlaybackQualityChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlaybackQuality playbackQuality) {

                    }

                    @Override
                    public void onPlaybackRateChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlaybackRate playbackRate) {

                    }

                    @Override
                    public void onError(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerError playerError) {

                    }

                    @Override
                    public void onCurrentSecond(@NotNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoDuration(@NotNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoLoadedFraction(@NotNull YouTubePlayer youTubePlayer, float v) {

                    }

                    @Override
                    public void onVideoId(@NotNull YouTubePlayer youTubePlayer, @NotNull String s) {

                    }

                    @Override
                    public void onApiChange(@NotNull YouTubePlayer youTubePlayer) {
                        //youTubePlayer.mute();
                    }
                });
                youTubePlayerView.addFullScreenListener(new YouTubePlayerFullScreenListener() {
                    @Override
                    public void onYouTubePlayerEnterFullScreen() {
                        Intent intent = new Intent(context, VideoPopUpActivity.class);
                        intent.putExtra(AppConstants.VIDEO_PATH, bookModel.getVideo_path());
                        bookStorageHelper.setBook_title(bookModel.getTitle());
                        //Toast.makeText(context, bookStorageHelper.getBook_title(), Toast.LENGTH_SHORT).show();
                        context.startActivity(intent);
                    }

                    @Override
                    public void onYouTubePlayerExitFullScreen() {

                    }
                });
            }
        }

        holder.itemView.findViewById(R.id.tv_watch_trailer).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                holder.itemView.findViewById(R.id.tv_watch_trailer).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.youtube_player).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.tv_hide_trailer).setVisibility(View.VISIBLE);
            }
        });

        holder.itemView.findViewById(R.id.tv_hide_trailer).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                holder.itemView.findViewById(R.id.tv_hide_trailer).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.youtube_player).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.tv_watch_trailer).setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }

    private class BookIdAndKey{
        private String id, key;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    private ArrayList<BookIdAndKey> favourite_books = new ArrayList<BookIdAndKey>();

    public void getDataFromFavouriteBooks(){
        mFavouriteBooksDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favourite_books.removeAll(favourite_books);
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String book_id = String.valueOf(ds.child("id").getValue());
                    String book_key = String.valueOf(ds.getKey());
                    BookIdAndKey book_data = new BookIdAndKey();
                    book_data.setId(book_id);
                    book_data.setKey(book_key);
                    favourite_books.add(book_data);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public String getFavBookKey(String book_id){
        for(BookIdAndKey id : favourite_books){
            if(id.getId().equals(book_id))
                return id.getKey();
        }
        return "";
    }

    public void computeMeanRating(String book_id){
        if(book_rating_list.size()!=0){
            int rating_sum=0;
            int number_of_ratings=0;
            for(AppreciateBookModel model : book_rating_list){
                if(model.getBook_id()!="null" && model.getBook_id().equals(book_id)){
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