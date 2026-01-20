package com.example.squarespool.tpi.dto;

public class DebitResponse {
    private Long responseCode;
    private String responseMessage;
    private String aleaRoundId;
    private String aleaTransactionId;
    private MoneyAmount aleaAccountBalance;
    private MoneyAmount aleaAccountBonusBalance;

    public Long getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Long responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAleaRoundId() {
        return aleaRoundId;
    }

    public void setAleaRoundId(String aleaRoundId) {
        this.aleaRoundId = aleaRoundId;
    }

    public String getAleaTransactionId() {
        return aleaTransactionId;
    }

    public void setAleaTransactionId(String aleaTransactionId) {
        this.aleaTransactionId = aleaTransactionId;
    }

    public MoneyAmount getAleaAccountBalance() {
        return aleaAccountBalance;
    }

    public void setAleaAccountBalance(MoneyAmount aleaAccountBalance) {
        this.aleaAccountBalance = aleaAccountBalance;
    }

    public MoneyAmount getAleaAccountBonusBalance() {
        return aleaAccountBonusBalance;
    }

    public void setAleaAccountBonusBalance(MoneyAmount aleaAccountBonusBalance) {
        this.aleaAccountBonusBalance = aleaAccountBonusBalance;
    }
}
