package com.example.booksapp;

import com.example.booksapp.dataModels.BookData;

import java.util.ArrayList;

public class AppConstants {
    public static final String MY_PREFS_NAME = "prefs_name", EMAIL = "user_mail";
    public static final String PARAM_EDIT_BOOK_TABLE = "", VIDEO_PATH = "", PARAM_ADD_BOOK_TABLE="";
    public static final ArrayList<BookData> BOOKS_ARRAY = new ArrayList<BookData>();
    public static final ArrayList<String> IMG_CAME_FROM = new ArrayList<String>();
    public static final ArrayList<String> IMG_PLAN_CAME_FROM = new ArrayList<String>();
    public static final ArrayList<String> BookExistsInFav = new ArrayList<String>();
    public static final String[] commonRomanianWords= new String[]{
        ""
    };

    public static final String[] commonEnglishWords= new String[]{
            ""
    };

    public static final String[] MONTHS= new String[]{
            "ianuarie", "februarie", "martie", "aprilie", "mai", "iunie",
            "iulie", "august", "septembrie", "octombrie", "noiembrie", "decembrie"
    };

    public static final String ADD_REVIEW_ENABLED="add_review";
    public static final ArrayList<String> USER_RATING=new ArrayList<String>(), MEAN_RATING=new ArrayList<String>(), MEAN_RATING_PLAN_FRAG=new ArrayList<String>();
    public static final ArrayList<String> MEAN_RATING_RECOMM_FRAG = new ArrayList<String>();

    public static final ArrayList<String> BOOK_ID_LIST_READ = new ArrayList<>(), BOOK_ID_LIST_PLAN = new ArrayList<>();
    public static final ArrayList<String> USER_GMAIL_LIST = new ArrayList<>();
    public static final ArrayList<String> NUMBER_OF_LIKES = new ArrayList<>();
    public static final ArrayList<String> NUMBER_OF_DISLIKES = new ArrayList<>();
    public static final ArrayList<String> REVIEW_APPRECIATION_FROM_CURRENT_USER = new ArrayList<>(), REPLY_EXISTS_IN_LIKE_LIST= new ArrayList<>();
    public static final ArrayList<String> REVIEW_EXISTS_IN_LIKE_LIST = new ArrayList<>(), REPLY_APPRECIATION_FROM_CURRENT_USER = new ArrayList<>();
    public static final ArrayList<String> ID_LIKE_FOR_CURRENT_USER = new ArrayList<>();
    public static String REVIEW_ID="";
    public static String EMAIL_FROM_REVIEW="";
    public static String CONTENT_REVIEW="";
    public static String enable_add_review="";
    public static ArrayList<String> NUMBER_OF_REPLIES = new ArrayList<>();
    public static int INDEX_FOR_GETTING_NO_OF_LIKES;

}
