package com.example.menyaka.Models;

public class CartItems {
    private String productID, orderQuantity, productSize, userID, storeID, orderNo, purchaseMethod, status, productPrice, deliveryAddress, totalPrice, receiptID, timestamp;

    public CartItems() {
    }

    public CartItems(String productID, String orderQuantity, String productSize, String userID, String storeID, String orderNo, String purchaseMethod, String status, String productPrice, String deliveryAddress, String totalPrice, String receiptID, String timestamp) {
        this.productID = productID;
        this.orderQuantity = orderQuantity;
        this.productSize = productSize;
        this.userID = userID;
        this.storeID = storeID;
        this.orderNo = orderNo;
        this.purchaseMethod = purchaseMethod;
        this.status = status;
        this.productPrice = productPrice;
        this.deliveryAddress = deliveryAddress;
        this.totalPrice = totalPrice;
        this.receiptID = receiptID;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReceiptID() {
        return receiptID;
    }

    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurchaseMethod() {
        return purchaseMethod;
    }

    public void setPurchaseMethod(String purchaseMethod) {
        this.purchaseMethod = purchaseMethod;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(String orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getProductSize() {
        return productSize;
    }

    public void setProductSize(String productSize) {
        this.productSize = productSize;
    }
}
