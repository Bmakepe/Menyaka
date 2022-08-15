package com.example.menyaka.Models;

public class ShopReview {
    String shopID, reviewOwnerID, reviewText;

    public ShopReview() {
    }

    public ShopReview(String shopID, String reviewOwnerID, String reviewText) {
        this.shopID = shopID;
        this.reviewOwnerID = reviewOwnerID;
        this.reviewText = reviewText;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getReviewOwnerID() {
        return reviewOwnerID;
    }

    public void setReviewOwnerID(String reviewOwnerID) {
        this.reviewOwnerID = reviewOwnerID;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
