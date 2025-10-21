package com.example.githubsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Immutable model representing a paginated response for GitHub repository search.
 * <p>
 * Contains metadata and a list of repository items for the current page.
 */
@Value
@Builder(toBuilder = true)
public class GitRepositoryPaginatedResponse {

    /**
     * Total number of repositories matching the search query.
     */
    @JsonProperty("total_count")
    int totalCount;

    /**
     * Flag indicating if the search results are incomplete.
     */
    @JsonProperty("incomplete_results")
    boolean incompleteResults;

    /**
     * True if there exists at least one more page of results beyond the current page.
     */
    boolean hasNextPage;

    Integer pageNumber;

    Integer nextPageNumber;

    /**
     * List of repositories returned in this page.
     */
    List<GitRepositoryItems> items;
}
