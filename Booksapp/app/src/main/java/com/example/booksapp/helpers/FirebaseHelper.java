package com.example.booksapp.helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    public static final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    public static final DatabaseReference mBooksReadDatabase = FirebaseDatabase.getInstance().getReference().child("Books_read");
    public static final DatabaseReference mBooksPlannedDatabase = FirebaseDatabase.getInstance().getReference().child("Books_planned");
    public static final DatabaseReference mBooksRecommendedDatabase = FirebaseDatabase.getInstance().getReference().child("Books_recommended");
    public static final DatabaseReference mBookGenresDatabase = FirebaseDatabase.getInstance().getReference().child("Book_genres");
    public static final DatabaseReference mFavouriteBooksDatabase = FirebaseDatabase.getInstance().getReference().child("Favourite_books");
    public static final DatabaseReference mQuotesDatabase = FirebaseDatabase.getInstance().getReference().child("Quotes");
    public static final DatabaseReference mImagesDatabase = FirebaseDatabase.getInstance().getReference().child("Images");
    public static final DatabaseReference mReviewsDatabase = FirebaseDatabase.getInstance().getReference().child("Reviews");
    public static final DatabaseReference mRatingsDatabase = FirebaseDatabase.getInstance().getReference().child("Ratings");
    public static final DatabaseReference mLikesDatabase = FirebaseDatabase.getInstance().getReference().child("Likes");
    public static final DatabaseReference mRepliesDatabase = FirebaseDatabase.getInstance().getReference().child("Replies");
}
