package com.example.booksapp.dataModels;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class BookReadData {
    private String author_name, title, read_month, read_year, id, genre, video_path, uri, description, id_from_big_db;
    private YouTubePlayerView youTubePlayer;

    public BookReadData(){

    }

    public String getId_from_big_db() {
        return id_from_big_db;
    }

    public void setId_from_big_db(String id_from_big_db) {
        this.id_from_big_db = id_from_big_db;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookReadData(String author_name, String title, String read_month, String read_year) {
        this.author_name = author_name;
        this.title = title;
        this.read_month = read_month;
        this.read_year = read_year;
    }

    public BookReadData(String author_name, String title, String read_month, String read_year, String uri) {
        this.author_name = author_name;
        this.title = title;
        this.read_month = read_month;
        this.read_year = read_year;
        this.uri = uri;
    }

    public BookReadData(String author_name, String title) {
        this.author_name = author_name;
        this.title = title;
    }

    public BookReadData(String author_name, String title, String genre) {
        this.author_name = author_name;
        this.title = title;
        this.genre = genre;
    }

    public void setBookReadData(String author_name, String title, String genre, String uri){
        this.author_name = author_name;
        this.title = title;
        this.genre = genre;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
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

    public String getRead_month() {
        return read_month;
    }

    public void setRead_month(String read_month) {
        this.read_month = read_month;
    }

    public void setRead_year(String read_year) {
        this.read_year = read_year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public YouTubePlayerView getYouTubePlayer() {
        return youTubePlayer;
    }

    public void setYouTubePlayer(YouTubePlayerView youTubePlayer) {
        this.youTubePlayer = youTubePlayer;
    }

}
