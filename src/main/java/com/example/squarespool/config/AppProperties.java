package com.example.squarespool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "squares")
public class AppProperties {
    private int reservationTtlSeconds = 30;
    private String adminToken = "admin";
    private boolean houseOnLock = true;
    private boolean showPurchaserNames = false;

    public int getReservationTtlSeconds() {
        return reservationTtlSeconds;
    }

    public void setReservationTtlSeconds(int reservationTtlSeconds) {
        this.reservationTtlSeconds = reservationTtlSeconds;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }

    public boolean isHouseOnLock() {
        return houseOnLock;
    }

    public void setHouseOnLock(boolean houseOnLock) {
        this.houseOnLock = houseOnLock;
    }

    public boolean isShowPurchaserNames() {
        return showPurchaserNames;
    }

    public void setShowPurchaserNames(boolean showPurchaserNames) {
        this.showPurchaserNames = showPurchaserNames;
    }
}
