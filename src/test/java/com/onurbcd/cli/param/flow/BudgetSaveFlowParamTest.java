package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.TestFactory.createBudgetDto;
import static com.onurbcd.cli.TestFactory.createSelectItems;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetSaveFlowParamTest {

    private static final List<SelectItem> BILL_TYPES = createSelectItems("billType");

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when budget is complete, should convert its fields and keep the bill types")
        void givenFullBudget_whenOf_shouldConvertEveryField() {
            var budget = createBudgetDto();
            budget.setPaid(true);

            var result = BudgetSaveFlowParam.of(budget, SaveFlowParam.budget(BILL_TYPES));

            var expected = BudgetSaveFlowParam.builder()
                    .name("bogus")
                    .refYear("2024")
                    .refMonth("3")
                    .billType("bill type")
                    .amount("100.50")
                    .paid(true)
                    .billTypeItems(BILL_TYPES)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when budget is null, should leave the fields null, paid false and keep the bill types")
        void givenNullBudget_whenOf_shouldDefaultPaidToFalse() {
            var result = BudgetSaveFlowParam.of(null, SaveFlowParam.budget(BILL_TYPES));

            var expected = BudgetSaveFlowParam.builder().paid(false).billTypeItems(BILL_TYPES).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when budget paid is null, should default paid to false")
        void givenBudgetWithoutPaid_whenOf_shouldDefaultPaidToFalse() {
            var result = BudgetSaveFlowParam.of(new BudgetDto(), SaveFlowParam.budget(BILL_TYPES));

            assertThat(result.getPaid()).isFalse();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BUDGET")
        void whenGetType_shouldBeBudget() {
            assertThat(BudgetSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BUDGET);
        }
    }
}
