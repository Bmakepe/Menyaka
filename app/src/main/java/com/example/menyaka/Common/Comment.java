package com.example.menyaka.Common;

public class Comment {

    private String commentID, comment, publisher, timeStamp;

    public Comment(String commentID, String comment, String publisher, String timeStamp) {
        this.commentID = commentID;
        this.comment = comment;
        this.publisher = publisher;
        this.timeStamp = timeStamp;
    }

    public Comment() {
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
