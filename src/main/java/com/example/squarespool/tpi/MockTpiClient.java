package com.example.squarespool.tpi;

import com.example.squarespool.config.TpiProperties;
import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import com.example.squarespool.tpi.TpiCustomer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public class MockTpiClient implements TpiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MockTpiClient(RestClient.Builder restClientBuilder, TpiProperties properties, ObjectMapper objectMapper) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public DebitResponse debit(DebitRequest request) {
        try {
            ResponseEntity<DebitResponse> response =
                    restClient.post()
                            .uri("/accountservice/debit")
                            .body(request)
                            .retrieve()
                            .toEntity(DebitResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new IllegalStateException("TPI debit failed with status " + response.getStatusCode());
        } catch (RestClientResponseException ex) {
            DebitResponse errorResponse = parseErrorResponse(ex);
            if (errorResponse != null) {
                return errorResponse;
            }
            throw new IllegalStateException(
                "TPI debit failed with status " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString(),
                ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to reach TPI mock service", ex);
        }
    }

    @Override
    public DebitResponse credit(DebitRequest request) {
        try {
            ResponseEntity<DebitResponse> response =
                    restClient.post()
                            .uri("/accountservice/credit")
                            .body(request)
                            .retrieve()
                            .toEntity(DebitResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new IllegalStateException("TPI credit failed with status " + response.getStatusCode());
        } catch (RestClientResponseException ex) {
            DebitResponse errorResponse = parseErrorResponse(ex);
            if (errorResponse != null) {
                return errorResponse;
            }
            throw new IllegalStateException(
                    "TPI credit failed with status " + ex.getStatusCode() + ": " + ex.getResponseBodyAsString(),
                    ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to reach TPI mock service", ex);
        }
    }

    private DebitResponse parseErrorResponse(RestClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(responseBody, DebitResponse.class);
        } catch (JsonProcessingException parseException) {
            return null;
        }
    }

    @Override
    public TpiCustomer resolveCustomer(String serviceTicket, String fallbackName) {
        String displayName = (fallbackName == null || fallbackName.isBlank()) ? "Customer" : fallbackName;
        String customerId = serviceTicket == null || serviceTicket.isBlank() ? "mock-customer" : serviceTicket;
        return new TpiCustomer(customerId, displayName);
    }
}
