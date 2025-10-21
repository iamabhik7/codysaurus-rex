package com.example.githubsearch.service.impl;

import com.example.githubsearch.config.GithubApiProperties;
import com.example.githubsearch.exception.ApiException;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import com.example.githubsearch.service.GitRepositoryClient;
import com.example.githubsearch.service.GitRepositoryService;
import com.example.githubsearch.service.impl.helper.PopularityScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Implementation of {@link GitRepositoryService} for searching and scoring
 * GitHub repositories.
 * <p>
 * Uses reactive streams for async processing and delegates API calls to
 * {@link GitRepositoryClient}.
 * <p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitRepositoryServiceImpl implements GitRepositoryService {

    private final GitRepositoryClient gitRepositoryClient;
    private final GithubApiProperties githubApiProperties;

    /**
     * Searches repositories and computes a popularity score for each.
     *
     * @param searchRequest the search criteria (internal model)
     * @return a reactive Mono of paginated response with scored repositories and
     *         pagination metadata (internal model)
     */
    @Override
    public Mono<GitRepositoryPaginatedResponse> searchRepositories(final SearchRequest searchRequest) {
        log.info("Starting searchRepositories with SearchRequest: {}", searchRequest);

        // Use default page and perPage as SearchRequest does not have pagination fields
        final int defaultPage = githubApiProperties.getApi().getDefaultPage();
        final int defaultPerPage = githubApiProperties.getApi().getDefaultPerPage();

        return gitRepositoryClient.fetchRepositories(searchRequest)
                .flatMap(response -> {
                    int totalCount = response.getTotalCount();
                    int totalPages = (int) Math.ceil((double) totalCount / defaultPerPage);

                    boolean hasNextPage = defaultPage < totalPages;

                    return Flux.fromIterable(response.getItems())
                            .parallel()
                            .runOn(Schedulers.parallel())
                            .map(PopularityScoreCalculator::calculateScore)
                            .sequential()
                            .collectList()
                            .map(scoredItems -> GitRepositoryPaginatedResponse.builder()
                                    .totalCount(totalCount)
                                    .incompleteResults(response.isIncompleteResults())
                                    .hasNextPage(hasNextPage)
                                    .pageNumber(searchRequest.getPageNumber() != null ? searchRequest.getPageNumber()
                                            : defaultPage)
                                    .nextPageNumber(hasNextPage
                                            ? ((searchRequest.getPageNumber() != null ? searchRequest.getPageNumber()
                                            : defaultPage) + 1)
                                            : null)
                                    .items(scoredItems)
                                    .build());
                })
                .doOnSuccess(
                        resp -> log.info("Successfully fetched and scored {} repositories", resp.getItems().size()))
                .doOnError(throwable -> {
                    if (throwable instanceof ApiException apiEx) {
                        log.error("API Exception during repository search: status={}, message={}", apiEx.getStatus(),
                                apiEx.getMessage());
                    } else {
                        log.error("Unexpected error occurred during repository search", throwable);
                    }
                });
    }
}