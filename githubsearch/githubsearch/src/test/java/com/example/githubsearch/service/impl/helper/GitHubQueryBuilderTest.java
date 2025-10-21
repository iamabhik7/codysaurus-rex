package com.example.githubsearch.service.impl.helper;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.model.SearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GitHubQueryBuilder}.
 * Ensures 100% line coverage, including normal and error paths.
 */
class GitHubQueryBuilderTest {

    /**
     * Helper to create a minimal GithubApiProperties with nested API config using
     * constructor.
     */
    private GithubApiProperties createProperties() {
        GithubApiProperties.RetryProperties retry = new GithubApiProperties.RetryProperties(3, 2);
        GithubApiProperties.Api api = new GithubApiProperties.Api(
                "https://api.github.com",
                "stars:>1",
                10,
                1,
                retry);
        return new GithubApiProperties("dummy-token", api);
    }

    @Test
    @DisplayName("buildSearchQuery: language and date")
    void testBuildSearchQuery_languageAndDate() {
        SearchRequest req = SearchRequest.builder()
                .language("java")
                .earliestCreatedDate(Instant.parse("2023-01-01T00:00:00Z"))
                .build();
        String expected = "language:java+created:>=2023-01-01T00:00:00Z";
        assertEquals(expected, GitHubQueryBuilder.buildSearchQuery(req));
    }

    @Test
    @DisplayName("buildSearchQuery: language only")
    void testBuildSearchQuery_languageOnly() {
        SearchRequest req = SearchRequest.builder()
                .language("python")
                .build();
        String expected = "language:python";
        assertEquals(expected, GitHubQueryBuilder.buildSearchQuery(req));
    }

    @Test
    @DisplayName("buildSearchQuery: date only")
    void testBuildSearchQuery_dateOnly() {
        SearchRequest req = SearchRequest.builder()
                .earliestCreatedDate(Instant.parse("2022-05-10T12:00:00Z"))
                .build();
        String expected = "created:>=2022-05-10T12:00:00Z";
        assertEquals(expected, GitHubQueryBuilder.buildSearchQuery(req));
    }

    @Test
    @DisplayName("buildSearchQuery: empty request")
    void testBuildSearchQuery_empty() {
        SearchRequest req = SearchRequest.builder().build();
        assertEquals("", GitHubQueryBuilder.buildSearchQuery(req));
    }

    @Test
    @DisplayName("buildUri: full query with custom page")
    void testBuildUri_fullQueryCustomPage() {
        GithubApiProperties props = createProperties();
        SearchRequest req = SearchRequest.builder()
                .language("go")
                .earliestCreatedDate(Instant.parse("2021-12-31T00:00:00Z"))
                .pageNumber(5)
                .build();
        String expected = "https://api.github.com/search/repositories?q=language:go+created:>=2021-12-31T00:00:00Z&page=5&per_page=10";
        assertEquals(expected, GitHubQueryBuilder.buildUri(props, req));
    }

    @Test
    @DisplayName("buildUri: fallback for blank query")
    void testBuildUri_fallbackBlankQuery() {
        GithubApiProperties props = createProperties();
        SearchRequest req = SearchRequest.builder().build();
        String expected = "https://api.github.com/search/repositories?q=stars:>1&page=1&per_page=10";
        assertEquals(expected, GitHubQueryBuilder.buildUri(props, req));
    }

    @Test
    @DisplayName("buildUri: null page number uses default")
    void testBuildUri_nullPageNumber() {
        GithubApiProperties props = createProperties();
        SearchRequest req = SearchRequest.builder()
                .language("js")
                .earliestCreatedDate(Instant.parse("2020-01-01T00:00:00Z"))
                .pageNumber(null)
                .build();
        String expected = "https://api.github.com/search/repositories?q=language:js+created:>=2020-01-01T00:00:00Z&page=1&per_page=10";
        assertEquals(expected, GitHubQueryBuilder.buildUri(props, req));
    }

    @Test
    @DisplayName("buildSearchQuery: null request returns empty string")
    void testBuildSearchQuery_nullRequest() {
        assertEquals("", GitHubQueryBuilder.buildSearchQuery(null));
    }

    @Test
    @DisplayName("buildUri: null properties throws NullPointerException")
    void testBuildUri_nullProperties() {
        SearchRequest req = SearchRequest.builder().build();
        assertThrows(NullPointerException.class, () -> GitHubQueryBuilder.buildUri(null, req));
    }
}
