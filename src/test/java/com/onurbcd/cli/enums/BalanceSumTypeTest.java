package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceSumTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order")
    void givenValues_whenListed_shouldContainTheDeclaredConstants() {
        assertThat(BalanceSumType.values()).containsExactly(
                BalanceSumType.INCOME,
                BalanceSumType.OUTCOME,
                BalanceSumType.DIFF);
    }

    @Test
    @DisplayName("valueOf resolves every constant by name")
    void givenConstantName_whenValueOf_shouldReturnSameConstant() {
        for (var constant : BalanceSumType.values()) {
            assertThat(BalanceSumType.valueOf(constant.name())).isSameAs(constant);
        }
    }
}
