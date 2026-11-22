package com.onurbcd.cli.dto.balance;

import com.onurbcd.cli.enums.BalanceSumType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceSumDtoTest {

    static Stream<Arguments> factories() {
        Function<BigDecimal, BalanceSumDto> income = BalanceSumDto::income;
        Function<BigDecimal, BalanceSumDto> outcome = BalanceSumDto::outcome;
        Function<BigDecimal, BalanceSumDto> diff = BalanceSumDto::diff;

        return Stream.of(
                Arguments.of("income", income, BalanceSumType.INCOME),
                Arguments.of("outcome", outcome, BalanceSumType.OUTCOME),
                Arguments.of("diff", diff, BalanceSumType.DIFF));
    }

    @Nested
    @DisplayName("factories")
    class Factories {

        @ParameterizedTest(name = "{0} keeps the given value")
        @MethodSource("com.onurbcd.cli.dto.balance.BalanceSumDtoTest#factories")
        void givenValue_shouldKeepTypeAndValue(String name, Function<BigDecimal, BalanceSumDto> factory,
                                               BalanceSumType type) {
            var result = factory.apply(new BigDecimal("12.34"));

            assertThat(result.getType()).isEqualTo(type);
            assertThat(result.getValue()).isEqualByComparingTo("12.34");
        }

        @ParameterizedTest(name = "{0} turns null into zero")
        @MethodSource("com.onurbcd.cli.dto.balance.BalanceSumDtoTest#factories")
        void givenNull_shouldUseZero(String name, Function<BigDecimal, BalanceSumDto> factory, BalanceSumType type) {
            var result = factory.apply(null);

            assertThat(result.getType()).isEqualTo(type);
            assertThat(result.getValue()).isSameAs(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var dto = new BalanceSumDto();
        assertThat(dto.getType()).isNull();
        assertThat(dto.getValue()).isNull();

        dto.setType(BalanceSumType.DIFF);
        dto.setValue(BigDecimal.TEN);

        assertThat(dto.getType()).isEqualTo(BalanceSumType.DIFF);
        assertThat(dto.getValue()).isEqualTo(BigDecimal.TEN);
    }
}
