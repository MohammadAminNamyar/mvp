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
}
