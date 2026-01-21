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
    private double payoutQ1Percent;
    private double payoutQ2Percent;
    private double payoutQ3Percent;
    private double payoutQ4Percent;
    private BoardStatus status;
    private int homeScore;
    private int awayScore;
    private String gameClock;
    private Quarter currentQuarter;
    private Set<Quarter> confirmedQuarters;
    private boolean rolloverOnNoWinner;
    private int rolloverCents;
    private Quarter lastRolloverQuarter;
    private boolean digitsRevealed;
    private List<Integer> rowDigits;
    private List<Integer> colDigits;
    private int purchasedCount;
    private boolean active;
    private Integer currentWinnerIdx;
    private int prizePoolCents;
    private int prizeQ1Cents;
    private int prizeQ2Cents;
    private int prizeQ3Cents;
    private int prizeQ4Cents;
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

    public String getGameClock() {
        return gameClock;
    }

    public void setGameClock(String gameClock) {
        this.gameClock = gameClock;
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

    public boolean isRolloverOnNoWinner() {
        return rolloverOnNoWinner;
    }

    public void setRolloverOnNoWinner(boolean rolloverOnNoWinner) {
        this.rolloverOnNoWinner = rolloverOnNoWinner;
    }

    public int getRolloverCents() {
        return rolloverCents;
    }

    public void setRolloverCents(int rolloverCents) {
        this.rolloverCents = rolloverCents;
    }

    public Quarter getLastRolloverQuarter() {
        return lastRolloverQuarter;
    }

    public void setLastRolloverQuarter(Quarter lastRolloverQuarter) {
        this.lastRolloverQuarter = lastRolloverQuarter;
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

    public int getPrizePoolCents() {
        return prizePoolCents;
    }

    public void setPrizePoolCents(int prizePoolCents) {
        this.prizePoolCents = prizePoolCents;
    }

    public int getPrizeQ1Cents() {
        return prizeQ1Cents;
    }

    public void setPrizeQ1Cents(int prizeQ1Cents) {
        this.prizeQ1Cents = prizeQ1Cents;
    }

    public int getPrizeQ2Cents() {
        return prizeQ2Cents;
    }

    public void setPrizeQ2Cents(int prizeQ2Cents) {
        this.prizeQ2Cents = prizeQ2Cents;
    }

    public int getPrizeQ3Cents() {
        return prizeQ3Cents;
    }

    public void setPrizeQ3Cents(int prizeQ3Cents) {
        this.prizeQ3Cents = prizeQ3Cents;
    }

    public int getPrizeQ4Cents() {
        return prizeQ4Cents;
    }

    public void setPrizeQ4Cents(int prizeQ4Cents) {
        this.prizeQ4Cents = prizeQ4Cents;
    }

    public List<SquareSnapshot> getSquares() {
        return squares;
    }

    public void setSquares(List<SquareSnapshot> squares) {
        this.squares = squares;
    }
}
