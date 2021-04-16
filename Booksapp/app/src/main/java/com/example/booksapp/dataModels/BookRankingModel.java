package com.example.booksapp.dataModels;

public class BookRankingModel implements Comparable<BookRankingModel>{
    private String book_id;
    private double ranking_score;

    public BookRankingModel(String book_id, double ranking_score) {
        this.book_id = book_id;
        this.ranking_score = ranking_score;
    }

    public double getRanking_score() {
        return ranking_score;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setRanking_score(double ranking_score) {
        this.ranking_score = ranking_score;
    }

    @Override
    public int compareTo(BookRankingModel o) {
        return String.valueOf(getRanking_score()).compareTo(String.valueOf(o.getRanking_score()));
    }
}
