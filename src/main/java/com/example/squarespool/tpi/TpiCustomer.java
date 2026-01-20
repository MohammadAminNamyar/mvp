package com.example.squarespool.tpi;

public class TpiCustomer {
    private final String customerId;
    private final String displayName;

    public TpiCustomer(String customerId, String displayName) {
        this.customerId = customerId;
        this.displayName = displayName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
