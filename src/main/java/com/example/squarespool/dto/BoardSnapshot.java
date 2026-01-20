package com.example.squarespool.dto;

import com.example.squarespool.model.BoardStatus;
import com.example.squarespool.model.Quarter;

import java.util.List;
import java.util.Set;

public class BoardSnapshot {
    private Long boardId;
    private String name;
    private String homeTeam;
    private String awayTeam;
    private int priceCents;
    private int housePercent;
    private int minSquaresToActivate;
    private BoardStatus status;
    private int homeScore;
    private int awayScore;
    private Quarter currentQuarter;
    private Set<Quarter> confirmedQuarters;
    private boolean digitsRevealed;
    private List<Integer> rowDigits;
    private List<Integer> colDigits;
    private int purchasedCount;
    private boolean active;
    private Integer currentWinnerIdx;
    private List<SquareSnapshot> squares;

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

    public BoardStatus getStatus() {
        return status;
    }

    public void setStatus(BoardStatus status) {
        this.status = status;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public Quarter getCurrentQuarter() {
        return currentQuarter;
    }

    public void setCurrentQuarter(Quarter currentQuarter) {
        this.currentQuarter = currentQuarter;
    }

    public Set<Quarter> getConfirmedQuarters() {
        return confirmedQuarters;
    }

    public void setConfirmedQuarters(Set<Quarter> confirmedQuarters) {
        this.confirmedQuarters = confirmedQuarters;
    }

    public boolean isDigitsRevealed() {
        return digitsRevealed;
    }

    public void setDigitsRevealed(boolean digitsRevealed) {
        this.digitsRevealed = digitsRevealed;
    }

    public List<Integer> getRowDigits() {
        return rowDigits;
    }

    public void setRowDigits(List<Integer> rowDigits) {
        this.rowDigits = rowDigits;
    }

    public List<Integer> getColDigits() {
        return colDigits;
    }

    public void setColDigits(List<Integer> colDigits) {
        this.colDigits = colDigits;
    }

    public int getPurchasedCount() {
        return purchasedCount;
    }

    public void setPurchasedCount(int purchasedCount) {
        this.purchasedCount = purchasedCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getCurrentWinnerIdx() {
        return currentWinnerIdx;
    }

    public void setCurrentWinnerIdx(Integer currentWinnerIdx) {
        this.currentWinnerIdx = currentWinnerIdx;
    }

    public List<SquareSnapshot> getSquares() {
        return squares;
    }

    public void setSquares(List<SquareSnapshot> squares) {
        this.squares = squares;
    }
}
