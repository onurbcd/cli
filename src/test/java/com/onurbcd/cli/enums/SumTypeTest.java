package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SumTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order")
    void givenValues_whenListed_shouldContainTheDeclaredConstants() {
        assertThat(SumType.values()).containsExactly(
                SumType.TOTAL,
                SumType.PAID,
                SumType.UNPAID,
                SumType.BALANCE);
    }

    @Test
    @DisplayName("valueOf resolves every constant by name")
    void givenConstantName_whenValueOf_shouldReturnSameConstant() {
        for (var constant : SumType.values()) {
            assertThat(SumType.valueOf(constant.name())).isSameAs(constant);
        }
    }
}
