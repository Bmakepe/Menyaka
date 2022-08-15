package com.example.menyaka.Models;

public class Moment {

    private String event_place, moment_title, username, moment_desc, imageUrl, key, momentId,
            timestamp, seen_number, location, privacy, videoUrl, postType, sharedPost;
    private double longitude, latitude;

    public Moment() {
    }

    public Moment(String event_place, String moment_title, String username, String moment_desc, String imageUrl, String key, String momentId, String timestamp, String seen_number, String location,
                  String privacy, String videoUrl, String postType, String sharedPost, double longitude, double latitude) {
        this.event_place = event_place;
        this.moment_title = moment_title;
        this.username = username;
        this.moment_desc = moment_desc;
        this.imageUrl = imageUrl;
        this.key = key;
        this.momentId = momentId;
        this.timestamp = timestamp;
        this.seen_number = seen_number;
        this.location = location;
        this.privacy = privacy;
        this.videoUrl = videoUrl;
        this.postType = postType;
        this.sharedPost = sharedPost;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getSharedPost() {
        return sharedPost;
    }

    public void setSharedPost(String sharedPost) {
        this.sharedPost = sharedPost;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEvent_place() {
        return event_place;
    }

    public void setEvent_place(String event_place) {
        this.event_place = event_place;
    }

    public String getMoment_title() {
        return moment_title;
    }

    public void setMoment_title(String moment_title) {
        this.moment_title = moment_title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMoment_desc() {
        return moment_desc;
    }

    public void setMoment_desc(String moment_desc) {
        this.moment_desc = moment_desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMomentId() {
        return momentId;
    }

    public void setMomentId(String momentId) {
        this.momentId = momentId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeen_number() {
        return seen_number;
    }

    public void setSeen_number(String seen_number) {
        this.seen_number = seen_number;
    }
}