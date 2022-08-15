package com.example.menyaka.Models;

public class BannerAds {
    String bannerID, bannerIMG, endDate, retail_ID, startDate, ad_type, bannerMedia;

    public BannerAds(String bannerID, String bannerIMG, String endDate, String retail_ID, String startDate, String ad_type, String bannerMedia) {
        this.bannerID = bannerID;
        this.bannerIMG = bannerIMG;
        this.endDate = endDate;
        this.retail_ID = retail_ID;
        this.startDate = startDate;
        this.ad_type = ad_type;
        this.bannerMedia = bannerMedia;
    }

    public BannerAds() {
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public String getBannerMedia() {
        return bannerMedia;
    }

    public void setBannerMedia(String bannerMedia) {
        this.bannerMedia = bannerMedia;
    }

    public String getBannerID() {
        return bannerID;
    }

    public void setBannerID(String bannerID) {
        this.bannerID = bannerID;
    }

    public String getBannerIMG() {
        return bannerIMG;
    }

    public void setBannerIMG(String bannerIMG) {
        this.bannerIMG = bannerIMG;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRetail_ID() {
        return retail_ID;
    }

    public void setRetail_ID(String retail_ID) {
        this.retail_ID = retail_ID;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
