package com.example.squarespool.dto;

import jakarta.validation.constraints.Min;

public class ScoreUpdateRequest {
    @Min(0)
    private int homeScore;
    @Min(0)
    private int awayScore;

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
}
