package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.persistency.entity.Secret;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretToEntityMapperTest {

    private final SecretToEntityMapperImpl mapper = new SecretToEntityMapperImpl();

    private static SecretSaveDto createSaveDto() {
        return SecretSaveDto.builder()
                .name("secret")
                .active(true)
                .description("description")
                .link("https://example.com")
                .username("user")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map every field")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = Secret.builder()
                    .name("secret")
                    .active(true)
                    .description("description")
                    .link("https://example.com")
                    .username("user")
                    .password("password")
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Secret.builder().build());
        }
    }
}
