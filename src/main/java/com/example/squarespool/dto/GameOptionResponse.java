package com.example.squarespool.dto;

public class GameOptionResponse {
    private String name;
    private String homeTeam;
    private String awayTeam;

    public GameOptionResponse(String name, String homeTeam, String awayTeam) {
        this.name = name;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public String getName() {
        return name;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }
}
