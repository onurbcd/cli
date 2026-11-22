package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.Budget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillOpenToEntityMapperTest {

    private final BillOpenToEntityMapperImpl mapper = new BillOpenToEntityMapperImpl();

    private static BillOpenDto createOpenDto() {
        return BillOpenDto.builder()
                .name("bill")
                .active(true)
                .referenceDayCalendarDate(LocalDate.of(2024, 3, 1))
                .documentDateCalendarDate(LocalDate.of(2024, 3, 2))
                .dueDateCalendarDate(LocalDate.of(2024, 3, 10))
                .observation("observation")
                .installment((short) 2)
                .documentType(DocumentType.BILL)
                .budgetId(PARENT_ID)
                .referenceType(ReferenceType.MONTH)
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the mapped fields and ignore the dates")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createOpenDto();

            var result = mapper.apply(dto);

            var expected = Bill.builder()
                    .name("bill")
                    .active(true)
                    .observation("observation")
                    .installment((short) 2)
                    .documentType(DocumentType.BILL)
                    .referenceType(ReferenceType.MONTH)
                    .budget(Budget.builder().id(PARENT_ID).build())
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createOpenDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Bill.builder().build());
        }
    }

    @Test
    void givenNullDto_whenMapBudget_shouldReturnEmptyBudget() {
        var result = mapper.billOpenDtoToBudget(null);

        assertThat(result).usingRecursiveComparison().isEqualTo(Budget.builder().build());
    }
}
