package com.example.githubsearch.service.impl.helper;

import com.example.githubsearch.model.GitRepositoryItems;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.time.Instant;

/**
 * Helper class for calculating popularity score of GitHub repositories.
 * <p>
 * Uses stars, forks, and recency to compute a weighted score.
 */
@Slf4j
public class PopularityScoreCalculator {

    /**
     * Calculates the popularity score for a GitHub repository.
     * The formula weights stars, forks, and recency of updates.
     *
     * @param gitRepositoryItems The repository to calculate score for.
     * @return A new repository object with the popularity score applied.
     */
    public static GitRepositoryItems calculateScore(final GitRepositoryItems gitRepositoryItems) {
        try {
            final int stars = gitRepositoryItems.getStargazerCount();
            final int forks = gitRepositoryItems.getForksCount();
            long daysSinceLastUpdate = 0;

            if (gitRepositoryItems.getUpdatedAt() != null) {
                daysSinceLastUpdate = Duration.between(gitRepositoryItems.getUpdatedAt(),
                        Instant.now()).toDays();
            }

            final double recencyFactor = 1.0 / (1 + daysSinceLastUpdate);
            final double popularityScore = stars * 0.6 + forks * 0.3 + recencyFactor * 100 * 0.1;

            return gitRepositoryItems.toBuilder()
                    .popularityScore(popularityScore)
                    .build();
        } catch (final Exception e) {
            log.error("Error calculating popularity score for repo id={} name={}",
                    gitRepositoryItems.getId(), gitRepositoryItems.getName(), e);

            return gitRepositoryItems.toBuilder()
                    .popularityScore(0)
                    .build();
        }
    }
}
