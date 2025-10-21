package com.example.githubsearch.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom unchecked exception that encapsulates error details from external API calls,
 * specifically tailored for handling GitHub API failures within the application.
 * Carries the HTTP status and error message returned by the GitHub API and any root cause.
 *
 */
public class ApiException extends RuntimeException{
    private final HttpStatus status;

    /**
     * Constructor with HTTP status and message.
     * @param status HTTP status returned from API
     * @param message descriptive error message
     */
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructor with HTTP status, message, and root cause.
     *
     * @param status HTTP status returned from API
     * @param message descriptive error message
     * @param cause original throwable cause
     */
    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Retrieves the HTTP status associated with the exception.
     * @return HTTP status code
     */
    public HttpStatus getStatus() {
        return status;
    }
}
