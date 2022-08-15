package com.example.menyaka.Models;

public class Sales {

    String rating, salesMessage, retailName, retail_id, salesComment, salesId, salesItem, salesPrice, seen, timestamp;

    public Sales(String rating, String salesMessage, String retailName, String retail_id, String salesComment, String salesId, String salesItem, String salesPrice, String seen, String timestamp) {
        this.rating = rating;
        this.salesMessage = salesMessage;
        this.retailName = retailName;
        this.retail_id = retail_id;
        this.salesComment = salesComment;
        this.salesId = salesId;
        this.salesItem = salesItem;
        this.salesPrice = salesPrice;
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public Sales() {
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSalesMessage() {
        return salesMessage;
    }

    public void setSalesMessage(String salesMessage) {
        this.salesMessage = salesMessage;
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

    public String getSalesComment() {
        return salesComment;
    }

    public void setSalesComment(String salesComment) {
        this.salesComment = salesComment;
    }

    public String getSalesId() {
        return salesId;
    }

    public void setSalesId(String salesId) {
        this.salesId = salesId;
    }

    public String getSalesItem() {
        return salesItem;
    }

    public void setSalesItem(String salesItem) {
        this.salesItem = salesItem;
    }

    public String getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(String salesPrice) {
        this.salesPrice = salesPrice;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
