package com.example.booksapp.dataModels;

public class WordModel implements Comparable<WordModel>{
    String word;
    int levenshtein_score;

    public WordModel(String word, int levenshtein_score) {
        this.word = word;
        this.levenshtein_score = levenshtein_score;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getLevenshtein_score() {
        return levenshtein_score;
    }

    public void setLevenshtein_score(int levenshtein_score) {
        this.levenshtein_score = levenshtein_score;
    }


    @Override
    public int compareTo(WordModel o) {
        return String.valueOf(getLevenshtein_score()).compareTo(String.valueOf(o.getLevenshtein_score()));
    }
}
