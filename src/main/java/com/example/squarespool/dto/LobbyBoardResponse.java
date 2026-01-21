package com.example.squarespool.dto;

import com.example.squarespool.model.BoardStatus;

public class LobbyBoardResponse {
    private Long id;
    private String name;
    private BoardStatus status;
    private int openSquares;
    private int totalSquares;
    private boolean full;

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

    public int getTotalSquares() {
        return totalSquares;
    }

    public void setTotalSquares(int totalSquares) {
        this.totalSquares = totalSquares;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
