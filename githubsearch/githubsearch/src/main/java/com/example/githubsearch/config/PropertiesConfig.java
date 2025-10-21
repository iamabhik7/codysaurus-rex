package com.example.githubsearch.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable binding of external properties to
 * immutable, type-safe configuration classes.
 * Registers {@link GithubApiProperties} as a Spring bean.
 */
@Configuration
@EnableConfigurationProperties(GithubApiProperties.class)
public class PropertiesConfig {
}
