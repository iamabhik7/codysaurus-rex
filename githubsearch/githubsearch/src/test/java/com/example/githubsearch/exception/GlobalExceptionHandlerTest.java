package com.example.githubsearch.exception;

import com.example.githubsearch.dto.ApiErrorResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.Path;

/**
 * Unit tests for {@link GlobalExceptionHandler} and {@link ApiException}
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * Tests ApiException constructors and getStatus().
     */
    @Test
    @DisplayName("ApiException: constructors and getStatus")
    void testApiExceptionConstructors() {
        ApiException ex1 = new ApiException(HttpStatus.NOT_FOUND, "Not found");
        assertEquals(HttpStatus.NOT_FOUND, ex1.getStatus());
        assertEquals("Not found", ex1.getMessage());
        assertNull(ex1.getCause());

        Exception cause = new Exception("root");
        ApiException ex2 = new ApiException(HttpStatus.BAD_REQUEST, "Bad req", cause);
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatus());
        assertEquals("Bad req", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }

    /**
     * Tests handleApiException returns correct response.
     */
    @Test
    @DisplayName("handleApiException: returns structured error response")
    void testHandleApiException() {
        ApiException ex = new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleApiException(ex);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.FORBIDDEN.value(), body.getStatus());
        assertEquals("API Error", body.getError());
        assertEquals("Forbidden", body.getMessage());
    }

    /**
     * Tests handleValidationException returns correct error details.
     */
    @Test
    @DisplayName("handleValidationException: returns validation error details")
    void testHandleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        FieldError fe1 = new FieldError("obj", "field1", "must not be null");
        FieldError fe2 = new FieldError("obj", "field2", "invalid");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fe1, fe2));
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.getError());
        assertTrue(body.getDetails().contains("field1: must not be null"));
        assertTrue(body.getDetails().contains("field2: invalid"));
    }

    /**
     * Tests handleConstraintViolationException returns correct error details.
     */
    @Test
    @DisplayName("handleConstraintViolationException: returns constraint violation details")
    void testHandleConstraintViolationException() {
        ConstraintViolation<?> v1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> v2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        when(path1.toString()).thenReturn("field1");
        when(path2.toString()).thenReturn("field2");
        when(v1.getPropertyPath()).thenReturn(path1);
        when(v1.getMessage()).thenReturn("must be positive");
        when(v2.getPropertyPath()).thenReturn(path2);
        when(v2.getMessage()).thenReturn("invalid");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(v1, v2));
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleConstraintViolationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Validation failed", body.getError());
        assertTrue(body.getDetails().stream().anyMatch(s -> s.contains("field1")));
        assertTrue(body.getDetails().stream().anyMatch(s -> s.contains("field2")));
    }

    /**
     * Tests handleAllExceptions: invalid date format for Instant.
     */
    @Test
    @DisplayName("handleAllExceptions: invalid date format for Instant")
    void testHandleAllExceptionsInvalidDateFormat() {
        // Simulate DecodingException -> InvalidFormatException for Instant
        com.fasterxml.jackson.databind.exc.InvalidFormatException ife =
                new com.fasterxml.jackson.databind.exc.InvalidFormatException(null, "bad", "bad", java.time.Instant.class);
        org.springframework.core.codec.DecodingException de = new org.springframework.core.codec.DecodingException("decode", ife);
        Exception ex = new Exception(de);
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleAllExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Invalid date format", body.getError());
        assertTrue(body.getMessage().contains("earliestCreatedDate"));
    }

    /**
     * Tests handleAllExceptions: IllegalArgumentException.
     */
    @Test
    @DisplayName("handleAllExceptions: IllegalArgumentException")
    void testHandleAllExceptionsIllegalArgument() {
        Exception ex = new IllegalArgumentException("bad arg");
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleAllExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Bad Request", body.getError());
        assertEquals("bad arg", body.getMessage());
    }

    /**
     * Tests handleAllExceptions: Malformed JSON (JsonParseException).
     */
    @Test
    @DisplayName("handleAllExceptions: Malformed JSON")
    void testHandleAllExceptionsMalformedJson() {
        com.fasterxml.jackson.core.JsonParseException jpe =
                new com.fasterxml.jackson.core.JsonParseException(null, "Unrecognized token 'foo' (for pageNumber)");
        Exception ex = new Exception(jpe);
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleAllExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Malformed JSON request", body.getError());
        assertTrue(body.getMessage().contains("pageNumber"));
    }

    /**
     * Tests handleAllExceptions: generic fallback.
     */
    @Test
    @DisplayName("handleAllExceptions: generic fallback")
    void testHandleAllExceptionsGenericFallback() {
        Exception ex = new Exception("something went wrong");
        ResponseEntity<ApiErrorResponseDto> resp = handler.handleAllExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ApiErrorResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("Unexpected error", body.getError());
        assertEquals("something went wrong", body.getMessage());
    }
}
