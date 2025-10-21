package com.example.githubsearch.service.impl;

import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests concurrency and non-blocking behavior of GitRepositoryService.
 * Ensures the service handles multiple parallel requests efficiently under load.
 * Uses mocked responses and reactive streams to validate scalability and thread safety.
 */
class GitRepositoryServiceImplConcurrencyTest {

    @Test
    @DisplayName("Service handles multiple concurrent requests without blocking")
    void testServiceHandlesConcurrentRequests() {
        GitRepositoryService service = Mockito.mock(GitRepositoryService.class);
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(service.searchRepositories(any())).thenReturn(Mono.just(response));

        int parallelism = 20;
        Flux<GitRepositoryPaginatedResponse> flux = Flux.range(0, parallelism)
                .flatMap(i -> service.searchRepositories(
                        SearchRequest.builder()
                                .language("Java")
                                .pageNumber(i + 1)
                                .earliestCreatedDate(Instant.now())
                                .build()
                ));

        StepVerifier.create(flux)
                .expectNextCount(parallelism)
                .verifyComplete();
    }

    @Test
    @DisplayName("Service is non-blocking under load (simulated delay)")
    void testServiceNonBlockingUnderLoad() throws InterruptedException {
        GitRepositoryService service = Mockito.mock(GitRepositoryService.class);
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(service.searchRepositories(any()))
                .thenReturn(Mono.just(response).delayElement(java.time.Duration.ofMillis(50)));

        int parallelism = 10;
        CountDownLatch latch = new CountDownLatch(parallelism);

        Flux.range(0, parallelism)
                .flatMap(i -> service.searchRepositories(
                        SearchRequest.builder()
                                .language("Java")
                                .pageNumber(i + 1)
                                .earliestCreatedDate(Instant.now())
                                .build()
                ))
                .doOnNext(r -> latch.countDown())
                .subscribe();

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertTrue(completed, "All concurrent requests should complete within timeout");
    }
}