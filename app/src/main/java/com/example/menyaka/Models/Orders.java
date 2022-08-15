package com.example.menyaka.Models;

public class Orders {
    String receiptID, userID, totalItems, deliveryCharges, subTotal, status, storeID, timeStamp, deliveryAddress, paymentMethod;
    long totalPrice;

    public Orders() {
    }

    public Orders(String receiptID, String userID, String totalItems, String deliveryCharges, String subTotal, String status, String storeID, String timeStamp, String deliveryAddress, String paymentMethod, long totalPrice) {
        this.receiptID = receiptID;
        this.userID = userID;
        this.totalItems = totalItems;
        this.deliveryCharges = deliveryCharges;
        this.subTotal = subTotal;
        this.status = status;
        this.storeID = storeID;
        this.timeStamp = timeStamp;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getStoreID() {
        return storeID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiptID() {
        return receiptID;
    }

    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public String getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(String deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }
}
