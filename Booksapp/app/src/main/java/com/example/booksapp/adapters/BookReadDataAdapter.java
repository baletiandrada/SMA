package com.example.booksapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.booksapp.AppConstants;
import com.example.booksapp.BookEditActivity;
import com.example.booksapp.VideoPopUpActivity;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.R;
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

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.booksapp.AppConstants.MY_PREFS_NAME;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksReadDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mQuotesDatabase;

public class BookReadDataAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{
    private List<BookReadData> choicesList;
    private Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    public BookReadDataAdapter(List<BookReadData> bookList){
        this.choicesList = bookList;
    }

    @NonNull
    @Override
    public BookReadDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater. inflate(R.layout.row_book_read_data, parent, false);
        BookReadDataViewHolder viewHolder = new BookReadDataViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull BookReadDataViewHolder holder, int position) {
        BookReadData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_genre).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_done_text).setVisibility(View.GONE);

        holder.itemView.findViewById(R.id.iv_heart_icon).setVisibility(View.VISIBLE);
        if(AppConstants.BookExistsInFav.get(position).equals("Yes"))
            ((ImageView) holder.itemView.findViewById(R.id.iv_heart_icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.heart_icon_selected, context.getApplicationContext().getTheme()));
        else
            ((ImageView) holder.itemView.findViewById(R.id.iv_heart_icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.heart_icon, context.getApplicationContext().getTheme()));
        holder.setValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getRead_month(), bookModel.getRead_year());
        if(!(bookModel.getUri()==null) && !bookModel.getUri().isEmpty() && !bookModel.getUri().equals("null"))
            Glide.with(context).load(bookModel.getUri()).placeholder(R.mipmap.ic_launcher).into(holder.iv_book_image);
        else
            holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);

        if(AppConstants.IMG_CAME_FROM.get(position).equals("Admin"))
            holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);

        holder.itemView.findViewById(R.id.iv_delete_just_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBooksReadDatabase.child(currentUser.getUid()).child(bookModel.getId()).child("uri").removeValue();
                mFavouriteBooksDatabase.child(currentUser.getUid()).child(bookModel.getId()).child("uri").removeValue();
                holder.itemView.findViewById(R.id.iv_delete_just_image).setBackgroundResource(R.drawable.default_book_image);
                holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);
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
                Intent intent = new Intent(context, BookEditActivity.class);
                String param_bookTable_value = "Read books";
                intent.putExtra(AppConstants.param_bookTable, param_bookTable_value);
                context.startActivity(intent);
            }
        });

        holder.itemView.findViewById(R.id.iv_heart_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppConstants.BookExistsInFav.get(position).equals("No")){
                    String id = bookModel.getId();
                    BookReadData newbook = new BookReadData(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getUri());
                    mFavouriteBooksDatabase.child(currentUser.getUid()).child(id).setValue(newbook);
                    ((ImageView) holder.itemView.findViewById(R.id.iv_heart_icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.heart_icon_selected, context.getApplicationContext().getTheme()));
                }
                else if(AppConstants.BookExistsInFav.get(position).equals("Yes")){
                    String id = bookModel.getId();
                    mFavouriteBooksDatabase.child(currentUser.getUid()).child(id).removeValue();
                    ((ImageView) holder.itemView.findViewById(R.id.iv_heart_icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.heart_icon, context.getApplicationContext().getTheme()));
                }
            }
        });


        if(bookModel.getVideo_path()=="null" || bookModel.getVideo_path()=="" || bookModel.getVideo_path()==null)
            holder.itemView.findViewById(R.id.youtube_player).setVisibility(View.GONE);
        else{
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

    }

    @Override
    public int getItemCount() {
        return choicesList.size();
    }
}