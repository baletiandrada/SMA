package com.example.booksapp.helpers;

import java.util.ArrayList;

public class GenreStorageHelper {
    private static GenreStorageHelper instance;
    private ArrayList<String> genresReadAndPlanned = new ArrayList<String>();

    private GenreStorageHelper(){
    }

    public static GenreStorageHelper getInstance(){
        if(instance==null)
            instance = new GenreStorageHelper();
        return instance;
    }

    public ArrayList<String> getGenresReadAndPlanned() {
        return genresReadAndPlanned;
    }

    public void setGenresReadAndPlannede(ArrayList<String> genresReadAndPlanned) {
        this.genresReadAndPlanned = genresReadAndPlanned;
    }
}
