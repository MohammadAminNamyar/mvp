package com.example.squarespool.dto;

public class AdminSettingsRequest {
    private boolean showPurchaserNames;

    public boolean isShowPurchaserNames() {
        return showPurchaserNames;
    }

    public void setShowPurchaserNames(boolean showPurchaserNames) {
        this.showPurchaserNames = showPurchaserNames;
    }
}
