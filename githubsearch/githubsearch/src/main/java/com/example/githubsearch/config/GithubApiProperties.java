package com.example.githubsearch.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Immutable, type-safe configuration for GitHub API properties.
 * <p>
 * Holds authentication token and API-specific settings.
 */
@Getter
@ConfigurationProperties(prefix = "github")
public class GithubApiProperties {

    private final String token;
    private final Api api;

    public GithubApiProperties(String token, Api api) {
        this.token = token;
        this.api = api;
    }

    @Getter
    public static class Api {
        private final String baseUrl;
        private final String defaultQuery;
        private final int defaultPerPage;
        private final int defaultPage;
        private final RetryProperties retry;

        public Api(String baseUrl, String defaultQuery, int defaultPerPage, int defaultPage, RetryProperties retry) {
            this.baseUrl = baseUrl;
            this.defaultQuery = defaultQuery;
            this.defaultPerPage = defaultPerPage;
            this.defaultPage = defaultPage;
            this.retry = retry;
        }
    }

    @Getter
    public static class RetryProperties {
        private final int attempts;
        private final int backoffSeconds;

        public RetryProperties(int attempts, int backoffSeconds) {
            this.attempts = attempts;
            this.backoffSeconds = backoffSeconds;
        }
    }
}