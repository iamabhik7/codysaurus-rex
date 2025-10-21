package com.example.githubsearch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Immutable Data Transfer Object for search request parameters sent by the client.
 * Used as the input payload for repository search API.
 */
@Value
@Builder
public class SearchRequestDto {

    /**
     * Programming language to filter by.
     */
    @Schema(description = "Programming language to filter by", example = "Java")
    @Pattern(regexp = "^[a-zA-Z0-9+#\\-]+( [a-zA-Z0-9+#\\-]+)*$", message = "Invalid language format")
    String language;

    /**
     * Earliest creation date (ISO-8601).
     */
    @Schema(description = "Earliest creation date (ISO-8601)", example = "2025-10-16T22:29:00Z")
    Instant earliestCreatedDate;

    /**
     * Page number for pagination (minimum 1).
     */
    @Schema(description = "Page number (1-9)", example = "1", minimum = "1", maximum = "9")
    @Min(value = 1, message = "Page number must be at least 1")
    Integer pageNumber;
}