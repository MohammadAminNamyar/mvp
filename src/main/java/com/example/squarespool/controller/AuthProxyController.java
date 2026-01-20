package com.example.squarespool.controller;

import com.example.squarespool.dto.LoginRequest;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthProxyController {
  private static final String AUTH_BASE_URL = "https://wsng.dev.alea.ca";
  private static final String AUTH_PATH = "/cca/customerauthn/pl/login";

  private final HttpClient httpClient;

  public AuthProxyController() {
    this.httpClient = createHttpClient();
  }

  @PostMapping("/login")
  public ResponseEntity<Object> login(
      @Validated @RequestBody LoginRequest request,
      @RequestHeader(name = "X-Debug-Auth", required = false) String debugHeader) {
    try {
      URI uri =
          URI.create(
              AUTH_BASE_URL
                  + AUTH_PATH
                  + "?username="
                  + urlEncode(request.getUsername())
                  + "&password="
                  + urlEncode(request.getPassword()));
      HttpRequest httpRequest = HttpRequest.newBuilder().uri(uri).GET().build();
      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      if ("true".equalsIgnoreCase(debugHeader)) {
        return ResponseEntity.ok(buildDebugResponse(response));
      }

      if (isSuccessResponse(response.uri().toString(), response.body())) {
        return ResponseEntity.noContent().build();
      }
      if (isFailureResponse(response.uri().toString(), response.body())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed.");
      }

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed.");
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body("Authentication service unavailable.");
    }
  }

  private static HttpClient createHttpClient() {
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
      CookieManager cookieManager = new CookieManager();
      cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
      // Trusts the upstream self-signed certificate; replace with a proper truststore in prod.
      return HttpClient.newBuilder()
          .followRedirects(Redirect.ALWAYS)
          .sslContext(sslContext)
          .sslParameters(sslParameters)
          .cookieHandler(cookieManager)
          .build();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to configure auth proxy SSL context.", ex);
    }
  }

  private static boolean isFailureResponse(String location, String body) {
    String marker = normalizeMarker(location, body);
    return marker.contains("authentication failed")
        || marker.contains("invalid credentials")
        || marker.contains("missing parameters")
        || marker.contains("error=");
  }

  private static boolean isSuccessResponse(String location, String body) {
    String marker = normalizeMarker(location, body);
    return marker.contains("log in successful") || marker.contains("successfully logged");
  }

  private static String normalizeMarker(String location, String body) {
    StringBuilder builder = new StringBuilder();
    if (location != null) {
      builder.append(location).append(' ');
    }
    if (body != null) {
      builder.append(body);
    }
    return builder.toString().toLowerCase(Locale.ROOT);
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static Map<String, Object> buildDebugResponse(HttpResponse<String> response) {
    Map<String, Object> debug = new HashMap<>();
    debug.put("status", response.statusCode());
    debug.put("finalUrl", response.uri().toString());
    debug.put("contentType", response.headers().firstValue("content-type").orElse(null));
    debug.put("bodyLength", response.body() == null ? 0 : response.body().length());
    debug.put("bodySnippet", bodySnippet(response.body()));
    debug.put("bodySnippetBase64", bodySnippetBase64(response.body()));
    return debug;
  }

  private static String bodySnippet(String body) {
    if (body == null) {
      return null;
    }
    int max = 400;
    return body.length() <= max ? body : body.substring(0, max);
  }

  private static String bodySnippetBase64(String body) {
    if (body == null) {
      return null;
    }
    int max = 400;
    String snippet = body.length() <= max ? body : body.substring(0, max);
    return Base64.getEncoder().encodeToString(snippet.getBytes(StandardCharsets.UTF_8));
  }
}
