package com.example.squarespool.dto;

public class AdminSettingsRequest {
    private boolean showPurchaserNames;
    private boolean rolloverOnNoWinner;

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
