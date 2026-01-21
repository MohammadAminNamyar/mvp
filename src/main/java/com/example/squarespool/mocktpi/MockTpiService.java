package com.example.squarespool.mocktpi;

import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.dto.MoneyAmount;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockTpiService {
    private static final long SUCCESS_CODE = 0L;
    private static final long INSUFFICIENT_FUNDS_CODE = 1001L;
    private static final long INVALID_REQUEST_CODE = 1000L;

    private final Map<String, Long> balances = new ConcurrentHashMap<>();

    public DebitResponse debit(DebitRequest request) {
        validateRequest(request);
        String customerKey = request.getCustomerId();
        long currentBalance = balances.getOrDefault(customerKey, 100_000_00L);
        long amount = request.getAmount().getValue();

        if (amount > currentBalance) {
            return buildResponse(request, INSUFFICIENT_FUNDS_CODE, "Insufficient funds", currentBalance);
        }

        long updated = currentBalance - amount;
        balances.put(customerKey, updated);
        return buildResponse(request, SUCCESS_CODE, "OK", updated);
    }

    private void validateRequest(DebitRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Missing amount");
        }
        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Missing customerId");
        }
        if (request.getAmount().getValue() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private DebitResponse buildResponse(DebitRequest request, long code, String message, long balance) {
        DebitResponse response = new DebitResponse();
        response.setResponseCode(code);
        response.setResponseMessage(message);
        response.setAleaRoundId(request.getThirdPartyRoundId() != null ? request.getThirdPartyRoundId() : randomId());
        response.setAleaTransactionId(request.getThirdPartyTransactionId() != null ? request.getThirdPartyTransactionId() : randomId());
        response.setAleaAccountBalance(new MoneyAmount(
                request.getAmount().getCurrency(),
                balance));
        response.setAleaAccountBonusBalance(new MoneyAmount(
                request.getAmount().getCurrency(),
                0L));
        return response;
    }

    private String randomId() {
        return UUID.randomUUID().toString() + "-" + Instant.now().toEpochMilli();
    }
}
