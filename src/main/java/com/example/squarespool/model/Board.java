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

    @Enumerated(EnumType.STRING)
    private BoardStatus status = BoardStatus.OPEN;

    private String rowDigits;
    private String colDigits;

    private int homeScore;
    private int awayScore;

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
