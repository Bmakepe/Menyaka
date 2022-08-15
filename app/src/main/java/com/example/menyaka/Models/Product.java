package com.example.menyaka.Models;

public class Product {

    String productName, price, productImg, productSize, productCategory, productId, orderQuantity, storeId, productDepartment;

    public Product(String productName, String price, String productImg, String productSize, String productCategory, String productId, String orderQuantity, String storeId, String productDepartment) {
        this.productName = productName;
        this.price = price;
        this.productImg = productImg;
        this.productSize = productSize;
        this.productCategory = productCategory;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
        this.storeId = storeId;
        this.productDepartment = productDepartment;
    }

    public Product() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProductImg() {
        return productImg;
    }

    public void setProductImg(String productImg) {
        this.productImg = productImg;
    }

    public String getProductSize() {
        return productSize;
    }

    public void setProductSize(String productSize) {
        this.productSize = productSize;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(String orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProductDepartment() {
        return productDepartment;
    }

    public void setProductDepartment(String productDepartment) {
        this.productDepartment = productDepartment;
    }
}
