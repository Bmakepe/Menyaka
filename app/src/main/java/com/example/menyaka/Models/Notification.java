package com.example.menyaka.Models;

public class Notification {
    private String userid, text, postid, timeStamp, notificationID;
    private boolean ispost;

    public Notification() {
    }

    public Notification(String userid, String text, String postid, String timeStamp, String notificationID, boolean ispost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.timeStamp = timeStamp;
        this.notificationID = notificationID;
        this.ispost = ispost;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }
}
