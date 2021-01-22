package com.example.booksapp.dataModels;

public class QuoteModel {
    String text_quote, id;
    public QuoteModel(String text_quote){
        this.text_quote = text_quote;
    }

    public String getText_quote() {
        return text_quote;
    }

    public void setText_quote(String textQuote) {
        this.text_quote = text_quote;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
