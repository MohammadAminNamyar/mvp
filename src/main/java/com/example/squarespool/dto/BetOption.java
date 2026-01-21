package com.example.squarespool.dto;

public class BetOption {
    private int cents;
    private String label;

    public BetOption() {}

    public BetOption(int cents, String label) {
        this.cents = cents;
        this.label = label;
    }

    public int getCents() {
        return cents;
    }

    public void setCents(int cents) {
        this.cents = cents;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
