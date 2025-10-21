package com.example.githubsearch.controller;

import com.example.githubsearch.dto.ApiErrorResponseDto;
import com.example.githubsearch.dto.GitRepositoryPaginatedResponseDto;
import com.example.githubsearch.dto.SearchRequestDto;
import com.example.githubsearch.mapper.GitRepositoryMapper;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

/**
 * REST controller for handling GitHub repository search requests.
 * <p>
 * Provides endpoints to search repositories with popularity scoring and
 * pagination.
 */
@Slf4j
@RestController
@RequestMapping("/api/gitrepo")
@RequiredArgsConstructor
public class GitRepositoryController {

    private final GitRepositoryService gitRepositoryService;
    private final GitRepositoryMapper gitRepositoryMapper;

    /**
     * Endpoint to search GitHub repositories with popularity scoring.
     *
     * @param searchRequestDTO The search criteria (passed as JSON in POST request).
     * @return A Mono wrapping a paginated response of repository results.
     */
    @Operation(summary = "Search GitHub repositories with popularity scoring")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response"),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiErrorResponseDto.class)))
    })
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GitRepositoryPaginatedResponseDto> searchGitRepositories(@Valid @RequestBody final SearchRequestDto searchRequestDTO) {
        log.info("Received repository search request from user. Request: {}", searchRequestDTO);
        SearchRequest searchRequest = gitRepositoryMapper.toInternal(searchRequestDTO);
        return gitRepositoryService
                .searchRepositories(searchRequest)
                .map(gitRepositoryMapper::toDto);
    }
}