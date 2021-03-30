package com.example.booksapp.dataModels;

import java.util.ArrayList;

public class ReviewModel {
    private String user_id, book_id, content, id, rating;

    public ReviewModel(){

    }

    public ReviewModel(String user_id, String book_id, String content) {
        this.user_id = user_id;
        this.book_id = book_id;
        this.content = content;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
