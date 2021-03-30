package com.example.booksapp.helpers;

public class ReviewStorageHelper {
    private String user_id, book_id, content, author_name, book_title, rating;
    private static ReviewStorageHelper instance;

    private ReviewStorageHelper(){
    }

    public static ReviewStorageHelper getInstance(){
        if(instance==null)
            instance = new ReviewStorageHelper();
        return instance;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getBook_title() {
        return book_title;
    }

    public void setBook_title(String book_title) {
        this.book_title = book_title;
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
