package com.example.menyaka.Models;

public class HotDeal {

    private String hotDeal_id, storeId, itemName, discount, itemPrice, itemUrl, views,
            storeName, dealEnd;

    public HotDeal(String hotDeal_id, String storeId, String itemName, String discount, String itemPrice, String itemUrl, String views, String storeName, String dealEnd) {
        this.hotDeal_id = hotDeal_id;
        this.storeId = storeId;
        this.itemName = itemName;
        this.discount = discount;
        this.itemPrice = itemPrice;
        this.itemUrl = itemUrl;
        this.views = views;
        this.storeName = storeName;
        this.dealEnd = dealEnd;
    }

    public HotDeal() {
    }

    public String getHotDeal_id() {
        return hotDeal_id;
    }

    public void setHotDeal_id(String hotDeal_id) {
        this.hotDeal_id = hotDeal_id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getDealEnd() {
        return dealEnd;
    }

    public void setDealEnd(String dealEnd) {
        this.dealEnd = dealEnd;
    }
}
