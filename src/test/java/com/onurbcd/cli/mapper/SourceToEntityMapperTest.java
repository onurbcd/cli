package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import com.onurbcd.cli.persistency.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class SourceToEntityMapperTest {

    private final SourceToEntityMapperImpl mapper = new SourceToEntityMapperImpl();

    private static SourceSaveDto createSaveDto() {
        return SourceSaveDto.builder()
                .name("source")
                .active(true)
                .incomeSourceId(PARENT_ID)
                .sourceType(SourceType.USABLE)
                .currencyType(CurrencyType.DIGITAL)
                .balance(new BigDecimal("100.50"))
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the fields and the income source id")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = Source.builder()
                    .name("source")
                    .active(true)
                    .incomeSource(IncomeSource.builder().id(PARENT_ID).build())
                    .sourceType(SourceType.USABLE)
                    .currencyType(CurrencyType.DIGITAL)
                    .balance(new BigDecimal("100.50"))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Source.builder().build());
        }
    }

    @Test
    void givenNullDto_whenMapIncomeSource_shouldReturnEmptyIncomeSource() {
        var result = mapper.sourceSaveDtoToIncomeSource(null);

        assertThat(result).usingRecursiveComparison().isEqualTo(IncomeSource.builder().build());
    }
}
