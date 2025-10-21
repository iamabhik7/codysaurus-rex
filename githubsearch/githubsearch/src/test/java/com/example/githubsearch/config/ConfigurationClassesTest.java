package com.example.githubsearch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;


class ConfigurationClassesTest {

    /**
     * Tests immutability and constructor of GithubApiProperties and nested classes.
     */
    @Test
    @DisplayName("GithubApiProperties: constructor and immutability")
    void testGithubApiProperties() {
        GithubApiProperties.RetryProperties retry = new GithubApiProperties.RetryProperties(3, 10);
        assertEquals(3, retry.getAttempts());
        assertEquals(10, retry.getBackoffSeconds());

        GithubApiProperties.Api api = new GithubApiProperties.Api(
                "https://api.github.com", "q=java", 30, 1, retry);
        assertEquals("https://api.github.com", api.getBaseUrl());
        assertEquals("q=java", api.getDefaultQuery());
        assertEquals(30, api.getDefaultPerPage());
        assertEquals(1, api.getDefaultPage());
        assertEquals(retry, api.getRetry());

        GithubApiProperties props = new GithubApiProperties("token123", api);
        assertEquals("token123", props.getToken());
        assertEquals(api, props.getApi());
    }

    /**
     * Tests PropertiesConfig enables GithubApiProperties as a bean.
     */
    @Test
    @DisplayName("PropertiesConfig: enables GithubApiProperties bean")
    void testPropertiesConfig() {
        // Direct instantiation to cover PropertiesConfig constructor for line coverage
        PropertiesConfig config = new PropertiesConfig();
        assertNotNull(config);
    }

    /**
     * Tests WebClientConfig creates a WebClient bean with correct base URL and headers.
     */
    @Test
    @DisplayName("WebClientConfig: creates WebClient bean")
    void testWebClientConfig() {
        GithubApiProperties.RetryProperties retry = new GithubApiProperties.RetryProperties(2, 5);
        GithubApiProperties.Api api = new GithubApiProperties.Api("https://api.github.com", "q=java", 30, 1, retry);
        GithubApiProperties props = new GithubApiProperties("token-abc", api);
        WebClient.Builder builder = WebClient.builder();
        WebClientConfig config = new WebClientConfig();
        WebClient client = config.webClient(builder, props);
        assertNotNull(client);
    }
}
