package com.example.githubsearch.service;

import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import reactor.core.publisher.Mono;


/**
 * Abstraction for fetching GitHub repositories from external API.
 * All operations are asynchronous and non-blocking returning Reactor types.
 *
 */
public interface GitRepositoryClient {

    /**
     * Fetches a page of repositories from GitHub asynchronously based on the search request.
     *
     * @param searchRequest encapsulates query, sorting, pagination.
     * @return Mono emitting a {@link GitRepositoryPaginatedResponse} containing results.
     */
    Mono<GitRepositoryPaginatedResponse> fetchRepositories(SearchRequest searchRequest);
}
