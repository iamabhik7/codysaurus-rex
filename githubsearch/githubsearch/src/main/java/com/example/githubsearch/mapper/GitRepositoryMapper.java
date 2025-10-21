package com.example.githubsearch.mapper;

import com.example.githubsearch.dto.GitRepositoryPaginatedResponseDto;
import com.example.githubsearch.dto.RepositoryItemDto;
import com.example.githubsearch.dto.SearchRequestDto;
import com.example.githubsearch.model.GitRepositoryItems;
import com.example.githubsearch.model.GitRepositoryPaginatedResponse;
import com.example.githubsearch.model.SearchRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for converting between internal models and DTOs
 * for GitHub repository search.
 */
@Mapper(componentModel = "spring")
public interface GitRepositoryMapper {

    GitRepositoryMapper INSTANCE = Mappers.getMapper(GitRepositoryMapper.class);

    /**
     * Maps a SearchRequestDTO to the internal SearchRequest model.
     *
     * @param dto the API request DTO
     * @return the internal model
     */
    SearchRequest toInternal(SearchRequestDto dto);

    /**
     * Maps a single internal model to a DTO.
     *
     * @param item the internal model
     * @return the DTO
     */
    RepositoryItemDto toDto(GitRepositoryItems item);

    /**
     * Maps a list of internal models to a list of DTOs.
     *
     * @param items the list of internal models
     * @return the list of DTOs
     */
    List<RepositoryItemDto> toDtoList(List<GitRepositoryItems> items);

    /**
     * Maps a paginated internal model to a paginated DTO.
     *
     * @param response the internal paginated response
     * @return the DTO paginated response
     */
    @Mappings({
            @Mapping(source = "pageNumber", target = "currentPageNumber"),
            @Mapping(target = "nextPageNumber", expression = "java(response.isHasNextPage() && response.getPageNumber() != null ? response.getPageNumber() + 1 : null)"),
            @Mapping(source = "items", target = "items")
    })
    GitRepositoryPaginatedResponseDto toDto(GitRepositoryPaginatedResponse response);
}