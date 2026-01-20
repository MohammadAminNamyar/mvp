package com.example.squarespool.tpi;

import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;

public interface TpiClient {
    DebitResponse debit(DebitRequest request);
}
