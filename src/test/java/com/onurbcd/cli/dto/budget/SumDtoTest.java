package com.onurbcd.cli.dto.budget;

import com.onurbcd.cli.enums.SumType;
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

class SumDtoTest {

    static Stream<Arguments> factories() {
        Function<BigDecimal, SumDto> total = SumDto::total;
        Function<BigDecimal, SumDto> paid = SumDto::paid;
        Function<BigDecimal, SumDto> unpaid = SumDto::unpaid;
        Function<BigDecimal, SumDto> balance = SumDto::balance;

        return Stream.of(
                Arguments.of("total", total, SumType.TOTAL),
                Arguments.of("paid", paid, SumType.PAID),
                Arguments.of("unpaid", unpaid, SumType.UNPAID),
                Arguments.of("balance", balance, SumType.BALANCE));
    }

    @Nested
    @DisplayName("factories")
    class Factories {

        @ParameterizedTest(name = "{0} keeps the given value")
        @MethodSource("com.onurbcd.cli.dto.budget.SumDtoTest#factories")
        void givenValue_shouldKeepTypeAndValue(String name, Function<BigDecimal, SumDto> factory, SumType type) {
            var result = factory.apply(new BigDecimal("12.34"));

            assertThat(result.getType()).isEqualTo(type);
            assertThat(result.getValue()).isEqualByComparingTo("12.34");
        }

        @ParameterizedTest(name = "{0} turns null into zero")
        @MethodSource("com.onurbcd.cli.dto.budget.SumDtoTest#factories")
        void givenNull_shouldUseZero(String name, Function<BigDecimal, SumDto> factory, SumType type) {
            var result = factory.apply(null);

            assertThat(result.getType()).isEqualTo(type);
            assertThat(result.getValue()).isSameAs(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var dto = new SumDto();
        assertThat(dto.getType()).isNull();
        assertThat(dto.getValue()).isNull();

        dto.setType(SumType.PAID);
        dto.setValue(BigDecimal.TEN);

        assertThat(dto.getType()).isEqualTo(SumType.PAID);
        assertThat(dto.getValue()).isEqualTo(BigDecimal.TEN);
    }
}
