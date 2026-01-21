package com.example.squarespool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "squares", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"board_id", "idx"})
})
public class Square {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private int rowIndex;
    private int colIndex;
    private int idx;

    @Enumerated(EnumType.STRING)
    private SquareStatus status = SquareStatus.EMPTY;

    private String ownerName;
    private String ownerSessionId;
    private String reservedBySessionId;
    private Instant reservedUntil;
    private String tpiCustomerId;

    private boolean wonQ1;
    private boolean wonQ2;
    private boolean wonQ3;
    private boolean wonFinal;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
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

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
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

    public String getTpiCustomerId() {
        return tpiCustomerId;
    }

    public void setTpiCustomerId(String tpiCustomerId) {
        this.tpiCustomerId = tpiCustomerId;
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
