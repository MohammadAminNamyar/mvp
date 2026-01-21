package com.example.squarespool.dto;

public class AdminSettingsResponse {
    private boolean showPurchaserNames;
    private boolean rolloverOnNoWinner;

    public AdminSettingsResponse() {
    }

    public AdminSettingsResponse(boolean showPurchaserNames, boolean rolloverOnNoWinner) {
        this.showPurchaserNames = showPurchaserNames;
        this.rolloverOnNoWinner = rolloverOnNoWinner;
    }

    public boolean isShowPurchaserNames() {
        return showPurchaserNames;
    }

    public void setShowPurchaserNames(boolean showPurchaserNames) {
        this.showPurchaserNames = showPurchaserNames;
    }

    public boolean isRolloverOnNoWinner() {
        return rolloverOnNoWinner;
    }

    public void setRolloverOnNoWinner(boolean rolloverOnNoWinner) {
        this.rolloverOnNoWinner = rolloverOnNoWinner;
    }
}
