package com.example.squarespool.tpi;

import com.example.squarespool.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.Locale;

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

    private String safeId(String raw) {
        if (raw == null || raw.isBlank()) {
            return "anon";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
    }
}
