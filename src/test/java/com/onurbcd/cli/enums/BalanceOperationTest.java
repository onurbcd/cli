package com.onurbcd.cli.enums;

import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceOperationTest {

    private static final BigDecimal TEN = BigDecimal.TEN;
    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    static Stream<Arguments> constants() {
        return Stream.of(
                Arguments.of(BalanceOperation.INSERT_INCOME, Operation.INSERT, BalanceType.INCOME, BigDecimal.valueOf(13)),
                Arguments.of(BalanceOperation.INSERT_OUTCOME, Operation.INSERT, BalanceType.OUTCOME, BigDecimal.valueOf(7)),
                Arguments.of(BalanceOperation.DELETE_INCOME, Operation.DELETE, BalanceType.INCOME, BigDecimal.valueOf(7)),
                Arguments.of(BalanceOperation.DELETE_OUTCOME, Operation.DELETE, BalanceType.OUTCOME, BigDecimal.valueOf(13)),
                Arguments.of(BalanceOperation.UPDATE_INCOME, Operation.UPDATE, BalanceType.INCOME, BigDecimal.valueOf(13)),
                Arguments.of(BalanceOperation.UPDATE_OUTCOME, Operation.UPDATE, BalanceType.OUTCOME, BigDecimal.valueOf(7)));
    }

    @Test
    @DisplayName("declares exactly the six operation/type combinations, in order")
    void givenValues_whenListed_shouldContainTheSixConstants() {
        assertThat(BalanceOperation.values()).containsExactly(
                BalanceOperation.INSERT_INCOME,
                BalanceOperation.INSERT_OUTCOME,
                BalanceOperation.DELETE_INCOME,
                BalanceOperation.DELETE_OUTCOME,
                BalanceOperation.UPDATE_INCOME,
                BalanceOperation.UPDATE_OUTCOME);
    }

    @Nested
    @DisplayName("getters and func")
    class Accessors {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.enums.BalanceOperationTest#constants")
        void givenConstant_whenRead_shouldExposeOperationTypeAndFunction(
                BalanceOperation constant, Operation operation, BalanceType type, BigDecimal expected) {
            var result = constant.getFunc().apply(TEN, THREE);

            assertThat(constant.getOperation()).isEqualTo(operation);
            assertThat(constant.getBalanceType()).isEqualTo(type);
            assertThat(result).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("from")
    class From {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.enums.BalanceOperationTest#constants")
        void givenOperationAndType_whenFrom_shouldReturnMatchingConstant(
                BalanceOperation expected, Operation operation, BalanceType type, BigDecimal ignored) {
            var result = BalanceOperation.from(operation, type);

            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("when operation is null, throws the business error instead of a NullPointerException")
        void givenNullOperation_whenFrom_shouldThrowMissingOperation() {
            assertThatThrownBy(() -> BalanceOperation.from(null, BalanceType.INCOME))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.BALANCE_OPERATION_MISSING);
                        assertThat(e.getMessage())
                                .isEqualTo("There is no balance operation with operation null and balance type INCOME");
                    });
        }

        @Test
        @DisplayName("when balance type is null, throws the business error instead of a NullPointerException")
        void givenNullBalanceType_whenFrom_shouldThrowMissingOperation() {
            assertThatThrownBy(() -> BalanceOperation.from(Operation.DELETE, null))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.BALANCE_OPERATION_MISSING);
                        assertThat(e.getMessage())
                                .isEqualTo("There is no balance operation with operation DELETE and balance type null");
                    });
        }
    }
}
