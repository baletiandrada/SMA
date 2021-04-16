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
import com.example.booksapp.R;
import com.example.booksapp.BookReviewsActivity;
import com.example.booksapp.VideoPopUpActivity;
import com.example.booksapp.dataModels.BookReadData;
import com.example.booksapp.helpers.BookStorageHelper;
import com.example.booksapp.helpers.AppreciateBookStorageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import static com.example.booksapp.AppConstants.ADD_REVIEW_ENABLED;
import static com.example.booksapp.helpers.FirebaseHelper.mBooksRecommendedDatabase;
import static com.example.booksapp.helpers.FirebaseHelper.mFavouriteBooksDatabase;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

public class BooksRecommendedAdapter extends RecyclerView.Adapter<BookReadDataViewHolder>{
    private List<BookReadData> choicesList;
    private Context context;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    BookStorageHelper bookStorageHelper = BookStorageHelper.getInstance();

    public BooksRecommendedAdapter(List<BookReadData> bookList){
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
        BookReadData bookModel = choicesList.get(position);
        holder.itemView.findViewById(R.id.tv_row_read_month).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.tv_row_read_year).setVisibility(View.GONE);
        //holder.itemView.findViewById(R.id.iv_delete_just_image).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.layout_done_text).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_for_description).setVisibility(View.VISIBLE);
        holder.setThreeValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getGenre());
        if(bookModel.getDescription()!=null)
            holder.setDescription(bookModel.getDescription());
        if(!(bookModel.getUri()==null) && !bookModel.getUri().isEmpty() && !bookModel.getUri().equals("null"))
            Glide.with(context).load(bookModel.getUri()).placeholder(R.mipmap.ic_launcher).into(holder.iv_book_image);

        if(!currentUser.getEmail().equals("admin@gmail.com")){
            holder.itemView.findViewById(R.id.iv_delete_image).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.iv_edit_icon).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.layout_edit_delete_icons).setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.tv_see_reviews).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.tv_see_reviews).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookReviewsActivity.class);
                AppreciateBookStorageHelper appreciateBookStorageHelper = AppreciateBookStorageHelper.getInstance();
                appreciateBookStorageHelper.setUser_id(currentUser.getUid());
                appreciateBookStorageHelper.setBook_id(bookModel.getId());
                appreciateBookStorageHelper.setAuthor_name(bookModel.getAuthor_name());
                appreciateBookStorageHelper.setBook_title(bookModel.getTitle());
                intent.putExtra(ADD_REVIEW_ENABLED, "NO");
                context.startActivity(intent);
            }
        });

        //rating UI
        holder.itemView.findViewById(R.id.layout_users_rating).setVisibility(View.VISIBLE);
        holder.itemView.findViewById(R.id.iv_user_rating_colored).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_user_rating_discolored).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.tv_rating_user_score).setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.GONE);
        if(!AppConstants.MEAN_RATING_RECOMM_FRAG.get(position).equals("0")){
            holder.mean_rating.setText(AppConstants.MEAN_RATING_RECOMM_FRAG.get(position) + "/5");
            holder.itemView.findViewById(R.id.iv_users_rating_discolored).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.iv_users_rating_colored).setVisibility(View.VISIBLE);
        }


        holder.itemView.findViewById(R.id.iv_delete_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this book?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!(choicesList==null)){
                                    Toast.makeText(context,"Book deleted successfully", Toast.LENGTH_SHORT).show();
                                    if(currentUser!=null) {
                                        mBooksRecommendedDatabase.child(bookModel.getId()).removeValue();
                                        mFavouriteBooksDatabase.child(bookModel.getId()).removeValue();
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
                Toast.makeText(context, bookStorageHelper.getId_book(), Toast.LENGTH_LONG).show();
                bookStorageHelper.setId_book(bookModel.getId());
                bookStorageHelper.setThreeValues(bookModel.getAuthor_name(), bookModel.getTitle(), bookModel.getGenre());
                Intent intent = new Intent(context, EditBookActivity.class);
                String param_bookTable_value = "Recommended books";
                //Toast.makeText(context, bookStorageHelper.getId_book(), Toast.LENGTH_LONG).show();
                intent.putExtra(AppConstants.param_bookTable, param_bookTable_value);
                context.startActivity(intent);
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

        holder.itemView.findViewById(R.id.iv_for_description).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                holder.itemView.findViewById(R.id.iv_for_description).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.tv_description).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.iv_hide_description).setVisibility(View.VISIBLE);

            }
        });

        holder.itemView.findViewById(R.id.iv_hide_description).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.itemView.findViewById(R.id.iv_for_description).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.tv_description).setVisibility(View.GONE);
                holder.itemView.findViewById(R.id.iv_hide_description).setVisibility(View.GONE);
            }
        });

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
}
