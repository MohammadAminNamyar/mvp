package com.example.squarespool.dto;

import com.example.squarespool.model.SquareStatus;

import java.time.Instant;

public class SquareSnapshot {
    private int idx;
    private int rowIndex;
    private int colIndex;
    private SquareStatus status;
    private String ownerName;
    private String ownerSessionId;
    private String reservedBySessionId;
    private Instant reservedUntil;
    private boolean wonQ1;
    private boolean wonQ2;
    private boolean wonQ3;
    private boolean wonFinal;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    public SquareStatus getStatus() {
        return status;
    }

    public void setStatus(SquareStatus status) {
        this.status = status;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public void setOwnerSessionId(String ownerSessionId) {
        this.ownerSessionId = ownerSessionId;
    }

    public String getReservedBySessionId() {
        return reservedBySessionId;
    }

    public void setReservedBySessionId(String reservedBySessionId) {
        this.reservedBySessionId = reservedBySessionId;
    }

    public Instant getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(Instant reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public boolean isWonQ1() {
        return wonQ1;
    }

    public void setWonQ1(boolean wonQ1) {
        this.wonQ1 = wonQ1;
    }

    public boolean isWonQ2() {
        return wonQ2;
    }

    public void setWonQ2(boolean wonQ2) {
        this.wonQ2 = wonQ2;
    }

    public boolean isWonQ3() {
        return wonQ3;
    }

    public void setWonQ3(boolean wonQ3) {
        this.wonQ3 = wonQ3;
    }

    public boolean isWonFinal() {
        return wonFinal;
    }

    public void setWonFinal(boolean wonFinal) {
        this.wonFinal = wonFinal;
    }
}
