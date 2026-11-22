package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceToEntityMapperTest {

    private final IncomeSourceToEntityMapperImpl mapper = new IncomeSourceToEntityMapperImpl();

    private static IncomeSourceSaveDto createSaveDto() {
        return IncomeSourceSaveDto.builder().name("income").active(true).build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the name and the active flag")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = IncomeSource.builder().name("income").active(true).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(IncomeSource.builder().build());
        }
    }
}
