package com.example.githubsearch.service.impl.helper;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.model.SearchRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

/**
 * Helper class for building GitHub search query strings.
 * <p>
 * Constructs queries with language and date filters for API requests.
 */
@Slf4j
public class GitHubQueryBuilder {

    /**
     * Builds the search query string including language qualifier and creation date
     * filter.
     * If language is blank, returns date filter only.
     */
    public static String buildSearchQuery(final SearchRequest searchRequest) {
        try {
            final StringBuilder queryBuilder = new StringBuilder();

            if (searchRequest.getLanguage() != null && !searchRequest.getLanguage().isBlank()) {
                queryBuilder.append("language:").append(searchRequest.getLanguage().trim());
            }

            if (searchRequest.getEarliestCreatedDate() != null) {
                String createdFilter = "created:>="
                        + DateTimeFormatter.ISO_INSTANT.format(searchRequest.getEarliestCreatedDate());
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("+");
                }
                queryBuilder.append(createdFilter);
            }

            return queryBuilder.toString();
        } catch (Exception e) {
            log.error("Error building the search query", e);
            return "";
        }
    }

    /**
     * Builds the full URI string for GitHub repository search.
     * Uses configured defaults for page and per_page.
     */
    public static String buildUri(final GithubApiProperties properties, final SearchRequest searchRequest) {
        try {
            final String rawQuery = buildSearchQuery(searchRequest);

            // Determine the page number: use SearchRequest.page if present, else default
            Integer page = searchRequest.getPageNumber();
            int pageNumber = (page != null) ? page : properties.getApi().getDefaultPage();

            if (rawQuery.isBlank()) {
                String fallbackUri = properties.getApi().getBaseUrl() + "/search/repositories?q="
                        + properties.getApi().getDefaultQuery()
                        + "&page=" + properties.getApi().getDefaultPage()
                        + "&per_page=" + properties.getApi().getDefaultPerPage();
                log.debug("No user input detected, using fallback URL: {}", fallbackUri);
                return fallbackUri;
            }

            StringBuilder uriBuilder = new StringBuilder(properties.getApi().getBaseUrl())
                    .append("/search/repositories?q=")
                    .append(rawQuery)
                    .append("&page=").append(pageNumber)
                    .append("&per_page=").append(properties.getApi().getDefaultPerPage());

            log.debug("Built full URI: {}", uriBuilder.toString());
            return uriBuilder.toString();
        } catch (Exception e) {
            String fallbackUri = properties.getApi().getBaseUrl() + "/search/repositories?q="
                    + properties.getApi().getDefaultQuery()
                    + "&page=" + properties.getApi().getDefaultPage()
                    + "&per_page=" + properties.getApi().getDefaultPerPage();
            log.error("Exception building URI, returning fallback URI: {}", fallbackUri, e);
            return fallbackUri;
        }
    }
}
