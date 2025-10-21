package com.example.githubsearch.service;

import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import reactor.core.publisher.Mono;

/**
 * Service interface for repository-related business logic.
 */
public interface GitRepositoryService {

    /**
     * Fetches repositories based on the specified search request,
     * applies business logic, including popularity scoring.
     *
     * @param searchRequest search and filter criteria
     * @return Mono emitting enriched repository page response
     */
    Mono<GitRepositoryPaginatedResponse> searchRepositories(SearchRequest searchRequest);
}