package com.example.menyaka.Models;

public class RatingAndReview {

    String comment, publisherId, timestamp, reviewID, message, userID;
    float rating;

    public RatingAndReview(String comment, String publisherId, String timestamp, String reviewID, String message, String userID, float rating) {
        this.comment = comment;
        this.publisherId = publisherId;
        this.timestamp = timestamp;
        this.reviewID = reviewID;
        this.message = message;
        this.userID = userID;
        this.rating = rating;
    }

    public RatingAndReview() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
