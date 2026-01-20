package com.example.squarespool.tpi.dto;

public class MoneyAmount {
    private String currency;
    private long value;

    public MoneyAmount() {
    }

    public MoneyAmount(String currency, long value) {
        this.currency = currency;
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
