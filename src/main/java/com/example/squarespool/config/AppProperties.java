package com.example.squarespool.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "squares")
public class AppProperties {
  private int reservationTtlSeconds = 30;
  private String adminToken = "admin";
  private boolean houseOnLock = true;
  private boolean showPurchaserNames = false;
  private boolean authEnabled = false;
  private String authUserHeader = "X-User-Id";
  private boolean rolloverOnNoWinner = true;
  private List<Integer> betAmountsCents = List.of(200, 500, 1000);
  private String fixturesBaseUrl = "http://mock01.dev.alea.ca:5000";

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

  public boolean isAuthEnabled() {
    return authEnabled;
  }

  public void setAuthEnabled(boolean authEnabled) {
    this.authEnabled = authEnabled;
  }

  public String getAuthUserHeader() {
    return authUserHeader;
  }

  public void setAuthUserHeader(String authUserHeader) {
    this.authUserHeader = authUserHeader;
  }

  public boolean isRolloverOnNoWinner() {
    return rolloverOnNoWinner;
  }

  public void setRolloverOnNoWinner(boolean rolloverOnNoWinner) {
    this.rolloverOnNoWinner = rolloverOnNoWinner;
  }

  public List<Integer> getBetAmountsCents() {
    return betAmountsCents;
  }

  public void setBetAmountsCents(List<Integer> betAmountsCents) {
    this.betAmountsCents = betAmountsCents;
  }

  public String getFixturesBaseUrl() {
    return fixturesBaseUrl;
  }

  public void setFixturesBaseUrl(String fixturesBaseUrl) {
    this.fixturesBaseUrl = fixturesBaseUrl;
  }
}
