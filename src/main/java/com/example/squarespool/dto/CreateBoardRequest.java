package com.example.squarespool.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateBoardRequest {
    @NotBlank
    private String name;
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
    @Min(0)
    private int payoutQ1Percent = 25;
    @Min(0)
    private int payoutQ2Percent = 25;
    @Min(0)
    private int payoutQ3Percent = 25;
    @Min(0)
    private int payoutQ4Percent = 25;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getPayoutQ1Percent() {
        return payoutQ1Percent;
    }

    public void setPayoutQ1Percent(int payoutQ1Percent) {
        this.payoutQ1Percent = payoutQ1Percent;
    }

    public int getPayoutQ2Percent() {
        return payoutQ2Percent;
    }

    public void setPayoutQ2Percent(int payoutQ2Percent) {
        this.payoutQ2Percent = payoutQ2Percent;
    }

    public int getPayoutQ3Percent() {
        return payoutQ3Percent;
    }

    public void setPayoutQ3Percent(int payoutQ3Percent) {
        this.payoutQ3Percent = payoutQ3Percent;
    }

    public int getPayoutQ4Percent() {
        return payoutQ4Percent;
    }

    public void setPayoutQ4Percent(int payoutQ4Percent) {
        this.payoutQ4Percent = payoutQ4Percent;
    }
}
