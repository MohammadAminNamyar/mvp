package com.example.squarespool.dto;

import java.util.List;

public class PlayerHistoryBoard {
    private Long boardId;
    private String name;
    private String sportType;
    private String gameName;
    private String homeTeam;
    private String awayTeam;
    private int priceCents;
    private int totalSpentCents;
    private int totalWinningsCents;
    private int netCents;
    private boolean finalPrizeRefunded;
    private int finalRefundPerPlayerCents;
    private List<PlayerHistorySquare> squares;

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
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

    public int getTotalSpentCents() {
        return totalSpentCents;
    }

    public void setTotalSpentCents(int totalSpentCents) {
        this.totalSpentCents = totalSpentCents;
    }

    public int getTotalWinningsCents() {
        return totalWinningsCents;
    }

    public void setTotalWinningsCents(int totalWinningsCents) {
        this.totalWinningsCents = totalWinningsCents;
    }

    public int getNetCents() {
        return netCents;
    }

    public void setNetCents(int netCents) {
        this.netCents = netCents;
    }

    public boolean isFinalPrizeRefunded() {
        return finalPrizeRefunded;
    }

    public void setFinalPrizeRefunded(boolean finalPrizeRefunded) {
        this.finalPrizeRefunded = finalPrizeRefunded;
    }

    public int getFinalRefundPerPlayerCents() {
        return finalRefundPerPlayerCents;
    }

    public void setFinalRefundPerPlayerCents(int finalRefundPerPlayerCents) {
        this.finalRefundPerPlayerCents = finalRefundPerPlayerCents;
    }

    public List<PlayerHistorySquare> getSquares() {
        return squares;
    }

    public void setSquares(List<PlayerHistorySquare> squares) {
        this.squares = squares;
    }
}
