package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationTest {

    @Test
    @DisplayName("declares exactly these constants, in this order")
    void givenValues_whenListed_shouldContainTheDeclaredConstants() {
        assertThat(Operation.values()).containsExactly(
                Operation.INSERT,
                Operation.UPDATE,
                Operation.DELETE);
    }

    @Test
    @DisplayName("valueOf resolves every constant by name")
    void givenConstantName_whenValueOf_shouldReturnSameConstant() {
        for (var constant : Operation.values()) {
            assertThat(Operation.valueOf(constant.name())).isSameAs(constant);
        }
    }
}
