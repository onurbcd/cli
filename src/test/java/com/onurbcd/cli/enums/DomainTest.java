package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainTest {

    @Test
    @DisplayName("declares exactly these constants, in this order")
    void givenValues_whenListed_shouldContainTheDeclaredConstants() {
        assertThat(Domain.values()).containsExactly(
                Domain.CATEGORY,
                Domain.INCOME_SOURCE,
                Domain.SECRET,
                Domain.BUDGET_SEQUENCE,
                Domain.BALANCE_SEQUENCE,
                Domain.BUDGET_SEQUENCE_VALIDATION,
                Domain.BALANCE_SEQUENCE_VALIDATION);
    }

    @Test
    @DisplayName("valueOf resolves every constant by name")
    void givenConstantName_whenValueOf_shouldReturnSameConstant() {
        for (var constant : Domain.values()) {
            assertThat(Domain.valueOf(constant.name())).isSameAs(constant);
        }
    }
}
