package com.example.githubsearch.service.impl;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.exception.ApiException;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryClient;
import com.example.githubsearch.service.impl.helper.GitHubQueryBuilder;
import com.example.githubsearch.service.impl.helper.ResponseHandlerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import java.time.Duration;

/**
 * Implementation of {@link GitRepositoryClient} for fetching repositories asynchronously from GitHub API.
 * <p>
 * Uses WebClient for async HTTP calls, applies rate limiting, and handles retries and error responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitRepositoryClientImpl implements GitRepositoryClient {
    private final WebClient webClient;
    private final GithubApiProperties githubApiProperties;

    /**
     * Fetches repositories from GitHub based on search criteria.
     *
     * @param searchRequest the search parameters,
     * @return Mono emitting paginated repository response or error
     */
    @Override
    @RateLimiter(name = "githubApiLimiter", fallbackMethod = "rateLimitFallback")
    public Mono<GitRepositoryPaginatedResponse> fetchRepositories(final SearchRequest searchRequest) {
        final String fullUri = GitHubQueryBuilder.buildUri(githubApiProperties, searchRequest);
        log.info("Full Web URI: {}", fullUri);
        return webClient.get()
                .uri(fullUri)
                .header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .exchangeToMono(ResponseHandlerHelper::handleResponse)
                // keep retry logic as-is (server errors only)
                .retryWhen(Retry.backoff(githubApiProperties.getApi().getRetry().getAttempts(),
                                Duration.ofSeconds(githubApiProperties.getApi().getRetry()
                                        .getBackoffSeconds()))
                        .filter(throwable -> throwable instanceof ApiException &&
                                ((ApiException) throwable).getStatus()
                                        .is5xxServerError())
                        .doBeforeRetry(retrySignal -> log.warn(
                                "Retrying GitHub API call due to {}. Attempt {}/{}",
                                retrySignal.failure().toString(),
                                retrySignal.totalRetries() + 1,
                                githubApiProperties.getApi().getRetry().getAttempts()))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal
                                .failure()))
                .doOnSuccess(response -> log.info(
                        "GitHub API call succeeded, total repositories found: {}",
                        response.getTotalCount()))
                .doOnError(ApiException.class,
                        ex -> log.error("API Exception during GitHub call: {}",
                                ex.getMessage()))
                .doOnError(e -> log.error("Unexpected error during GitHub API call: {}", e.getMessage(),
                        e));
    }

    public Mono<GitRepositoryPaginatedResponse> rateLimitFallback(SearchRequest searchRequest,
                                                                  RequestNotPermitted ex) {
        return Mono.error(new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Please try again later."));
    }
}
