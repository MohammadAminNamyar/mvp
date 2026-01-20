package com.example.squarespool.dto;

import jakarta.validation.constraints.NotBlank;

public class ReserveRequest {
    @NotBlank
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
