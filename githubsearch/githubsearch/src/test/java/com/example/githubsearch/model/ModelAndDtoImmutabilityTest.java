package com.example.githubsearch.model;

import com.example.githubsearch.dto.RepositoryItemDto;
import com.example.githubsearch.dto.GitRepositoryPaginatedResponseDto;
import com.example.githubsearch.dto.SearchRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for model and DTO immutability and builder patterns.
 * Ensures all fields are settable via builder, values are correct, and objects are immutable.
 */
class ModelAndDtoImmutabilityTest {

    /**
     * Test immutability and builder for GitRepositoryItems.
     */
    @Test
    @DisplayName("GitRepositoryItems: builder and immutability")
    void testGitRepositoryItemsBuilderAndImmutability() {
        Instant now = Instant.now();
        GitRepositoryItems item = GitRepositoryItems.builder()
                .id(1L)
                .name("repo")
                .description("desc")
                .language("Java")
                .stargazerCount(10)
                .forksCount(2)
                .htmlUrl("http://github.com/repo")
                .updatedAt(now)
                .createdAt(now.minusSeconds(1000))
                .popularityScore(99.9)
                .build();
        assertEquals(1L, item.getId());
        assertEquals("repo", item.getName());
        assertEquals("desc", item.getDescription());
        assertEquals("Java", item.getLanguage());
        assertEquals(10, item.getStargazerCount());
        assertEquals(2, item.getForksCount());
        assertEquals("http://github.com/repo", item.getHtmlUrl());
        assertEquals(now, item.getUpdatedAt());
        assertEquals(now.minusSeconds(1000), item.getCreatedAt());
        assertEquals(99.9, item.getPopularityScore());
        // Immutability: no setters, fields are final
        assertThrows(NoSuchMethodException.class, () -> item.getClass().getDeclaredMethod("setName", String.class));
        // toBuilder
        GitRepositoryItems copy = item.toBuilder().name("new").build();
        assertEquals("new", copy.getName());
        assertEquals(item.getId(), copy.getId());
        assertNotEquals(item, copy);
        assertNotEquals(item.hashCode(), copy.hashCode());
        assertTrue(item.toString().contains("repo"));
    }

    /**
     * Test immutability and builder for GitRepositoryPaginatedResponse.
     */
    @Test
    @DisplayName("GitRepositoryPaginatedResponse: builder and immutability")
    void testGitRepositoryPaginatedResponseBuilderAndImmutability() {
        GitRepositoryItems item = GitRepositoryItems.builder().id(1L).name("repo").build();
        List<GitRepositoryItems> items = List.of(item);
        GitRepositoryPaginatedResponse resp = GitRepositoryPaginatedResponse.builder()
                .totalCount(5)
                .incompleteResults(false)
                .hasNextPage(true)
                .pageNumber(1)
                .nextPageNumber(2)
                .items(items)
                .build();
        assertEquals(5, resp.getTotalCount());
        assertFalse(resp.isIncompleteResults());
        assertTrue(resp.isHasNextPage());
        assertEquals(1, resp.getPageNumber());
        assertEquals(2, resp.getNextPageNumber());
        assertEquals(items, resp.getItems());
        assertThrows(NoSuchMethodException.class, () -> resp.getClass().getDeclaredMethod("setTotalCount", int.class));
        GitRepositoryPaginatedResponse copy = resp.toBuilder().totalCount(10).build();
        assertEquals(10, copy.getTotalCount());
        assertNotEquals(resp, copy);
        assertTrue(resp.toString().contains("totalCount=5"));
    }

    /**
     * Test immutability and builder for SearchRequest.
     */
    @Test
    @DisplayName("SearchRequest: builder and immutability")
    void testSearchRequestBuilderAndImmutability() {
        Instant now = Instant.now();
        SearchRequest req = SearchRequest.builder()
                .language("Java")
                .earliestCreatedDate(now)
                .pageNumber(3)
                .build();
        assertEquals("Java", req.getLanguage());
        assertEquals(now, req.getEarliestCreatedDate());
        assertEquals(3, req.getPageNumber());
        assertThrows(NoSuchMethodException.class, () -> req.getClass().getDeclaredMethod("setLanguage", String.class));
        SearchRequest copy = req.toBuilder().pageNumber(4).build();
        assertEquals(4, copy.getPageNumber());
        assertNotEquals(req, copy);
        assertTrue(req.toString().contains("Java"));
    }

    /**
     * Test immutability and builder for RepositoryItemDto.
     */
    @Test
    @DisplayName("RepositoryItemDto: builder and immutability")
    void testRepositoryItemDtoBuilderAndImmutability() {
        Instant now = Instant.now();
        RepositoryItemDto dto = RepositoryItemDto.builder()
                .id(2L)
                .name("repo2")
                .description("desc2")
                .language("Kotlin")
                .stargazerCount(20)
                .forksCount(5)
                .htmlUrl("http://github.com/repo2")
                .updatedAt(now)
                .createdAt(now.minusSeconds(500))
                .popularityScore(88.8)
                .build();
        assertEquals(2L, dto.getId());
        assertEquals("repo2", dto.getName());
        assertEquals("desc2", dto.getDescription());
        assertEquals("Kotlin", dto.getLanguage());
        assertEquals(20, dto.getStargazerCount());
        assertEquals(5, dto.getForksCount());
        assertEquals("http://github.com/repo2", dto.getHtmlUrl());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(now.minusSeconds(500), dto.getCreatedAt());
        assertEquals(88.8, dto.getPopularityScore());
        assertThrows(NoSuchMethodException.class, () -> dto.getClass().getDeclaredMethod("setName", String.class));
        assertTrue(dto.toString().contains("repo2"));
    }

    /**
     * Test immutability and builder for GitRepositoryPaginatedResponseDto.
     */
    @Test
    @DisplayName("GitRepositoryPaginatedResponseDto: builder and immutability")
    void testGitRepositoryPaginatedResponseDtoBuilderAndImmutability() {
        RepositoryItemDto dto = RepositoryItemDto.builder().id(3L).name("repo3").build();
        List<RepositoryItemDto> items = List.of(dto);
        GitRepositoryPaginatedResponseDto resp = GitRepositoryPaginatedResponseDto.builder()
                .totalCount(7)
                .incompleteResults(true)
                .hasNextPage(false)
                .currentPageNumber(1)
                .nextPageNumber(null)
                .items(items)
                .build();
        assertEquals(7, resp.getTotalCount());
        assertTrue(resp.isIncompleteResults());
        assertFalse(resp.isHasNextPage());
        assertEquals(1, resp.getCurrentPageNumber());
        assertNull(resp.getNextPageNumber());
        assertEquals(items, resp.getItems());
        assertThrows(NoSuchMethodException.class, () -> resp.getClass().getDeclaredMethod("setTotalCount", int.class));
        assertTrue(resp.toString().contains("totalCount=7"));
    }

    /**
     * Test immutability and builder for SearchRequestDto.
     */
    @Test
    @DisplayName("SearchRequestDto: builder and immutability")
    void testSearchRequestDtoBuilderAndImmutability() {
        Instant now = Instant.now();
        SearchRequestDto dto = SearchRequestDto.builder()
                .language("Scala")
                .earliestCreatedDate(now)
                .pageNumber(2)
                .build();
        assertEquals("Scala", dto.getLanguage());
        assertEquals(now, dto.getEarliestCreatedDate());
        assertEquals(2, dto.getPageNumber());
        assertThrows(NoSuchMethodException.class, () -> dto.getClass().getDeclaredMethod("setLanguage", String.class));
        assertTrue(dto.toString().contains("Scala"));
    }
}
