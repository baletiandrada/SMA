package com.example.booksapp.helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    public static final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    public static final DatabaseReference mBooksReadDatabase = FirebaseDatabase.getInstance().getReference().child("Books_read");
    public static final DatabaseReference mBooksPlannedDatabase = FirebaseDatabase.getInstance().getReference().child("Books_planned");
    public static final DatabaseReference mBooksRecommendedDatabase = FirebaseDatabase.getInstance().getReference().child("Books_recommended");
    public static final DatabaseReference mImagesDatabase = FirebaseDatabase.getInstance().getReference().child("Images");
}