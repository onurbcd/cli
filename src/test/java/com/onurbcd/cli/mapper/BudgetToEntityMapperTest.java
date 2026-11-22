package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.Budget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetToEntityMapperTest {

    private static final Short REF_YEAR = 2025;
    private static final Short REF_MONTH = 7;
    private static final LocalDateTime AUDIT_DATE = LocalDateTime.of(2024, 1, 2, 3, 4);

    private final BudgetToEntityMapperImpl mapper = new BudgetToEntityMapperImpl();

    private static BudgetSaveDto createSaveDto() {
        return BudgetSaveDto.builder()
                .name("budget")
                .active(true)
                .sequence((short) 4)
                .refYear((short) 2024)
                .refMonth((short) 3)
                .billTypeId(BILL_TYPE_ID)
                .amount(new BigDecimal("100.50"))
                .paid(true)
                .build();
    }

    private static BudgetDto createBudgetDto() {
        var budget = new BudgetDto();
        budget.setId(ID);
        budget.setName("budget");
        budget.setActive(false);
        budget.setCreatedDate(AUDIT_DATE);
        budget.setLastModifiedDate(AUDIT_DATE);
        budget.setSequence((short) 4);
        budget.setRefYear((short) 2024);
        budget.setRefMonth((short) 3);
        budget.setBillTypeId(BILL_TYPE_ID);
        budget.setBillTypeName("bill type");
        budget.setAmount(new BigDecimal("100.50"));
        budget.setPaid(true);
        return budget;
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the fields and the bill type id")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = Budget.builder()
                    .name("budget")
                    .active(true)
                    .sequence((short) 4)
                    .refYear((short) 2024)
                    .refMonth((short) 3)
                    .billType(BillType.builder().id(BILL_TYPE_ID).build())
                    .amount(new BigDecimal("100.50"))
                    .paid(true)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Budget.builder().build());
        }
    }

    @Nested
    @DisplayName("copy")
    class Copy {

        @Test
        @DisplayName("when dto is complete, should copy it as a new active unpaid budget of the given period")
        void givenFullDto_whenCopy_shouldCopyAsNewUnpaidActiveBudget() {
            var dto = createBudgetDto();

            var result = mapper.copy(dto, REF_YEAR, REF_MONTH);

            var expected = Budget.builder()
                    .name("budget")
                    .active(true)
                    .sequence((short) 4)
                    .refYear(REF_YEAR)
                    .refMonth(REF_MONTH)
                    .billType(BillType.builder().id(BILL_TYPE_ID).build())
                    .amount(new BigDecimal("100.50"))
                    .paid(false)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedDate()).isNull();
            assertThat(result.getLastModifiedDate()).isNull();
            assertThat(dto).usingRecursiveComparison().isEqualTo(createBudgetDto());
        }

        @Test
        @DisplayName("when dto is null, should still set the fixed values and the period")
        void givenNullDto_whenCopy_shouldSetOnlyFixedValuesAndPeriod() {
            var result = mapper.copy(null, REF_YEAR, REF_MONTH);

            var expected = Budget.builder()
                    .active(true)
                    .refYear(REF_YEAR)
                    .refMonth(REF_MONTH)
                    .paid(false)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("nested mappers")
    class NestedMappers {

        @Test
        void givenNullSaveDto_whenMapBillType_shouldReturnEmptyBillType() {
            var result = mapper.budgetSaveDtoToBillType(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(BillType.builder().build());
        }

        @Test
        void givenNullBudgetDto_whenMapBillType_shouldReturnEmptyBillType() {
            var result = mapper.budgetDtoToBillType(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(BillType.builder().build());
        }
    }
}
