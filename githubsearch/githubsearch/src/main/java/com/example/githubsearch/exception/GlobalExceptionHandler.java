package com.example.githubsearch.exception;

import com.example.githubsearch.dto.ApiErrorResponseDto;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * <p>
 * Handles custom and generic exceptions, providing meaningful, structured error
 * responses.
 * Centralizes error logging and response formatting for the application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom {@link ApiException} thrown during API calls.
     * Logs the error and returns a structured error response.
     *
     * @param ex the {@link ApiException} instance
     * @return ResponseEntity with error message and HTTP status from the exception
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponseDto> handleApiException(ApiException ex) {
        log.error("ApiException: status={}, message={}", ex.getStatus(), ex.getMessage(), ex);
        ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                .status(ex.getStatus().value())
                .error("API Error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Handles validation errors for method arguments.
     *
     * @param ex the exception
     * @return ResponseEntity with validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .message("One or more fields are invalid.")
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles constraint violations (e.g., from @Valid).
     *
     * @param ex the exception
     * @return ResponseEntity with constraint violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation failed")
                .message("Constraint violation.")
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles all uncaught exceptions in the application.
     * Traverses the cause chain to find the root cause and returns a structured
     * error response.
     * Provides custom messaging for common deserialization and type mismatch
     * errors.
     *
     * @param ex the exception
     * @return ResponseEntity with error details and appropriate HTTP status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleAllExceptions(Exception ex) {
        // Handle invalid date format for Instant deserialization
        Throwable decodingCause = findCause(ex, DecodingException.class);
        if (decodingCause != null) {
            Throwable rootCause = decodingCause.getCause();
            if (rootCause instanceof InvalidFormatException invalidFormatException) {
                if ("java.time.Instant".equals(invalidFormatException.getTargetType().getTypeName())) {
                    ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .error("Invalid date format")
                            .message(
                                    "Invalid date format for 'earliestCreatedDate'. Please use ISO-8601 date-time format: yyyy-MM-ddTHH:mm:ssZ , e.g., '2025-10-16T22:29:00Z'")
                            .build();
                    log.warn("Invalid date format for 'earliestCreatedDate': {}", invalidFormatException.getValue());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
        }

        // Handle illegal arguments
        Throwable cause = findCause(ex, IllegalArgumentException.class);
        if (cause != null && cause instanceof IllegalArgumentException iae) {
            ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(iae.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Handle malformed JSON (JsonParseException)
        Throwable jsonParseCause = findCause(ex, com.fasterxml.jackson.core.JsonParseException.class);
        if (jsonParseCause != null) {
            String msg = jsonParseCause.getMessage();
            // Custom message for type mismatch in pageNumber
            if (msg.contains("Unrecognized token")) {
                if (msg.contains("pageNumber")) {
                    msg = "Was expecting a Number for field 'pageNumber'.";
                } else {
                    msg = "Was expecting a Number in the request body.";
                }
            }
            ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Malformed JSON request")
                    .message(msg)
                    .build();
            log.warn("Malformed JSON request: {}", msg);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // generic fallback
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ApiErrorResponseDto response = ApiErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Unexpected error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Recursively searches the cause chain of an exception to find the first
     * instance of the specified exception type.
     *
     * @param throwable            the throwable to search
     * @param targetExceptionClass the exception class to look for
     * @return the first matching throwable in the cause chain, or null if not found
     */
    private Throwable findCause(Throwable throwable, Class<?> targetExceptionClass) {
        while (throwable != null) {
            if (targetExceptionClass.isInstance(throwable)) {
                return throwable;
            }
            throwable = throwable.getCause();
        }
        return null;
    }
}