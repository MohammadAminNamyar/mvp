package com.example.squarespool.tpi.dto;

public class DebitRequest {
    private Long thirdPartyTransactionTypeId;
    private String thirdPartyTransactionId;
    private String thirdPartyRoundId;
    private String customerId;
    private Long gameTypeId;
    private Long gameTypeVariationId;
    private MoneyAmount amount;
    private boolean roundToBeClosed;

    public Long getThirdPartyTransactionTypeId() {
        return thirdPartyTransactionTypeId;
    }

    public void setThirdPartyTransactionTypeId(Long thirdPartyTransactionTypeId) {
        this.thirdPartyTransactionTypeId = thirdPartyTransactionTypeId;
    }

    public String getThirdPartyTransactionId() {
        return thirdPartyTransactionId;
    }

    public void setThirdPartyTransactionId(String thirdPartyTransactionId) {
        this.thirdPartyTransactionId = thirdPartyTransactionId;
    }

    public String getThirdPartyRoundId() {
        return thirdPartyRoundId;
    }

    public void setThirdPartyRoundId(String thirdPartyRoundId) {
        this.thirdPartyRoundId = thirdPartyRoundId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(Long gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public Long getGameTypeVariationId() {
        return gameTypeVariationId;
    }

    public void setGameTypeVariationId(Long gameTypeVariationId) {
        this.gameTypeVariationId = gameTypeVariationId;
    }

    public MoneyAmount getAmount() {
        return amount;
    }

    public void setAmount(MoneyAmount amount) {
        this.amount = amount;
    }

    public boolean isRoundToBeClosed() {
        return roundToBeClosed;
    }

    public void setRoundToBeClosed(boolean roundToBeClosed) {
        this.roundToBeClosed = roundToBeClosed;
    }
}
