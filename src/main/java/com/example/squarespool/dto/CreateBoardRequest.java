package com.example.squarespool.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateBoardRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String sportType;
    @NotBlank
    private String gameName;
    private String gameId;
    @NotBlank
    private String homeTeam;
    @NotBlank
    private String awayTeam;
    @Min(1)
    private int priceCents = 500;
    @Min(0)
    private int housePercent = 10;
    @Min(0)
    private int minSquaresToActivate = 0;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double payoutQ1Percent = 12.5;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double payoutQ2Percent = 25.0;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double payoutQ3Percent = 12.5;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double payoutQ4Percent = 50.0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(int priceCents) {
        this.priceCents = priceCents;
    }

    public int getHousePercent() {
        return housePercent;
    }

    public void setHousePercent(int housePercent) {
        this.housePercent = housePercent;
    }

    public int getMinSquaresToActivate() {
        return minSquaresToActivate;
    }

    public void setMinSquaresToActivate(int minSquaresToActivate) {
        this.minSquaresToActivate = minSquaresToActivate;
    }

    public double getPayoutQ1Percent() {
        return payoutQ1Percent;
    }

    public void setPayoutQ1Percent(double payoutQ1Percent) {
        this.payoutQ1Percent = payoutQ1Percent;
    }

    public double getPayoutQ2Percent() {
        return payoutQ2Percent;
    }

    public void setPayoutQ2Percent(double payoutQ2Percent) {
        this.payoutQ2Percent = payoutQ2Percent;
    }

    public double getPayoutQ3Percent() {
        return payoutQ3Percent;
    }

    public void setPayoutQ3Percent(double payoutQ3Percent) {
        this.payoutQ3Percent = payoutQ3Percent;
    }

    public double getPayoutQ4Percent() {
        return payoutQ4Percent;
    }

    public void setPayoutQ4Percent(double payoutQ4Percent) {
        this.payoutQ4Percent = payoutQ4Percent;
    }
}
