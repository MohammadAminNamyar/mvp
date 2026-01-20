package com.example.squarespool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tpi")
public class TpiProperties {
    private String baseUrl = "http://localhost:8080/mock-tpi";
    private String currency = "USD";
    private long gameTypeId = 1073741824L;
    private long gameTypeVariationId = 1073741824L;
    private long thirdPartyTransactionTypeId = 1073741824L;
    private boolean roundToBeClosed = true;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(long gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public long getGameTypeVariationId() {
        return gameTypeVariationId;
    }

    public void setGameTypeVariationId(long gameTypeVariationId) {
        this.gameTypeVariationId = gameTypeVariationId;
    }

    public long getThirdPartyTransactionTypeId() {
        return thirdPartyTransactionTypeId;
    }

    public void setThirdPartyTransactionTypeId(long thirdPartyTransactionTypeId) {
        this.thirdPartyTransactionTypeId = thirdPartyTransactionTypeId;
    }

    public boolean isRoundToBeClosed() {
        return roundToBeClosed;
    }

    public void setRoundToBeClosed(boolean roundToBeClosed) {
        this.roundToBeClosed = roundToBeClosed;
    }
}
