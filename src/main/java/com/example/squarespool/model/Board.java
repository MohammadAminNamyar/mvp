package com.example.squarespool.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String homeTeam;
    private String awayTeam;
    private int priceCents;
    private int housePercent;
    private int minSquaresToActivate;
    private int payoutQ1Percent = 25;
    private int payoutQ2Percent = 25;
    private int payoutQ3Percent = 25;
    private int payoutQ4Percent = 25;

    @Enumerated(EnumType.STRING)
    private BoardStatus status = BoardStatus.OPEN;

    private String rowDigits;
    private String colDigits;

    private int homeScore;
    private int awayScore;
    private String gameClock;
    private int gameClockSeconds;
    private boolean gameClockRunning;
    private Instant gameClockUpdatedAt;

    @Enumerated(EnumType.STRING)
    private Quarter currentQuarter = Quarter.Q1;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "board_confirmed_quarters", joinColumns = @JoinColumn(name = "board_id"))
    @Enumerated(EnumType.STRING)
    private Set<Quarter> confirmedQuarters = new HashSet<>();

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Square> squares = new ArrayList<>();

    public Long getId() {
        return id;
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

    public String getRowDigits() {
        return rowDigits;
    }

    public void setRowDigits(String rowDigits) {
        this.rowDigits = rowDigits;
    }

    public String getColDigits() {
        return colDigits;
    }

    public void setColDigits(String colDigits) {
        this.colDigits = colDigits;
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

    public int getGameClockSeconds() {
        return gameClockSeconds;
    }

    public void setGameClockSeconds(int gameClockSeconds) {
        this.gameClockSeconds = gameClockSeconds;
    }

    public boolean isGameClockRunning() {
        return gameClockRunning;
    }

    public void setGameClockRunning(boolean gameClockRunning) {
        this.gameClockRunning = gameClockRunning;
    }

    public Instant getGameClockUpdatedAt() {
        return gameClockUpdatedAt;
    }

    public void setGameClockUpdatedAt(Instant gameClockUpdatedAt) {
        this.gameClockUpdatedAt = gameClockUpdatedAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public void setSquares(List<Square> squares) {
        this.squares = squares;
    }
}
