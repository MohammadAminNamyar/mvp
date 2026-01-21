package com.example.squarespool.dto;

public class AdminSettingsResponse {
    private boolean showPurchaserNames;

    public AdminSettingsResponse() {
    }

    public AdminSettingsResponse(boolean showPurchaserNames) {
        this.showPurchaserNames = showPurchaserNames;
    }

    public boolean isShowPurchaserNames() {
        return showPurchaserNames;
    }

    public void setShowPurchaserNames(boolean showPurchaserNames) {
        this.showPurchaserNames = showPurchaserNames;
    }
}
