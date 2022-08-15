package com.example.menyaka.Models;

public class PhoneContacts {

    private String name, phoneNumber, profileImg, userId;

    public PhoneContacts() {
    }

    public PhoneContacts(String name, String phoneNumber, String profileImg, String userId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImg = profileImg;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
