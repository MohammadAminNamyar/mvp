package com.example.squarespool.dto;

import com.example.squarespool.model.BoardStatus;

public class BoardSummaryResponse {
    private Long id;
    private String name;
    private int openSquares;
    private int totalSquares;
    private boolean full;
    private BoardStatus status;

    public BoardSummaryResponse(Long id, String name, int openSquares, int totalSquares, boolean full, BoardStatus status) {
        this.id = id;
        this.name = name;
        this.openSquares = openSquares;
        this.totalSquares = totalSquares;
        this.full = full;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOpenSquares() {
        return openSquares;
    }

    public int getTotalSquares() {
        return totalSquares;
    }

    public boolean isFull() {
        return full;
    }

    public BoardStatus getStatus() {
        return status;
    }
}
