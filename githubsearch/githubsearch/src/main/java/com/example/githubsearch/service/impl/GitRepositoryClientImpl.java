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
import reactor.core.publisher.Flux;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import java.time.Duration;

/**
 * Implementation of {@link GitRepositoryClient} for fetching repositories
 * asynchronously from GitHub API.
 * <p>
 * Uses WebClient for async HTTP calls, applies rate limiting, and handles
 * retries and error responses.
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
                Flux<Integer> pageFlux;
                if (searchRequest.getPageNumber() != null) {
                        pageFlux = Flux.just(searchRequest.getPageNumber()); // if pageNumber is specified, fetch only that page
                        log.info("Fetching repositories for specified page number: {}", searchRequest.getPageNumber());
                } else {
                        pageFlux = Flux.range(1, 10); // GitHub allows up to 10 pages (max 1000 results, current default per page is set to 30, so max 300 will be fetched)
                }
                return pageFlux
                        .flatMap(page -> {
                                SearchRequest.SearchRequestBuilder builder = searchRequest.toBuilder();
                                builder.pageNumber(page);
                                SearchRequest pagedRequest = builder.build();
                                final String fullUri = GitHubQueryBuilder.buildUri(githubApiProperties, pagedRequest);
                                log.info("Fetching repositories from page {} with URI: {}", page, fullUri);

                                return webClient.get()
                                        .uri(fullUri)
                                        .header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                                        .exchangeToMono(ResponseHandlerHelper::handleResponse)
                                        .retryWhen(Retry.backoff(
                                                        githubApiProperties.getApi().getRetry().getAttempts(),
                                                        Duration.ofSeconds(githubApiProperties.getApi().getRetry().getBackoffSeconds()))
                                                .filter(throwable -> throwable instanceof ApiException &&
                                                        ((ApiException) throwable).getStatus().is5xxServerError())
                                                .doBeforeRetry(retrySignal -> log.warn(
                                                        "Retrying GitHub API page {} due to {}. Attempt {}/{}",
                                                        page,
                                                        retrySignal.failure().toString(),
                                                        retrySignal.totalRetries() + 1,
                                                        githubApiProperties.getApi().getRetry().getAttempts()))
                                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()))
                                        .onErrorResume(e -> {
                                                log.error("Error fetching page {}: {}", page, e.getMessage());
                                                return Mono.error(e); // Propagate error so tests and callers see it
                                        });
                        })
                        .filter(response -> response != null && response.getItems() != null && !response.getItems().isEmpty())
                        .flatMapIterable(GitRepositoryPaginatedResponse::getItems)
                        .collectList()
                        .map(allItems -> GitRepositoryPaginatedResponse.builder()
                                .totalCount(allItems.size())
                                .incompleteResults(false)
                                .hasNextPage(false)
                                .pageNumber(searchRequest.getPageNumber())
                                .nextPageNumber(null)
                                .items(allItems)
                                .build())
                        .doOnSuccess(response -> log.info(
                                "Successfully fetched total {} repositories from all pages.",
                                response.getTotalCount()))
                        .doOnError(ApiException.class,
                                ex -> log.error("API Exception during GitHub API calls: {}", ex.getMessage()))
                        .doOnError(e -> log.error("Unexpected error during GitHub API calls: {}", e.getMessage(), e));
        }

        public Mono<GitRepositoryPaginatedResponse> rateLimitFallback(SearchRequest searchRequest,
                        RequestNotPermitted ex) {
                return Mono.error(new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                                "Rate limit exceeded. Please try again later."));
        }
}
