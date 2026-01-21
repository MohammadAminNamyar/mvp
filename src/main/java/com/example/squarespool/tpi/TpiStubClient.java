package com.example.squarespool.tpi;

import com.example.squarespool.config.AppProperties;
import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.dto.MoneyAmount;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class TpiStubClient implements TpiClient {
    private static final String TICKET_PREFIX = "ST-";
    private final AppProperties appProperties;

    public TpiStubClient(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public TpiCustomer resolveCustomer(String serviceTicket, String fallbackName) {
        String ticket = serviceTicket == null ? "" : serviceTicket.trim();
        if (appProperties.isAuthEnabled() && ticket.isEmpty()) {
            throw new IllegalStateException("Missing service ticket");
        }
        if (appProperties.isAuthEnabled() && !ticket.startsWith(TICKET_PREFIX)) {
            throw new IllegalStateException("Invalid service ticket");
        }

        String customerId = ticket.isEmpty()
                ? "stub-" + safeId(fallbackName)
                : "stub-" + safeId(ticket.substring(TICKET_PREFIX.length()));

        String displayName = fallbackName == null || fallbackName.isBlank()
                ? "Customer " + customerId.substring(Math.max(0, customerId.length() - 6))
                : fallbackName;

        return new TpiCustomer(customerId, displayName);
    }

    @Override
    public DebitResponse debit(DebitRequest request) {
        DebitResponse response = new DebitResponse();
        response.setResponseCode(0L);
        response.setResponseMessage("APPROVED");
        response.setAleaRoundId(UUID.randomUUID().toString());
        response.setAleaTransactionId(UUID.randomUUID().toString());
        if (request != null && request.getAmount() != null) {
            MoneyAmount amount = request.getAmount();
            response.setAleaAccountBalance(new MoneyAmount(amount.getCurrency(), amount.getValue()));
        }
        return response;
    }

    @Override
    public DebitResponse credit(DebitRequest request) {
        DebitResponse response = new DebitResponse();
        response.setResponseCode(0L);
        response.setResponseMessage("CREDITED");
        response.setAleaRoundId(UUID.randomUUID().toString());
        response.setAleaTransactionId("credit-" + UUID.randomUUID());
        if (request != null && request.getAmount() != null) {
            MoneyAmount amount = request.getAmount();
            response.setAleaAccountBalance(new MoneyAmount(amount.getCurrency(), amount.getValue()));
        }
        return response;
    }

    private String safeId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "anon";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
    }
}
