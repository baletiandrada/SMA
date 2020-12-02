package com.example.secondapp_recyclerview;

public class BookReadData {
    private String author_name, title, read_year, id;

    public BookReadData(String author_name, String title, String read_year) {
        this.author_name = author_name;
        this.title = title;
        this.read_year = read_year;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRead_year() {
        return read_year;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public void setRead_year(String read_year) {
        this.read_year = read_year;
    }
}
