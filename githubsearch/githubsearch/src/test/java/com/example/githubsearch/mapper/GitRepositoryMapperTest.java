package com.example.githubsearch.mapper;

import com.example.githubsearch.model.GitRepositoryItems;
import com.example.githubsearch.dto.RepositoryItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GitRepositoryMapper} (MapStruct mapper).
 */
class GitRepositoryMapperTest {

    private final GitRepositoryMapper mapper = GitRepositoryMapper.INSTANCE;

    /**
     * Tests mapping from GitRepositoryItems to RepositoryItemDto.
     */
    @Test
    @DisplayName("map: GitRepositoryItems to RepositoryItemDto")
    void testMapToDto() {
        GitRepositoryItems model = GitRepositoryItems.builder()
                .id(2L)
                .name("repo2")
                .stargazerCount(99)
                .forksCount(11)
                .updatedAt(Instant.parse("2022-12-31T00:00:00Z"))
                .build();
        RepositoryItemDto dto = mapper.toDto(model);
        assertEquals(model.getId(), dto.getId());
        assertEquals(model.getName(), dto.getName());
        assertEquals(model.getStargazerCount(), dto.getStargazerCount());
        assertEquals(model.getForksCount(), dto.getForksCount());
        assertEquals(model.getUpdatedAt(), dto.getUpdatedAt());
    }

    /**
     * Tests mapping a list of GitRepositoryItems to a list of RepositoryItemDto.
     */
    @Test
    @DisplayName("map: List<GitRepositoryItems> to List<RepositoryItemDto>")
    void testMapListToDto() {
        List<GitRepositoryItems> modelList = List.of(
                GitRepositoryItems.builder().id(3L).name("repo3").build(),
                GitRepositoryItems.builder().id(4L).name("repo4").build()
        );
        List<RepositoryItemDto> dtoList = mapper.toDtoList(modelList);
        assertEquals(2, dtoList.size());
        assertEquals(modelList.get(0).getId(), dtoList.get(0).getId());
        assertEquals(modelList.get(1).getId(), dtoList.get(1).getId());
    }

    /**
     * Tests mapping null and empty lists for toDto and toDtoList.
     */
    @Test
    @DisplayName("map: null and empty lists")
    void testMapNullsAndEmpty() {
        assertNull(mapper.toDto((GitRepositoryItems) null));
        assertNull(mapper.toDtoList(null));
        assertTrue(mapper.toDtoList(List.of()).isEmpty());
    }
}
