package com.onurbcd.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NumberUtilTest {

    private static final BigDecimal TEN = new BigDecimal("10.50");
    private static final BigDecimal FOUR = new BigDecimal("4.25");

    private static Stream<Arguments> shortPairs() {
        return Stream.of(
                Arguments.of(null, null, true),
                Arguments.of(null, (short) 1, false),
                Arguments.of((short) 1, null, false),
                Arguments.of((short) 1, (short) 1, true),
                Arguments.of((short) 1, (short) 2, false),
                Arguments.of((short) 300, (short) 300, true));
    }

    @Nested
    @DisplayName("equals / notEquals")
    class Equality {

        @ParameterizedTest
        @MethodSource("com.onurbcd.cli.util.NumberUtilTest#shortPairs")
        @DisplayName("null-safe comparison of shorts, notEquals is the negation")
        void givenPair_shouldCompareNullSafe(Short first, Short second, boolean expected) {
            assertThat(NumberUtil.equals(first, second)).isEqualTo(expected);
            assertThat(NumberUtil.notEquals(first, second)).isEqualTo(!expected);
        }
    }

    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("two nulls add up to zero")
        void givenBothNull_shouldReturnZero() {
            assertThat(NumberUtil.add(null, null)).isSameAs(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("a null first operand returns the second")
        void givenFirstNull_shouldReturnSecond() {
            assertThat(NumberUtil.add(null, TEN)).isSameAs(TEN);
        }

        @Test
        @DisplayName("a null second operand returns the first")
        void givenSecondNull_shouldReturnFirst() {
            assertThat(NumberUtil.add(TEN, null)).isSameAs(TEN);
        }

        @Test
        @DisplayName("two values are summed and the operands are not changed")
        void givenBoth_shouldSum() {
            assertThat(NumberUtil.add(TEN, FOUR)).isEqualByComparingTo("14.75");
            assertThat(TEN).isEqualTo(new BigDecimal("10.50"));
            assertThat(FOUR).isEqualTo(new BigDecimal("4.25"));
        }
    }

    @Nested
    @DisplayName("subtract")
    class Subtract {

        @Test
        @DisplayName("two nulls subtract to zero")
        void givenBothNull_shouldReturnZero() {
            assertThat(NumberUtil.subtract(null, null)).isSameAs(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("a null first operand returns the negated second")
        void givenFirstNull_shouldReturnNegatedSecond() {
            assertThat(NumberUtil.subtract(null, TEN)).isEqualTo(new BigDecimal("-10.50"));
        }

        @Test
        @DisplayName("a null second operand returns the first")
        void givenSecondNull_shouldReturnFirst() {
            assertThat(NumberUtil.subtract(TEN, null)).isSameAs(TEN);
        }

        @Test
        @DisplayName("two values are subtracted in order and the operands are not changed")
        void givenBoth_shouldSubtract() {
            assertThat(NumberUtil.subtract(TEN, FOUR)).isEqualByComparingTo("6.25");
            assertThat(NumberUtil.subtract(FOUR, TEN)).isEqualByComparingTo("-6.25");
            assertThat(TEN).isEqualTo(new BigDecimal("10.50"));
            assertThat(FOUR).isEqualTo(new BigDecimal("4.25"));
        }
    }

    @Nested
    @DisplayName("parseShort")
    class ParseShort {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "abc", "1.5", "32768", "-32769", "1 2"})
        @DisplayName("blank or invalid text returns null")
        void givenBlankOrInvalid_shouldReturnNull(String value) {
            assertThat(NumberUtil.parseShort(value)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "7", "-7", "32767", "-32768"})
        @DisplayName("a valid short is parsed")
        void givenValid_shouldParse(String value) {
            assertThat(NumberUtil.parseShort(value)).isEqualTo(Short.parseShort(value));
        }
    }

    @Nested
    @DisplayName("toBigDecimal")
    class ToBigDecimal {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "abc", "1,5", "1.2.3"})
        @DisplayName("blank or invalid text returns null")
        void givenBlankOrInvalid_shouldReturnNull(String value) {
            assertThat(NumberUtil.toBigDecimal(value)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "12.50", "-3.1415", "1E+3"})
        @DisplayName("a valid number is parsed keeping its scale")
        void givenValid_shouldParse(String value) {
            assertThat(NumberUtil.toBigDecimal(value)).isEqualTo(new BigDecimal(value));
        }
    }
}
