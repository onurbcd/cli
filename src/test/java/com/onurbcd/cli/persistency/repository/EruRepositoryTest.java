package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.TestConstants;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.persistency.entity.Entityable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EruRepositoryTest {

    @SuppressWarnings("rawtypes")
    private final EruRepository<Entityable, Dtoable> repository = Mockito.mock(EruRepository.class, Mockito.CALLS_REAL_METHODS);

    @Nested
    @DisplayName("deleteUsingId")
    class DeleteUsingId {

        @Test
        @DisplayName("default implementation deletes nothing and returns zero")
        void givenId_whenDeleteUsingId_shouldReturnZero() {
            assertThat(repository.deleteUsingId(TestConstants.ID)).isZero();
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("default implementation returns an empty optional")
        void givenId_whenGet_shouldReturnEmpty() {
            Optional<Entityable> result = repository.get(TestConstants.ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = "11111111-1111-1111-1111-111111111111")
        @DisplayName("default implementation returns an empty list, with or without an excluded id")
        void givenNullableId_whenGetItems_shouldReturnEmptyList(UUID id) {
            List<ItemDto> result = repository.getItems(id);

            assertThat(result).isEmpty();
        }
    }
}
