package com.example.githubsearch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Immutable Data Transfer Object representing a single GitHub repository in the API response.
 */
@Value
@Builder
public class RepositoryItemDto {

    /**
     * Repository ID.
     */
    @Schema(description = "Repository ID")
    long id;

    /**
     * Repository name.
     */
    @Schema(description = "Repository name")
    String name;

    /**
     * Repository description.
     */
    @Schema(description = "Repository description")
    String description;

    /**
     * Programming language used in the repository.
     */
    @Schema(description = "Programming language")
    String language;

    /**
     * Number of stargazers.
     */
    @Schema(description = "Number of stargazers")
    int stargazerCount;

    /**
     * Number of forks.
     */
    @Schema(description = "Number of forks")
    int forksCount;

    /**
     * Repository URL.
     */
    @Schema(description = "Repository URL")
    String htmlUrl;

    /**
     * Last update timestamp.
     */
    @Schema(description = "Last update timestamp")
    Instant updatedAt;

    /**
     * Creation timestamp.
     */
    @Schema(description = "Creation timestamp")
    Instant createdAt;

    /**
     * Calculated popularity score.
     */
    @Schema(description = "Calculated popularity score")
    double popularityScore;
}