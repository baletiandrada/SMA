package com.example.booksapp.helpers;

import com.example.booksapp.dataModels.BookReadData;

import java.util.List;

public class BookListStorageHelper {
    private List<BookReadData> books_read_list;
    private List<BookReadData> books_planned_list;
    private List<BookReadData> books_recommended_list;
    private static BookListStorageHelper instance;

    private BookListStorageHelper(){

    }

    public static BookListStorageHelper getInstance(){
        if(instance==null)
            instance = new BookListStorageHelper();
        return instance;
    }

    public List<BookReadData> getBooks_read_list() {
        return books_read_list;
    }

    public void setBooks_read_list(List<BookReadData> books_read_list) {
        this.books_read_list = books_read_list;
    }

    public List<BookReadData> getBooks_planned_list() {
        return books_planned_list;
    }

    public void setBooks_planned_list(List<BookReadData> books_planned_list) {
        this.books_planned_list = books_planned_list;
    }

    public List<BookReadData> getBooks_recommended_list() {
        return books_recommended_list;
    }

    public void setBooks_recommended_list(List<BookReadData> books_recommended_list) {
        this.books_recommended_list = books_recommended_list;
    }
}
