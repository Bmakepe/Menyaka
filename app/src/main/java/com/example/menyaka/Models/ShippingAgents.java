package com.example.menyaka.Models;

public class ShippingAgents {
    String category, companyDescription, companyID, companyLogo, shippingName, shipperLocation;
    private float rating;
    private long deliveryFee;
    Boolean isSelected, verified;

    public ShippingAgents(String category, String companyDescription, String companyID, String companyLogo, String shippingName, String shipperLocation, float rating, long deliveryFee, Boolean isSelected, Boolean verified) {
        this.category = category;
        this.companyDescription = companyDescription;
        this.companyID = companyID;
        this.companyLogo = companyLogo;
        this.shippingName = shippingName;
        this.shipperLocation = shipperLocation;
        this.rating = rating;
        this.deliveryFee = deliveryFee;
        this.isSelected = isSelected;
        this.verified = verified;
    }

    public String getShipperLocation() {
        return shipperLocation;
    }

    public void setShipperLocation(String shipperLocation) {
        this.shipperLocation = shipperLocation;
    }

    public ShippingAgents() {
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(long deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }

    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}
