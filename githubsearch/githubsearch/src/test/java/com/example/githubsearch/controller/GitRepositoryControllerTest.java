
package com.example.githubsearch.controller;

import com.example.githubsearch.dto.GitRepositoryPaginatedResponseDto;
import com.example.githubsearch.dto.SearchRequestDto;
import com.example.githubsearch.mapper.GitRepositoryMapper;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GitRepositoryController}.
 */
public class GitRepositoryControllerTest {

    @Mock
    private GitRepositoryService gitRepositoryService;

    @Mock
    private GitRepositoryMapper gitRepositoryMapper;

    @InjectMocks
    private GitRepositoryController gitRepositoryController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(gitRepositoryController).build();
    }

    @Test
    @DisplayName("Valid search request returns expected response")
    void testValidSearchRequestReturnsResponse() {
        SearchRequest internal = SearchRequest.builder()
                .language("Java")
                .earliestCreatedDate(null)
                .pageNumber(1)
                .build();
        GitRepositoryPaginatedResponse paginatedResponse = GitRepositoryPaginatedResponse.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .pageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();
        GitRepositoryPaginatedResponseDto responseDto = GitRepositoryPaginatedResponseDto.builder()
                .totalCount(1)
                .incompleteResults(false)
                .hasNextPage(false)
                .currentPageNumber(1)
                .nextPageNumber(null)
                .items(Collections.emptyList())
                .build();

        when(gitRepositoryMapper.toInternal(any(SearchRequestDto.class))).thenReturn(internal);
        when(gitRepositoryService.searchRepositories(any(SearchRequest.class)))
                .thenReturn(Mono.just(paginatedResponse));
        when(gitRepositoryMapper.toDto(any(GitRepositoryPaginatedResponse.class))).thenReturn(responseDto);

        webTestClient.post()
                .uri("/api/gitrepo/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"language\":\"Java\",\"pageNumber\":1}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(GitRepositoryPaginatedResponseDto.class)
                .isEqualTo(responseDto);

        verify(gitRepositoryMapper).toInternal(any(SearchRequestDto.class));
        verify(gitRepositoryService).searchRepositories(any(SearchRequest.class));
        verify(gitRepositoryMapper).toDto(any(GitRepositoryPaginatedResponse.class));
    }

    @Test
    @DisplayName("Validation error returns Bad Request")
    void testValidationErrorReturnsBadRequest() {
        // pageNumber is invalid (0)
        webTestClient.post()
                .uri("/api/gitrepo/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"language\":\"Java\",\"pageNumber\":0}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Malformed JSON returns Bad Request")
    void testMalformedJsonReturnsBadRequest() {
        webTestClient.post()
                .uri("/api/gitrepo/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"language\":\"Java\",\"pageNumber\":x}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Service throws exception returns Internal Server Error")
    void testServiceThrowsExceptionReturnsInternalServerError() {
        SearchRequest internal = SearchRequest.builder()
                .language("Java")
                .pageNumber(1)
                .build();

        when(gitRepositoryMapper.toInternal(any(SearchRequestDto.class))).thenReturn(internal);
        when(gitRepositoryService.searchRepositories(any(SearchRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Rate limit exceeded")));

        webTestClient.post()
                .uri("/api/gitrepo/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"language\":\"Java\",\"pageNumber\":1}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/gitrepo/search with negative pageNumber returns 400 Bad Request")
    void testSearchRepositoriesNegativePageNumberReturnsBadRequest() {
        SearchRequestDto requestDto = SearchRequestDto.builder()
                .language("Java")
                .earliestCreatedDate(Instant.parse("2025-10-01T22:29:00Z"))
                .pageNumber(-1)
                .build();

        webTestClient.post()
                .uri("/api/gitrepo/search")
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
