package com.example.booksapp;

import com.example.booksapp.dataModels.BookReadData;

import java.util.ArrayList;

public class AppConstants {
    public static final String MY_PREFS_NAME = "prefs_name", EMAIL = "user_mail";
    public static final String param_bookTable = "", VIDEO_PATH = "", PARAM_ADD_BOOK_TABLE="";
    public static final ArrayList<BookReadData> BOOKS_ARRAY = new ArrayList<BookReadData>();
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

}
