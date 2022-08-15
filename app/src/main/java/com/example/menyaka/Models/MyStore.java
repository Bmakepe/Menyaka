package com.example.menyaka.Models;

public class MyStore {

    private String category, enableBooking, imageUrl, points, ratingStatus, retailName, retail_id, storeId, userId;
    private float rating;

    public MyStore(String category, String enableBooking, String imageUrl, String points, String ratingStatus, String retailName, String retail_id, String storeId, String userId, float rating) {
        this.category = category;
        this.enableBooking = enableBooking;
        this.imageUrl = imageUrl;
        this.points = points;
        this.ratingStatus = ratingStatus;
        this.retailName = retailName;
        this.retail_id = retail_id;
        this.storeId = storeId;
        this.userId = userId;
        this.rating = rating;
    }

    public MyStore() {
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEnableBooking() {
        return enableBooking;
    }

    public void setEnableBooking(String enableBooking) {
        this.enableBooking = enableBooking;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getRatingStatus() {
        return ratingStatus;
    }

    public void setRatingStatus(String ratingStatus) {
        this.ratingStatus = ratingStatus;
    }

    public String getRetailName() {
        return retailName;
    }

    public void setRetailName(String retailName) {
        this.retailName = retailName;
    }

    public String getRetail_id() {
        return retail_id;
    }

    public void setRetail_id(String retail_id) {
        this.retail_id = retail_id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
