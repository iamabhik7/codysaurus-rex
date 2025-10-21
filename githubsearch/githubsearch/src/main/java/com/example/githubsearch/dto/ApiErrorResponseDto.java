package com.example.githubsearch.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * Standard error response structure for all API errors.
 */
@Value
@Builder
public class ApiErrorResponseDto {
    int status;
    String error;
    String message;
    List<String> details; // Field-level or multiple errors
}