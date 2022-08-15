package com.example.menyaka.Models;

public class Contact {
    private String username, lastText, imageUrl, id, timestamp;

    public Contact(String username, String lastText, String imageUrl, String id, String timestamp) {
        this.username = username;
        this.lastText = lastText;
        this.imageUrl = imageUrl;
        this.id = id;
        this.timestamp = timestamp;
    }

    public Contact() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastText() {
        return lastText;
    }

    public void setLastText(String lastText) {
        this.lastText = lastText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

