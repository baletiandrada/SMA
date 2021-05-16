package com.example.booksapp.dataModels;

public class AppreciateBookModel implements Comparable<AppreciateBookModel>{
    private String user_id, book_id, content, id, rating, review_id;

    public AppreciateBookModel(){

    }

    public AppreciateBookModel(String user_id, String book_id, String content) {
        this.user_id = user_id;
        this.book_id = book_id;
        this.content = content;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
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

    @Override
    public int compareTo(AppreciateBookModel o) {
        return this.getRating().compareTo(o.getRating());
    }
}
