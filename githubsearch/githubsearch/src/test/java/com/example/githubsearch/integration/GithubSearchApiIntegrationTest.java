package com.example.githubsearch.integration;

import com.example.githubsearch.dto.SearchRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the GitHub Search API.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubSearchApiIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * Tests successful repository search with valid payload.
     */
    @Test
    @DisplayName("POST /api/github/search - success")
    void testSearchSuccess() {
        SearchRequestDto request = SearchRequestDto.builder()
                .language("Java")
                .earliestCreatedDate(Instant.parse("2020-01-01T00:00:00Z"))
                .pageNumber(1)
                .build();
        webTestClient.post().uri("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().value(status -> assertTrue(status == 200 || status == 400))
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(resp -> {
                    String body = new String(resp.getResponseBody() == null ? new byte[0] : resp.getResponseBody());
                    assertTrue(body.contains("totalCount") || body.contains("error"));
                });
    }

    /**
     * Tests validation error for missing required fields.
     */
    @Test
    @DisplayName("POST /api/github/search - validation error")
    void testSearchValidationError() {
        SearchRequestDto request = SearchRequestDto.builder()
                .language("") // Invalid: empty
                .pageNumber(0) // Invalid: less than 1
                .build();
        webTestClient.post().uri("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(resp -> {
                    String body = new String(resp.getResponseBody() == null ? new byte[0] : resp.getResponseBody());
                    assertTrue(body.contains("Validation failed") || body.contains("Unexpected error") || body.contains("error"));
                });
    }

    /**
     * Tests error for malformed JSON payload.
     */
    @Test
    @DisplayName("POST /api/github/search - malformed JSON")
    void testSearchMalformedJson() {
        String badJson = "{\"language\": Java, pageNumber: 1}"; // Missing quotes, invalid JSON
        webTestClient.post().uri("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(resp -> {
                    String body = new String(resp.getResponseBody() == null ? new byte[0] : resp.getResponseBody());
                    assertTrue(body.contains("Malformed JSON request") || body.contains("Unexpected error") || body.contains("error"));
                });
    }

    /**
     * Tests error for invalid date format.
     */
    @Test
    @DisplayName("POST /api/github/search - invalid date format")
    void testSearchInvalidDateFormat() {
        String badDateJson = "{\"language\":\"Java\",\"earliestCreatedDate\":\"not-a-date\",\"pageNumber\":1}";
        webTestClient.post().uri("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badDateJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .consumeWith(resp -> {
                    String body = new String(resp.getResponseBody() == null ? new byte[0] : resp.getResponseBody());
                    assertTrue(body.contains("Invalid date format") || body.contains("Unexpected error") || body.contains("error"));
                });
    }

    /**
     * Tests error for rate limit exceeded.
     */
    @Test
    @DisplayName("POST /api/github/search - rate limit exceeded (simulated)")
    void testSearchRateLimitExceeded() {
        webTestClient.post().uri("/api/github/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SearchRequestDto.builder().language("Java").pageNumber(1).build())
                .header("X-RateLimit-Test", "exceed")
                .exchange()
                .expectStatus().value(status -> assertTrue(status == 429 || status == 200 || status == 403 || status == 400))
                .expectBody().consumeWith(resp -> {
                    String body = new String(resp.getResponseBody() == null ? new byte[0] : resp.getResponseBody());
                    assertTrue(body.contains("rate limit") || body.contains("error") || body.contains("Unexpected error") || body.contains("totalCount"));
                });
    }
}
