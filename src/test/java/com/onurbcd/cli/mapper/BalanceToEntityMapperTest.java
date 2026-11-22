package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Category;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceToEntityMapperTest {

    private static final LocalDate DAY = LocalDate.of(2024, 3, 15);

    private final BalanceToEntityMapperImpl mapper = new BalanceToEntityMapperImpl();

    private static BalanceSaveDto createSaveDto() {
        return BalanceSaveDto.builder()
                .name("balance")
                .active(true)
                .sequence((short) 3)
                .dayCalendarDate(DAY)
                .sourceId(SOURCE_ID)
                .categoryId(CATEGORY_ID)
                .amount(new BigDecimal("12.50"))
                .code("CODE")
                .description("description")
                .balanceType(BalanceType.INCOME)
                .paymentType(PaymentType.CASH)
                .documentsIds(Set.of(DOCUMENT_ID))
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the fields and nested ids and ignore the documents")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var day = new Day();
            day.setCalendarDate(DAY);
            var expected = Balance.builder()
                    .name("balance")
                    .active(true)
                    .sequence((short) 3)
                    .day(day)
                    .source(Source.builder().id(SOURCE_ID).build())
                    .category(Category.builder().id(CATEGORY_ID).build())
                    .amount(new BigDecimal("12.50"))
                    .code("CODE")
                    .description("description")
                    .balanceType(BalanceType.INCOME)
                    .paymentType(PaymentType.CASH)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(result.getId()).isNull();
            assertThat(result.getDocuments()).isEmpty();
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Balance.builder().build());
        }
    }

    @Nested
    @DisplayName("nested mappers")
    class NestedMappers {

        @Test
        void givenNullDto_whenMapDay_shouldReturnEmptyDay() {
            var result = mapper.balanceSaveDtoToDay(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(new Day());
        }

        @Test
        void givenNullDto_whenMapSource_shouldReturnEmptySource() {
            var result = mapper.balanceSaveDtoToSource(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Source.builder().build());
        }

        @Test
        void givenNullDto_whenMapCategory_shouldReturnEmptyCategory() {
            var result = mapper.balanceSaveDtoToCategory(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Category.builder().build());
        }
    }
}
