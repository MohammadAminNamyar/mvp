package com.example.squarespool.dto;

import com.example.squarespool.model.Board;
import com.example.squarespool.model.BoardStatus;

public class AdminBoardResponse {
    private Long id;
    private String name;
    private String sportType;
    private String gameName;
    private String homeTeam;
    private String awayTeam;
    private int priceCents;
    private int housePercent;
    private int minSquaresToActivate;
    private int boardNumber;
    private BoardStatus status;

    public AdminBoardResponse(Board board) {
        this.id = board.getId();
        this.name = board.getName();
        this.sportType = board.getSportType();
        this.gameName = board.getGameName();
        this.homeTeam = board.getHomeTeam();
        this.awayTeam = board.getAwayTeam();
        this.priceCents = board.getPriceCents();
        this.housePercent = board.getHousePercent();
        this.minSquaresToActivate = board.getMinSquaresToActivate();
        this.boardNumber = board.getBoardNumber();
        this.status = board.getStatus();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSportType() {
        return sportType;
    }

    public String getGameName() {
        return gameName;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getHousePercent() {
        return housePercent;
    }

    public int getMinSquaresToActivate() {
        return minSquaresToActivate;
    }

    public int getBoardNumber() {
        return boardNumber;
    }

    public BoardStatus getStatus() {
        return status;
    }
}
