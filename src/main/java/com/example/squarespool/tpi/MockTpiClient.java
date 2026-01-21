package com.example.squarespool.tpi;

import com.example.squarespool.config.TpiProperties;
import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class MockTpiClient implements TpiClient {
  private final RestClient restClient;

  public MockTpiClient(RestClient.Builder restClientBuilder, TpiProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
  }

  @Override
  public TpiCustomer resolveCustomer(String serviceTicket, String fallbackName) {
    return new TpiCustomer(fallbackName);
  }

  @Override
  public DebitResponse debit(DebitRequest request) {
    try {
      ResponseEntity<DebitResponse> response =
          restClient
              .post()
              .uri("/accountservice/debit")
              .body(request)
              .retrieve()
              .toEntity(DebitResponse.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return response.getBody();
      }
      throw new IllegalStateException("TPI debit failed with status " + response.getStatusCode());
    } catch (RestClientException ex) {
      throw new IllegalStateException("Unable to reach TPI mock service", ex);
    }
  }
}
