package com.example.squarespool.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payout_events")
public class PayoutEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @Enumerated(EnumType.STRING)
    private Quarter quarter;

    @Enumerated(EnumType.STRING)
    private PayoutEventType eventType;

    private String recipient;
    private int amountCents;
    private Integer winnerSquareIdx;
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Quarter getQuarter() {
        return quarter;
    }

    public void setQuarter(Quarter quarter) {
        this.quarter = quarter;
    }

    public PayoutEventType getEventType() {
        return eventType;
    }

    public void setEventType(PayoutEventType eventType) {
        this.eventType = eventType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(int amountCents) {
        this.amountCents = amountCents;
    }

    public Integer getWinnerSquareIdx() {
        return winnerSquareIdx;
    }

    public void setWinnerSquareIdx(Integer winnerSquareIdx) {
        this.winnerSquareIdx = winnerSquareIdx;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
