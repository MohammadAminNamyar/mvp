package com.example.squarespool.dto;

import com.example.squarespool.model.BoardStatus;

public class AdminBoardSummary {
    private Long id;
    private String name;
    private String sportType;
    private String gameName;
    private String gameId;
    private String homeTeam;
    private String awayTeam;
    private int priceCents;
    private int housePercent;
    private int minSquaresToActivate;
    private int payoutQ1Percent;
    private int payoutQ2Percent;
    private int payoutQ3Percent;
    private int payoutQ4Percent;
    private BoardStatus status;
    private int openSquares;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BoardStatus getStatus() {
        return status;
    }

    public void setStatus(BoardStatus status) {
        this.status = status;
    }

    public int getOpenSquares() {
        return openSquares;
    }

    public void setOpenSquares(int openSquares) {
        this.openSquares = openSquares;
    }
}
