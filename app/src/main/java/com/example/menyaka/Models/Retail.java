package com.example.menyaka.Models;

public class Retail {

    private String imageUrl, retail_id, category, retailName, userId, retailDescription;
    private float rating;
    private boolean verified;

    public Retail(String imageUrl, String retail_id, String category, String retailName, String userId, String retailDescription, float rating, boolean verified) {
        this.imageUrl = imageUrl;
        this.retail_id = retail_id;
        this.category = category;
        this.retailName = retailName;
        this.userId = userId;
        this.retailDescription = retailDescription;
        this.rating = rating;
        this.verified = verified;
    }

    public Retail() {
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRetail_id() {
        return retail_id;
    }

    public void setRetail_id(String retail_id) {
        this.retail_id = retail_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRetailName() {
        return retailName;
    }

    public void setRetailName(String retailName) {
        this.retailName = retailName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRetailDescription() {
        return retailDescription;
    }

    public void setRetailDescription(String retailDescription) {
        this.retailDescription = retailDescription;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
