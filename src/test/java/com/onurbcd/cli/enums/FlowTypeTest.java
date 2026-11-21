package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order")
    void givenValues_whenListed_shouldContainTheDeclaredConstants() {
        assertThat(FlowType.values()).containsExactly(
                FlowType.BALANCE,
                FlowType.BILL_TYPE,
                FlowType.BUDGET,
                FlowType.CATEGORY,
                FlowType.SOURCE,
                FlowType.INCOME_SOURCE,
                FlowType.SECRET,
                FlowType.BILL_OPEN,
                FlowType.BILL_CLOSE,
                FlowType.BILL);
    }

    @Test
    @DisplayName("valueOf resolves every constant by name")
    void givenConstantName_whenValueOf_shouldReturnSameConstant() {
        for (var constant : FlowType.values()) {
            assertThat(FlowType.valueOf(constant.name())).isSameAs(constant);
        }
    }
}
