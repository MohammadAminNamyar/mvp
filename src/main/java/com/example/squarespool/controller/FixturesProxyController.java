package com.example.squarespool.controller;

import com.example.squarespool.config.AppProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fixtures")
public class FixturesProxyController {
  private static final String FIXTURES_PATH = "/games";

  private final AppProperties appProperties;
  private final HttpClient httpClient;

  public FixturesProxyController(AppProperties appProperties) {
    this.appProperties = appProperties;
    this.httpClient = HttpClient.newHttpClient();
  }

  @GetMapping
  public ResponseEntity<String> listFixtures() {
    try {
      URI uri = URI.create(appProperties.getFixturesBaseUrl() + FIXTURES_PATH);
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(uri)
              .header("Accept", MediaType.APPLICATION_JSON_VALUE)
              .GET()
              .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response.body());
      }
    } catch (Exception ex) {
      // Fall through to a gateway error response.
    }

    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Unable to load fixtures.");
  }
}
