package com.onurbcd.cli.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalFormatterTest {

    private BigDecimalFormatter formatter;

    @BeforeEach
    void setUp() {
        var numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        formatter = new BigDecimalFormatter(numberFormat);
    }

    @Nested
    @DisplayName("format")
    class Format {

        @ParameterizedTest(name = "[{index}] {0} -> {1}")
        @MethodSource("com.onurbcd.cli.formatter.BigDecimalFormatterTest#bigDecimals")
        @DisplayName("when value is a BigDecimal, should delegate to the number format")
        void givenBigDecimal_whenFormat_shouldReturnFormattedValue(BigDecimal value, String expected) {
            var result = formatter.format(value);

            assertThat(result).containsExactly(expected);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.formatter.BigDecimalFormatterTest#otherValues")
        @DisplayName("when value is not a BigDecimal, should return a single empty string")
        void givenNonBigDecimal_whenFormat_shouldReturnEmpty(Object value) {
            var result = formatter.format(value);

            assertThat(result).containsExactly("");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("when value is null, should return a single empty string")
        void givenNull_whenFormat_shouldReturnEmpty(Object value) {
            var result = formatter.format(value);

            assertThat(result).containsExactly("");
        }

        @Test
        @DisplayName("should return a new array on every call")
        void givenTwoCalls_whenFormat_shouldNotShareArrays() {
            var first = formatter.format(BigDecimal.ONE);
            var second = formatter.format(BigDecimal.ONE);

            assertThat(first).isNotSameAs(second).containsExactly("1.00");
        }
    }

    static Stream<Arguments> bigDecimals() {
        return Stream.of(
                Arguments.of(new BigDecimal("1234.5"), "1,234.50"),
                Arguments.of(BigDecimal.ZERO, "0.00"),
                Arguments.of(new BigDecimal("-10.129"), "-10.13"));
    }

    static Stream<Object> otherValues() {
        return Stream.of("1234.5", 12, 1.5d, new Object());
    }
}
