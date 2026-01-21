package com.example.squarespool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PurchaseRequest {
    @NotBlank
    private String sessionId;
    private String customerName;
    @NotEmpty
    private List<Integer> indices;
    private String serviceTicket;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }

    public String getServiceTicket() {
        return serviceTicket;
    }

    public void setServiceTicket(String serviceTicket) {
        this.serviceTicket = serviceTicket;
    }
}
