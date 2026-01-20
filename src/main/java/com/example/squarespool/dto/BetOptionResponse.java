package com.example.squarespool.dto;

public class BetOptionResponse {
    private int amountCents;
    private String label;

    public BetOptionResponse(int amountCents, String label) {
        this.amountCents = amountCents;
        this.label = label;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public String getLabel() {
        return label;
    }
}
