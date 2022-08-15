package com.example.menyaka.Models;

public class Promotion {

    private String location, promo_desc, imageUrl, key, promo_id, timestamp;

    public Promotion(String location, String promo_desc, String imageUrl, String key, String promo_id, String timestamp) {
        this.location = location;
        this.promo_desc = promo_desc;
        this.imageUrl = imageUrl;
        this.key = key;
        this.promo_id = promo_id;
        this.timestamp = timestamp;
    }

    public Promotion() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPromo_desc() {
        return promo_desc;
    }

    public void setPromo_desc(String promo_desc) {
        this.promo_desc = promo_desc;
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

    public String getPromo_id() {
        return promo_id;
    }

    public void setPromo_id(String promo_id) {
        this.promo_id = promo_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
