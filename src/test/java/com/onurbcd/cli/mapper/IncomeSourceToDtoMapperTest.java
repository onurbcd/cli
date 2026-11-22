package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceToDtoMapperTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 1, 2, 3, 4);
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2024, 5, 6, 7, 8);

    private final IncomeSourceToDtoMapperImpl mapper = new IncomeSourceToDtoMapperImpl();

    private static IncomeSource createEntity() {
        return IncomeSource.builder()
                .id(ID)
                .name("income")
                .active(true)
                .createdDate(CREATED)
                .lastModifiedDate(MODIFIED)
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when entity is complete, should map every field including the audit dates")
        void givenFullEntity_whenApply_shouldMapEveryField() {
            var entity = createEntity();

            var result = mapper.apply(entity);

            var expected = new IncomeSourceDto();
            expected.setId(ID);
            expected.setName("income");
            expected.setActive(true);
            expected.setCreatedDate(CREATED);
            expected.setLastModifiedDate(MODIFIED);
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(entity).usingRecursiveComparison().isEqualTo(createEntity());
        }

        @Test
        @DisplayName("when entity is null, should return an empty dto")
        void givenNullEntity_whenApply_shouldReturnEmptyDto() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(new IncomeSourceDto());
        }
    }
}
