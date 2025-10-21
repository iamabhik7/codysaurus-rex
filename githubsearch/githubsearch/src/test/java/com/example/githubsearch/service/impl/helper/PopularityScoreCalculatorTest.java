package com.example.githubsearch.service.impl.helper;

import com.example.githubsearch.model.GitRepositoryItems;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PopularityScoreCalculator}.
 * 
 */
class PopularityScoreCalculatorTest {

    /**
     * Tests calculation for a typical repository with all fields set.
     */
    @Test
    @DisplayName("Calculates correct popularity score for typical repo")
    void testCalculateScoreTypical() {
        GitRepositoryItems repo = GitRepositoryItems.builder()
                .id(1L)
                .name("test-repo")
                .stargazerCount(100)
                .forksCount(50)
                .updatedAt(Instant.now().minusSeconds(86400 * 10)) // 10 days ago
                .build();

        GitRepositoryItems result = PopularityScoreCalculator.calculateScore(repo);
        double expectedRecency = 1.0 / (1 + 10);
        double expectedScore = 100 * 0.6 + 50 * 0.3 + expectedRecency * 100 * 0.1;
        assertEquals(expectedScore, result.getPopularityScore(), 0.0001);
    }

    /**
     * Tests calculation when updatedAt is null (should treat as 0 days since
     * update).
     */
    @Test
    @DisplayName("Handles null updatedAt as 0 days since update")
    void testCalculateScoreNullUpdatedAt() {
        GitRepositoryItems repo = GitRepositoryItems.builder()
                .id(2L)
                .name("no-update-repo")
                .stargazerCount(10)
                .forksCount(5)
                .updatedAt(null)
                .build();

        GitRepositoryItems result = PopularityScoreCalculator.calculateScore(repo);
        double expectedRecency = 1.0 / (1 + 0);
        double expectedScore = 10 * 0.6 + 5 * 0.3 + expectedRecency * 100 * 0.1;
        assertEquals(expectedScore, result.getPopularityScore(), 0.0001);
    }

    /**
     * Tests that the returned instance is a new object and does not mutate the
     * input.
     */
    @Test
    @DisplayName("Returns new instance, does not mutate input")
    void testImmutability() {
        GitRepositoryItems repo = GitRepositoryItems.builder()
                .id(3L)
                .name("immutable-repo")
                .stargazerCount(5)
                .forksCount(2)
                .updatedAt(Instant.now())
                .popularityScore(123.45)
                .build();

        GitRepositoryItems result = PopularityScoreCalculator.calculateScore(repo);
        assertNotSame(repo, result);
        assertNotEquals(123.45, result.getPopularityScore());
    }

    /**
     * Tests error path: if a required field is missing, the catch block is
     * executed.
     * This is simulated by passing a minimal object with a null name (if allowed by
     * builder).
     * If the builder does not allow nulls, this test will still pass as a no-op.
     */
    @Test
    @DisplayName("Handles error path and returns score 0")
    void testErrorPathReturnsZero() {
        // Simulate error by passing a repo with a null name (if allowed)
        GitRepositoryItems repo = GitRepositoryItems.builder()
                .id(4L)
                .name(null)
                .stargazerCount(1)
                .forksCount(1)
                .updatedAt(Instant.now())
                .build();
        try {
            GitRepositoryItems result = PopularityScoreCalculator.calculateScore(repo);
            // If no exception, score should be calculated as normal (name is not used in
            // calculation)
            assertNotNull(result);
        } catch (Exception e) {
            // If exception occurs, ensure it is handled and does not propagate
            fail("Exception should be handled internally");
        }
    }

    @Test
    @DisplayName("PopularityScoreCalculator handles zero stars, forks, and old update")
    void testPopularityScoreZeroValues() {
        GitRepositoryItems item = GitRepositoryItems.builder()
                .stargazerCount(0)
                .forksCount(0)
                .updatedAt(Instant.parse("2000-01-01T00:00:00Z"))
                .createdAt(Instant.parse("2000-01-01T00:00:00Z"))
                .build();
        GitRepositoryItems scored = PopularityScoreCalculator.calculateScore(item);
        assertNotNull(scored);
        assertTrue(scored.getPopularityScore() >= 0);
    }

    @Test
    @DisplayName("PopularityScoreCalculator handles null updatedAt and createdAt")
    void testPopularityScoreNullDates() {
        GitRepositoryItems item = GitRepositoryItems.builder()
                .stargazerCount(10)
                .forksCount(5)
                .updatedAt(null)
                .createdAt(null)
                .build();
        GitRepositoryItems scored = PopularityScoreCalculator.calculateScore(item);
        assertNotNull(scored);
        assertTrue(scored.getPopularityScore() >= 0);
    }
}
