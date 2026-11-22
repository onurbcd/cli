package com.onurbcd.cli.dto.budget;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestFactory.createBudgetDto;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BudgetSaveDto validDto() {
        return BudgetSaveDto.builder()
                .name("Water")
                .active(true)
                .sequence((short) 1)
                .refYear((short) 2024)
                .refMonth((short) 3)
                .billTypeId(BILL_TYPE_ID)
                .amount(new BigDecimal("10.00"))
                .paid(false)
                .build();
    }

    private static java.util.Set<String> messages(BudgetSaveDto dto) {
        return TestValidator.messages(validator, dto);
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without a budget, should map the context and default to active")
        void givenNoBudget_shouldMapContext() {
            var context = ComponentContext.empty();
            context.put("name", "  Water   Bill ");
            context.put("refYear", "2024");
            context.put("refMonth", "3");
            context.put("billTypeId", BILL_TYPE_ID.toString());
            context.put("amount", "12.50");
            context.put("paid", true);

            var result = BudgetSaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    BudgetSaveDto.builder()
                            .name("Water Bill")
                            .active(true)
                            .refYear((short) 2024)
                            .refMonth((short) 3)
                            .billTypeId(BILL_TYPE_ID)
                            .amount(new BigDecimal("12.50"))
                            .paid(true)
                            .build());
        }

        @Test
        @DisplayName("with a budget and an empty context, should keep its active flag and sequence")
        void givenBudgetAndEmptyContext_shouldKeepBudgetValues() {
            var result = BudgetSaveDto.of(ComponentContext.empty(), createBudgetDto());

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    BudgetSaveDto.builder().active(false).sequence((short) 4).build());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a valid dto has no violations")
        void givenValid_shouldHaveNoViolations() {
            assertThat(messages(validDto())).isEmpty();
        }

        @Test
        @DisplayName("sequence may be null, 1 is accepted and 0 is rejected")
        void givenSequence_shouldCheckMin() {
            var dto = validDto();
            dto.setSequence(null);
            assertThat(messages(dto)).isEmpty();

            dto.setSequence((short) 0);
            assertThat(messages(dto)).containsExactly("Sequence must be greater than or equal to 1");
        }

        @Test
        @DisplayName("each mandatory field is reported when null")
        void givenNulls_shouldReportEachMandatory() {
            var dto = validDto();
            dto.setRefYear(null);
            dto.setRefMonth(null);
            dto.setBillTypeId(null);
            dto.setAmount(null);
            dto.setPaid(null);

            assertThat(messages(dto)).containsExactlyInAnyOrder(
                    "Reference year is mandatory",
                    "Reference month is mandatory",
                    "Bill type is mandatory",
                    "Amount is mandatory",
                    "Paid status is mandatory");
        }

        @ParameterizedTest
        @ValueSource(shorts = {2000, 2025})
        @DisplayName("reference year on the limits is accepted")
        void givenYearOnLimit_shouldBeAccepted(short year) {
            var dto = validDto();
            dto.setRefYear(year);

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("reference year below the minimum is rejected")
        void givenYearBelowMin_shouldBeRejected() {
            var dto = validDto();
            dto.setRefYear((short) 1999);

            assertThat(messages(dto)).containsExactly("The year cannot be less than 2000");
        }

        @Test
        @DisplayName("reference year above the current year is rejected")
        void givenYearAboveMax_shouldBeRejected() {
            var dto = validDto();
            dto.setRefYear((short) 2026);

            assertThat(messages(dto)).containsExactly("The year cannot be greater than 2025");
        }

        @ParameterizedTest
        @ValueSource(shorts = {1, 12})
        @DisplayName("reference month on the limits is accepted")
        void givenMonthOnLimit_shouldBeAccepted(short month) {
            var dto = validDto();
            dto.setRefMonth(month);

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("reference month below 1 is rejected")
        void givenMonthBelowMin_shouldBeRejected() {
            var dto = validDto();
            dto.setRefMonth((short) 0);

            assertThat(messages(dto)).containsExactly("Reference month must be greater than or equal to 1");
        }

        @Test
        @DisplayName("reference month above 12 is rejected")
        void givenMonthAboveMax_shouldBeRejected() {
            var dto = validDto();
            dto.setRefMonth((short) 13);

            assertThat(messages(dto)).containsExactly("Reference month must be less than or equal to 12");
        }

        @Test
        @DisplayName("amount on the limits is accepted")
        void givenAmountOnLimit_shouldBeAccepted() {
            var dto = validDto();
            dto.setAmount(new BigDecimal("0.0001"));
            assertThat(messages(dto)).isEmpty();

            dto.setAmount(new BigDecimal("999999999999999"));
            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("amount below the minimum is rejected")
        void givenAmountBelowMin_shouldBeRejected() {
            var dto = validDto();
            dto.setAmount(new BigDecimal("0.0000"));

            assertThat(messages(dto)).containsExactly("Amount must be greater than or equal to 0.0001");
        }

        @Test
        @DisplayName("amount above the maximum is rejected")
        void givenAmountAboveMax_shouldBeRejected() {
            var dto = validDto();
            dto.setAmount(new BigDecimal("1000000000000000"));

            assertThat(messages(dto)).containsExactly("Amount must be less than or equal to 999999999999999");
        }
    }
}
