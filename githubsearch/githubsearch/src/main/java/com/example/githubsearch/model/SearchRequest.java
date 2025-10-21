package com.example.githubsearch.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Immutable data transfer object encapsulating parameters for GitHub repository search.
 * <p>
 * Used to pass search criteria from API layer to service layer.
 */
@Value
@Builder(toBuilder = true)
public class SearchRequest {
    @Schema(description = "Programming language to filter by", example = "Java")
    @Pattern(regexp = "^[a-zA-Z0-9+#\\-]+( [a-zA-Z0-9+#\\-]+)*$", message = "Invalid language format")
    String language;

    @Schema(description = "Earliest creation date (ISO-8601)", example = "2025-10-16T22:29:00Z")
    Instant earliestCreatedDate;

    @Schema(description = "Page number (1-9)", example = "1", minimum = "1", maximum = "9")
    @Min(value = 1, message = "Page number must be at least 1")
    Integer pageNumber;
}