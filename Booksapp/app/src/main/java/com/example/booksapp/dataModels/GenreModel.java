package com.example.booksapp.dataModels;

import com.example.booksapp.helpers.GenreStorageHelper;

public class GenreModel {
    private String name, src;
    public GenreModel(String name, String src){
        this.name = name;
        this.src = src;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
