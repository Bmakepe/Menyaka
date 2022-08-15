package com.example.menyaka.Models;

import java.util.List;

public class Address {
    String addressID, userID, deliveryName, deliveryNumber, deliveryAddress, defaultAddress, deliveryHouse, deliveryRoad, deliveryNeighbourHood, deliveryDistrict, deliveryZipCode;

    public Address() {
    }

    public Address(String addressID, String userID, String deliveryName, String deliveryNumber, String deliveryAddress, String defaultAddress, String deliveryHouse, String deliveryRoad, String deliveryNeighbourHood, String deliveryDistrict, String deliveryZipCode) {
        this.addressID = addressID;
        this.userID = userID;
        this.deliveryName = deliveryName;
        this.deliveryNumber = deliveryNumber;
        this.deliveryAddress = deliveryAddress;
        this.defaultAddress = defaultAddress;
        this.deliveryHouse = deliveryHouse;
        this.deliveryRoad = deliveryRoad;
        this.deliveryNeighbourHood = deliveryNeighbourHood;
        this.deliveryDistrict = deliveryDistrict;
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getDeliveryHouse() {
        return deliveryHouse;
    }

    public void setDeliveryHouse(String deliveryHouse) {
        this.deliveryHouse = deliveryHouse;
    }

    public String getDeliveryRoad() {
        return deliveryRoad;
    }

    public void setDeliveryRoad(String deliveryRoad) {
        this.deliveryRoad = deliveryRoad;
    }

    public String getDeliveryNeighbourHood() {
        return deliveryNeighbourHood;
    }

    public void setDeliveryNeighbourHood(String deliveryNeighbourHood) {
        this.deliveryNeighbourHood = deliveryNeighbourHood;
    }

    public String getDeliveryDistrict() {
        return deliveryDistrict;
    }

    public void setDeliveryDistrict(String deliveryDistrict) {
        this.deliveryDistrict = deliveryDistrict;
    }

    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }
}
