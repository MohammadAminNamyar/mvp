package com.example.squarespool.tpi;

import org.springframework.stereotype.Service;

@Service
public class TpiClient {
  public TpiCustomer resolveCustomer(String serviceTicket, String fallbackName) {
    return new TpiCustomer(fallbackName);
  }
}
