package com.example.menyaka.Models;

public class Merchants {
    String shopID, merchantName, merchantNumber, merchantType;

    public Merchants(String shopID, String merchantName, String merchantNumber, String merchantType) {
        this.shopID = shopID;
        this.merchantName = merchantName;
        this.merchantNumber = merchantNumber;
        this.merchantType = merchantType;
    }

    public Merchants() {
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getShopID() {
        return shopID;
    }

    public void setShopID(String shopID) {
        this.shopID = shopID;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantNumber() {
        return merchantNumber;
    }

    public void setMerchantNumber(String merchantNumber) {
        this.merchantNumber = merchantNumber;
    }
}
