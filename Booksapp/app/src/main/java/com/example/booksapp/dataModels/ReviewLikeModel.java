package com.example.booksapp.dataModels;

public class ReviewLikeModel implements Comparable<ReviewLikeModel>{
    private String review_id, appreciation, id, user_id, like_score;

    public ReviewLikeModel() {
    }

    public ReviewLikeModel(String review_id, String appreciation) {
        this.review_id = review_id;
        this.appreciation = appreciation;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
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

    public String getLike_score() {
        return like_score;
    }

    public void setLike_score(String like_score) {
        this.like_score = like_score;
    }

    @Override
    public int compareTo(ReviewLikeModel o) {
        return this.getLike_score().compareTo(o.getLike_score());
    }
}
