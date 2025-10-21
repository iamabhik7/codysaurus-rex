package com.example.githubsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Immutable domain model representing a GitHub repository.
 * <p>
 * Contains metadata fields retrieved from the GitHub API.
 */
@Value
@Builder(toBuilder = true)
public class GitRepositoryItems {
    long id;
    String name;
    String description;
    String language;

    @JsonProperty("stargazers_count")
    int stargazerCount;

    @JsonProperty("forks_count")
    int forksCount;

    @JsonProperty("html_url")
    String htmlUrl;

    @JsonProperty("updated_at")
    Instant updatedAt;

    @JsonProperty("created_at")
    Instant createdAt;

    double popularityScore;
}
