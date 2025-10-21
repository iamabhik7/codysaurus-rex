package com.example.githubsearch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Immutable Data Transfer Object for paginated repository search response.
 * Used as the output payload for repository search API.
 */
@Value
@Builder
public class GitRepositoryPaginatedResponseDto {

    /**
     * Total number of repositories matching the search query.
     */
    @Schema(description = "Total number of repositories matching the search query")
    int totalCount;

    /**
     * Flag indicating if the search results are incomplete.
     */
    @Schema(description = "Flag indicating if the search results are incomplete")
    boolean incompleteResults;

    /**
     * True if there exists at least one more page of results beyond the current page.
     */
    @Schema(description = "True if there exists at least one more page of results beyond the current page")
    boolean hasNextPage;

    /**
     * Current page number.
     */
    @Schema(description = "Current page number")
    Integer currentPageNumber;

    /**
     * Next page number, or null if there is no next page.
     */
    @Schema(description = "Next page number, or null if there is no next page")
    Integer nextPageNumber;

    /**
     * List of repositories returned in this page.
     */
    @Schema(description = "List of repositories returned in this page")
    List<RepositoryItemDto> items;
}