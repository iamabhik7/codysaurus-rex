package com.example.githubsearch.mapper;

import com.example.githubsearch.dto.GitRepositoryPaginatedResponseDto;
import com.example.githubsearch.dto.RepositoryItemDto;
import com.example.githubsearch.dto.SearchRequestDto;
import com.example.githubsearch.model.GitRepositoryItems;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-21T00:12:49+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class GitRepositoryMapperImpl implements GitRepositoryMapper {

    @Override
    public SearchRequest toInternal(SearchRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        SearchRequest.SearchRequestBuilder searchRequest = SearchRequest.builder();

        searchRequest.language( dto.getLanguage() );
        searchRequest.earliestCreatedDate( dto.getEarliestCreatedDate() );
        searchRequest.pageNumber( dto.getPageNumber() );

        return searchRequest.build();
    }

    @Override
    public RepositoryItemDto toDto(GitRepositoryItems item) {
        if ( item == null ) {
            return null;
        }

        RepositoryItemDto.RepositoryItemDtoBuilder repositoryItemDto = RepositoryItemDto.builder();

        repositoryItemDto.id( item.getId() );
        repositoryItemDto.name( item.getName() );
        repositoryItemDto.description( item.getDescription() );
        repositoryItemDto.language( item.getLanguage() );
        repositoryItemDto.stargazerCount( item.getStargazerCount() );
        repositoryItemDto.forksCount( item.getForksCount() );
        repositoryItemDto.htmlUrl( item.getHtmlUrl() );
        repositoryItemDto.updatedAt( item.getUpdatedAt() );
        repositoryItemDto.createdAt( item.getCreatedAt() );
        repositoryItemDto.popularityScore( item.getPopularityScore() );

        return repositoryItemDto.build();
    }

    @Override
    public List<RepositoryItemDto> toDtoList(List<GitRepositoryItems> items) {
        if ( items == null ) {
            return null;
        }

        List<RepositoryItemDto> list = new ArrayList<RepositoryItemDto>( items.size() );
        for ( GitRepositoryItems gitRepositoryItems : items ) {
            list.add( toDto( gitRepositoryItems ) );
        }

        return list;
    }

    @Override
    public GitRepositoryPaginatedResponseDto toDto(GitRepositoryPaginatedResponse response) {
        if ( response == null ) {
            return null;
        }

        GitRepositoryPaginatedResponseDto.GitRepositoryPaginatedResponseDtoBuilder gitRepositoryPaginatedResponseDto = GitRepositoryPaginatedResponseDto.builder();

        gitRepositoryPaginatedResponseDto.currentPageNumber( response.getPageNumber() );
        gitRepositoryPaginatedResponseDto.items( toDtoList( response.getItems() ) );
        gitRepositoryPaginatedResponseDto.totalCount( response.getTotalCount() );
        gitRepositoryPaginatedResponseDto.incompleteResults( response.isIncompleteResults() );
        gitRepositoryPaginatedResponseDto.hasNextPage( response.isHasNextPage() );

        gitRepositoryPaginatedResponseDto.nextPageNumber( response.isHasNextPage() && response.getPageNumber() != null ? response.getPageNumber() + 1 : null );

        return gitRepositoryPaginatedResponseDto.build();
    }
}
