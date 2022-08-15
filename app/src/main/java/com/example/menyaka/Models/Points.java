package com.example.menyaka.Models;

public class Points {
    String shopID, userID, pointsID, pointsNo, subscriptionDate, expiryDate;

    public Points() {
    }

    public Points(String shopID, String userID, String pointsID, String pointsNo, String subscriptionDate, String expiryDate) {
        this.shopID = shopID;
        this.userID = userID;
        this.pointsID = pointsID;
        this.pointsNo = pointsNo;
        this.subscriptionDate = subscriptionDate;
        this.expiryDate = expiryDate;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPointsID() {
        return pointsID;
    }

    public void setPointsID(String pointsID) {
        this.pointsID = pointsID;
    }

    public String getPointsNo() {
        return pointsNo;
    }

    public void setPointsNo(String pointsNo) {
        this.pointsNo = pointsNo;
    }

    public String getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(String subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
