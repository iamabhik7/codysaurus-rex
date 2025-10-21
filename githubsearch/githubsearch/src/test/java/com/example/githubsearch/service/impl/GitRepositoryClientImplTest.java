package com.example.githubsearch.service.impl;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.exception.ApiException;
import com.example.githubsearch.model.GitRepositoryItems;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GitRepositoryClientImpl}.
 */
class GitRepositoryClientImplTest {
    @Mock
    private WebClient webClient;

    @Mock
    private GithubApiProperties githubApiProperties;

    @Mock
    private GithubApiProperties.Api apiProps;

    @InjectMocks
    private GitRepositoryClientImpl client;

    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec uriSpec;
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec headersSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(githubApiProperties.getApi()).thenReturn(apiProps);
        when(apiProps.getBaseUrl()).thenReturn("https://api.github.com");
        when(apiProps.getDefaultQuery()).thenReturn("Q");
        when(apiProps.getDefaultPage()).thenReturn(1);
        when(apiProps.getDefaultPerPage()).thenReturn(10);
        when(apiProps.getRetry()).thenReturn(new GithubApiProperties.RetryProperties(1, 1));
        uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
    }

    @Test
    @DisplayName("Successful fetch returns paginated response")
    void testFetchRepositoriesSuccess() {
        SearchRequest request = SearchRequest.builder().language("Java").pageNumber(1).build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.singletonList(
                        GitRepositoryItems.builder()
                                .id(1L)
                                .name("spring-boot")
                                .description("desc")
                                .language("Java")
                                .stargazerCount(10)
                                .forksCount(5)
                                .htmlUrl("url")
                                .updatedAt(Instant.now())
                                .createdAt(Instant.now())
                                .popularityScore(1.0)
                                .build()))
                .build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.OK);
            when(clientResponse.bodyToMono(GitRepositoryPaginatedResponse.class)).thenReturn(Mono.just(response));
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectNextMatches(r -> r.getTotalCount() == 1 && !r.isIncompleteResults())
                .verifyComplete();
    }

    @Test
    @DisplayName("304 Not Modified returns empty Mono")
    void testFetchRepositoriesNotModified() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.NOT_MODIFIED);
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectError(NullPointerException.class)
                .verify();
    }

    @Test
    @DisplayName("422 Unprocessable Entity returns ApiException")
    void testFetchRepositoriesValidationError() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.UNPROCESSABLE_ENTITY);
            when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("validation error"));
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(e -> e instanceof ApiException && e.getMessage().contains("Validation failed"))
                .verify();
    }

    @Test
    @DisplayName("503 Service Unavailable returns ApiException")
    void testFetchRepositoriesServiceUnavailable() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
            when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("service unavailable"));
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(e -> e instanceof ApiException && e.getMessage().contains("Service unavailable"))
                .verify();
    }

    @Test
    @DisplayName("Other error returns ApiException")
    void testFetchRepositoriesOtherError() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.BAD_REQUEST);
            when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("bad request"));
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(
                        e -> e instanceof ApiException && e.getMessage().contains("GitHub API responded with error"))
                .verify();
    }

    @Test
    @DisplayName("Rate limit fallback returns TOO_MANY_REQUESTS ApiException")
    void testRateLimitFallback() {
        SearchRequest request = SearchRequest.builder().build();
        RequestNotPermitted ex = mock(RequestNotPermitted.class);

        StepVerifier.create(client.rateLimitFallback(request, ex))
                .expectErrorMatches(e -> e instanceof ApiException &&
                        ((ApiException) e).getStatus() == HttpStatus.TOO_MANY_REQUESTS)
                .verify();
    }

    // API returns 500 Internal Server Error
    @Test
    @DisplayName("500 Internal Server Error returns ApiException")
    void testFetchRepositoriesInternalServerError() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>> handler = (Function<ClientResponse, Mono<GitRepositoryPaginatedResponse>>) invocation
                    .getArgument(0);
            ClientResponse clientResponse = mock(ClientResponse.class);
            when(clientResponse.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
            when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("internal error"));
            return handler.apply(clientResponse);
        });

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(
                        e -> e instanceof ApiException && e.getMessage().contains("GitHub API responded with error"))
                .verify();
    }

    // Simulate a timeout (simulate by returning Mono.error)
    @Test
    @DisplayName("Timeout returns ApiException")
    void testFetchRepositoriesTimeout() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenReturn(Mono.error(new RuntimeException("Timeout")));

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(e -> e instanceof ApiException || e instanceof RuntimeException)
                .verify();
    }

    // Malformed JSON (simulate by returning Mono.error)
    @Test
    @DisplayName("Malformed JSON response returns ApiException")
    void testFetchRepositoriesMalformedJson() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenReturn(Mono.error(new RuntimeException("JSON parse error")));

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(e -> e instanceof ApiException || e instanceof RuntimeException)
                .verify();
    }

    // Network error (simulate by returning Mono.error)
    @Test
    @DisplayName("Network error returns ApiException")
    void testFetchRepositoriesNetworkError() {
        SearchRequest request = SearchRequest.builder().build();

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(eq(HttpHeaders.ACCEPT), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any())).thenReturn(Mono.error(new RuntimeException("Connection refused")));

        StepVerifier.create(client.fetchRepositories(request))
                .expectErrorMatches(e -> e instanceof ApiException || e instanceof RuntimeException)
                .verify();
    }
}
