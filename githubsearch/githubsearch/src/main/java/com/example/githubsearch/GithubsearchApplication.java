package com.example.githubsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the GitHub Search Spring Boot application.
 * <p>
 * This class bootstraps the application using Spring Boot's auto-configuration.
 */
@SpringBootApplication
public class GithubsearchApplication {

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(GithubsearchApplication.class, args);
	}
}
