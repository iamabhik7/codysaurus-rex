package com.example.githubsearch.service.impl.helper;

import com.example.githubsearch.exception.ApiException;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

/**
 * Helper class for handling HTTP responses from GitHub API.
 * <p>
 * Provides methods to parse responses and handle error status codes.
 */
@Slf4j
public class ResponseHandlerHelper {
    /**
     * Handles the HTTP response from GitHub.
     * @param response the HTTP response
     * @return Mono emitting parsed response or error
     * Added: GitHub API rate-limit header monitoring
     */
    @SuppressWarnings("null")
    public static Mono<GitRepositoryPaginatedResponse> handleResponse(final ClientResponse response) {
        final HttpStatus status = HttpStatus.resolve(response.statusCode().value());

        //Errored Status Code Check
        if (status.is2xxSuccessful()) {
            return response.bodyToMono(GitRepositoryPaginatedResponse.class);
        }
        else if (status == HttpStatus.NOT_MODIFIED) {
            log.info("GitHub API returned 304 Not Modified, returning empty Mono.");
            return Mono.empty();
        }
        else if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
            return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("GitHub API validation failed (422): {}", errorBody);
                        return Mono.error(new ApiException(status, "Validation failed: " + errorBody));
                    });
        }
        else if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("GitHub API service unavailable (503): {}", errorBody);
                        return Mono.error(new ApiException(status, "Service unavailable: " + errorBody));
                    });
        }
        else {
            return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        log.error("GitHub API call failed: status={}, body={}", status, errorBody);
                        return Mono.error(new ApiException(status, "GitHub API responded with error: " + errorBody));
                    });
        }
    }
}
