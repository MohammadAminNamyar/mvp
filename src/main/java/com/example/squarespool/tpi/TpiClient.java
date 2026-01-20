package com.example.squarespool.tpi;

import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;

public interface TpiClient {
    TpiCustomer resolveCustomer(String serviceTicket, String fallbackName);
    DebitResponse debit(DebitRequest request);
}
