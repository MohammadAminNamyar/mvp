package com.example.squarespool.tpi;

import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.TpiCustomer;

public interface TpiClient {
    TpiCustomer resolveCustomer(String serviceTicket, String fallbackName);
    DebitResponse debit(DebitRequest request);
    DebitResponse credit(DebitRequest request);
}
