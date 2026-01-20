package com.example.squarespool.controller;

import com.example.squarespool.dto.LoginRequest;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthProxyController {
  private static final String AUTH_BASE_URL = "https://wsng.dev.alea.ca";
  private static final String AUTH_PATH = "/cca/customerauthn/pl/login";

  private final RestClient restClient;

  public AuthProxyController(RestClient.Builder restClientBuilder) {
    this.restClient =
        restClientBuilder.baseUrl(AUTH_BASE_URL).requestFactory(createRequestFactory()).build();
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@Validated @RequestBody LoginRequest request) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(AUTH_PATH)
                      .queryParam("username", request.getUsername())
                      .queryParam("password", request.getPassword())
                      .build())
          .exchange(
              (clientRequest, clientResponse) -> {
                if (clientResponse.getBody() == null) {
                  return ResponseEntity.status(clientResponse.getStatusCode()).build();
                }
                String body =
                    StreamUtils.copyToString(clientResponse.getBody(), StandardCharsets.UTF_8);
                return ResponseEntity.status(clientResponse.getStatusCode()).body(body);
              });
    } catch (RestClientException ex) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body("Authentication service unavailable.");
    }
  }

  private static JdkClientHttpRequestFactory createRequestFactory() {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(
          null,
          new TrustManager[] {
            new X509TrustManager() {
              @Override
              public void checkClientTrusted(X509Certificate[] chain, String authType) {}

              @Override
              public void checkServerTrusted(X509Certificate[] chain, String authType) {}

              @Override
              public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
              }
            }
          },
          new SecureRandom());
      SSLParameters sslParameters = new SSLParameters();
      sslParameters.setEndpointIdentificationAlgorithm(null);
      // Trusts the upstream self-signed certificate; replace with a proper truststore in prod.
      HttpClient httpClient =
          HttpClient.newBuilder().sslContext(sslContext).sslParameters(sslParameters).build();
      return new JdkClientHttpRequestFactory(httpClient);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to configure auth proxy SSL context.", ex);
    }
  }
}
