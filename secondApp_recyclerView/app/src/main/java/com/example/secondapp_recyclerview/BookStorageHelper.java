package com.example.secondapp_recyclerview;

public class BookStorageHelper {
    private String author_name, book_title, read_year, id_book;
    private static BookStorageHelper instance;

    private BookStorageHelper(){

    }

    public static BookStorageHelper getInstance(){
        if(instance==null)
            instance = new BookStorageHelper();
        return instance;
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

    public String getRead_year() {
        return read_year;
    }

    public void setRead_year(String read_year) {
        this.read_year = read_year;
    }

    public String getId_book() {
        return id_book;
    }

    public void setId_book(String id_book) {
        this.id_book = id_book;
    }

    public void setValues(String author_name, String book_title, String read_year){
        this.author_name = author_name;
        this.book_title = book_title;
        this.read_year = read_year;
    }
}
