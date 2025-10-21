package com.example.githubsearch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for setting up the {@link WebClient} bean.
 * This WebClient is pre-configured with GitHub API base URL and standard
 * headers.
 * It also configures maximum in-memory buffer size for response bodies.
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder, GithubApiProperties githubApiProperties) {
        return webClientBuilder.baseUrl(githubApiProperties.getApi().getBaseUrl())
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .defaultHeader("Authorization", "Bearer " + githubApiProperties.getToken())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }
}
