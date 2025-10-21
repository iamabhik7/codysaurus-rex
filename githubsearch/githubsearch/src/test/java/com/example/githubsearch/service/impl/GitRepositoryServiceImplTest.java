package com.example.githubsearch.service.impl;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.exception.ApiException;
import com.example.githubsearch.model.GitRepositoryItems;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryClient;
import com.example.githubsearch.service.impl.helper.PopularityScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GitRepositoryServiceImpl}.
 */
class GitRepositoryServiceImplTest {

    @Mock
    private GitRepositoryClient gitRepositoryClient;

    @Mock
    private GithubApiProperties githubApiProperties;

    @Mock
    private GithubApiProperties.Api apiProps;

    @InjectMocks
    private GitRepositoryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(githubApiProperties.getApi()).thenReturn(apiProps);
        when(apiProps.getDefaultPage()).thenReturn(1);
        when(apiProps.getDefaultPerPage()).thenReturn(10);
    }

    @Test
    @DisplayName("Successful repository search returns scored paginated response")
    void testSearchRepositoriesSuccess() {
        SearchRequest request = SearchRequest.builder()
                .language("Java")
                .pageNumber(1)
                .earliestCreatedDate(Instant.parse("2025-10-16T22:29:00Z"))
                .build();

        GitRepositoryItems repoItem = GitRepositoryItems.builder()
                .id(1L)
                .name("spring-boot")
                .description("Spring Boot repo")
                .language("Java")
                .stargazerCount(100)
                .forksCount(50)
                .htmlUrl("https://github.com/spring-projects/spring-boot")
                .updatedAt(Instant.parse("2025-10-15T12:34:56Z"))
                .createdAt(Instant.parse("2014-01-01T00:00:00Z"))
                .popularityScore(0)
                .build();

        double expectedScore = PopularityScoreCalculator.calculateScore(repoItem).getPopularityScore();
        GitRepositoryItems scoredItem = repoItem.toBuilder().popularityScore(expectedScore).build();

        GitRepositoryPaginatedResponse paginatedResponse = GitRepositoryPaginatedResponse.builder()
                .totalCount(15)
                .incompleteResults(false)
                .hasNextPage(true)
                .pageNumber(1)
                .nextPageNumber(2)
                .items(List.of(repoItem))
                .build();

        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(paginatedResponse));

        var resultMono = service.searchRepositories(request);
        var result = resultMono.block();
        assertNotNull(result);
        assertEquals(15, result.getTotalCount());
        assertFalse(result.isIncompleteResults());
        assertTrue(result.isHasNextPage());
        assertEquals(1, result.getPageNumber());
        assertEquals(2, result.getNextPageNumber());
        assertEquals(1, result.getItems().size());
        assertEquals(scoredItem, result.getItems().get(0));
    }

    @Test
    @DisplayName("API exception is handled and logged")
    void testSearchRepositoriesApiException() {
        SearchRequest request = SearchRequest.builder().build();
        ApiException apiException = new ApiException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                "API error");

        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.error(apiException));

        var resultMono = service.searchRepositories(request);
        ApiException thrown = assertThrows(ApiException.class, resultMono::block);
        assertEquals("API error", thrown.getMessage());
        assertEquals(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, thrown.getStatus());
    }

    @Test
    @DisplayName("Unexpected error is handled and logged")
    void testSearchRepositoriesUnexpectedError() {
        SearchRequest request = SearchRequest.builder().build();
        RuntimeException unexpected = new RuntimeException("Unexpected");

        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.error(unexpected));

        var resultMono = service.searchRepositories(request);
        RuntimeException thrown = assertThrows(RuntimeException.class, resultMono::block);
        assertEquals("Unexpected", thrown.getMessage());
    }

    @Test
    @DisplayName("Empty items list handled correctly")
    void testSearchRepositoriesEmptyItems() {
        SearchRequest request = SearchRequest.builder().pageNumber(1).build();

        GitRepositoryPaginatedResponse paginatedResponse = GitRepositoryPaginatedResponse.builder()
                .totalCount(0)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();

        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(paginatedResponse));

        var resultMono = service.searchRepositories(request);
        var result = resultMono.block();
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertFalse(result.isIncompleteResults());
        assertFalse(result.isHasNextPage());
        assertEquals(1, result.getPageNumber());
        assertNull(result.getNextPageNumber());
        assertEquals(Collections.emptyList(), result.getItems());
    }

    @Test
    @DisplayName("Null SearchRequest throws NullPointerException")
    void testSearchRepositoriesNullRequest() {
        assertThrows(NullPointerException.class, () -> service.searchRepositories(null).block());
    }

    @Test
    @DisplayName("Very large page number in SearchRequest handled gracefully")
    void testSearchRepositoriesLargePageNumber() {
        int largePage = Integer.MAX_VALUE;
        SearchRequest request = SearchRequest.builder().pageNumber(largePage).build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(0)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(largePage)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(largePage, result.getPageNumber());
    }

    @Test
    @DisplayName("Item with missing fields handled gracefully")
    void testSearchRepositoriesItemWithMissingFields() {
        GitRepositoryItems incompleteItem = GitRepositoryItems.builder()
                .id(2L)
                .name(null) // missing name
                .description(null)
                .language(null)
                .stargazerCount(0)
                .forksCount(0)
                .htmlUrl(null)
                .updatedAt(null)
                .createdAt(null)
                .popularityScore(0)
                .build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(List.of(incompleteItem))
                .build();
        SearchRequest request = SearchRequest.builder().build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertNull(result.getItems().get(0).getName());
    }

    @Test
    @DisplayName("Downstream client error is propagated")
    void testSearchRepositoriesDownstreamErrorPropagation() {
        SearchRequest request = SearchRequest.builder().build();
        when(gitRepositoryClient.fetchRepositories(request))
                .thenReturn(Mono.error(new IllegalStateException("Downstream error")));
        Exception ex = assertThrows(IllegalStateException.class, () -> service.searchRepositories(request).block());
        assertEquals("Downstream error", ex.getMessage());
    }

    @Test
    @DisplayName("Multiple items in response are processed correctly")
    void testSearchRepositoriesMultipleItems() {
        GitRepositoryItems item1 = GitRepositoryItems.builder().id(1L).name("repo1").popularityScore(1.0).build();
        GitRepositoryItems item2 = GitRepositoryItems.builder().id(2L).name("repo2").popularityScore(2.0).build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(2)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(List.of(item1, item2))
                .build();
        SearchRequest request = SearchRequest.builder().build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        List<String> names = result.getItems().stream().map(GitRepositoryItems::getName).toList();
        assertTrue(names.contains("repo1"));
        assertTrue(names.contains("repo2"));
    }

    @Test
    @DisplayName("Boundary value page number 1 is handled correctly")
    void testSearchRepositoriesPageNumberOne() {
        SearchRequest request = SearchRequest.builder().pageNumber(1).build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(0)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(1, result.getPageNumber());
    }

    @Test
    @DisplayName("Null fields in SearchRequest handled gracefully")
    void testSearchRepositoriesNullFieldsInRequest() {
        SearchRequest request = SearchRequest.builder()
                .language(null)
                .earliestCreatedDate(null)
                .pageNumber(1)
                .build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(0)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(1, result.getPageNumber());
    }

    @Test
    @DisplayName("Client returns response with empty items list")
    void testSearchRepositoriesClientReturnsEmptyItemsList() {
        SearchRequest request = SearchRequest.builder().build();
        GitRepositoryPaginatedResponse response = GitRepositoryPaginatedResponse.builder()
                .totalCount(0)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        when(gitRepositoryClient.fetchRepositories(request)).thenReturn(Mono.just(response));
        var result = service.searchRepositories(request).block();
        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertFalse(result.isIncompleteResults());
        assertFalse(result.isHasNextPage());
        assertEquals(1, result.getPageNumber());
        assertNull(result.getNextPageNumber());
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty());
    }
}
