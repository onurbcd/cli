package com.onurbcd.cli.util;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.param.YearAndMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilTest {

    private static final Short Y = 2024;
    private static final Short M = 3;
    private static final Short D = 15;

    private static Stream<Arguments> monthPairs() {
        return Stream.of(
                Arguments.of(Y, M, Y, M, true),
                Arguments.of(Y, M, (short) 2025, M, false),
                Arguments.of(Y, M, Y, (short) 4, false),
                Arguments.of(null, M, Y, M, false),
                Arguments.of(Y, null, Y, M, false),
                Arguments.of(Y, M, null, M, false),
                Arguments.of(Y, M, Y, null, false));
    }

    private static Stream<Arguments> dayPairs() {
        return Stream.of(
                Arguments.of(Y, M, D, Y, M, D, true),
                Arguments.of(Y, M, D, (short) 2025, M, D, false),
                Arguments.of(Y, M, D, Y, (short) 4, D, false),
                Arguments.of(Y, M, D, Y, M, (short) 16, false),
                Arguments.of(null, M, D, Y, M, D, false),
                Arguments.of(Y, null, D, Y, M, D, false),
                Arguments.of(Y, M, null, Y, M, D, false),
                Arguments.of(Y, M, D, null, M, D, false),
                Arguments.of(Y, M, D, Y, null, D, false),
                Arguments.of(Y, M, D, Y, M, null, false));
    }

    @ParameterizedTest
    @MethodSource("monthPairs")
    @DisplayName("equalMonth requires all four values and equal year and month")
    void givenValues_whenEqualMonth_shouldCompare(Short y, Short m, Short oy, Short om, boolean expected) {
        assertThat(DateUtil.equalMonth(y, m, oy, om)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("dayPairs")
    @DisplayName("equalDay requires all six values and equal year, month and day")
    void givenValues_whenEqualDay_shouldCompare(Short y, Short m, Short d, Short oy, Short om, Short od,
                                               boolean expected) {
        assertThat(DateUtil.equalDay(y, m, d, oy, om, od)).isEqualTo(expected);
    }

    @Nested
    @DisplayName("parseLocalDate")
    class ParseLocalDate {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("blank text returns null")
        void givenBlank_shouldReturnNull(String text) {
            assertThat(DateUtil.parseLocalDate(text)).isNull();
        }

        @Test
        @DisplayName("an ISO date is parsed")
        void givenIso_shouldParse() {
            assertThat(DateUtil.parseLocalDate("2024-02-29")).isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @ParameterizedTest
        @ValueSource(strings = {"15/03/2024", "2024-13-01", "2023-02-29", "abc", "2024-3-5"})
        @DisplayName("an invalid date fails with PARSE_LOCAL_DATE")
        void givenInvalid_shouldThrow(String text) {
            assertThatThrownBy(() -> DateUtil.parseLocalDate(text))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.PARSE_LOCAL_DATE);
                        assertThat(e.getCause()).isInstanceOf(DateTimeException.class);
                    });
        }
    }

    @Nested
    @DisplayName("formatDate")
    class FormatDate {

        @Test
        @DisplayName("null returns null")
        void givenNull_shouldReturnNull() {
            assertThat(DateUtil.formatDate(null)).isNull();
        }

        @Test
        @DisplayName("a date is formatted as ISO")
        void givenDate_shouldFormatIso() {
            assertThat(DateUtil.formatDate(LocalDate.of(2024, 3, 5))).isEqualTo("2024-03-05");
        }
    }

    @Nested
    @DisplayName("orCurrentYearAndMonth")
    class OrCurrentYearAndMonth {

        @Test
        @DisplayName("given both values, keeps them without reading the clock")
        void givenBoth_shouldKeepThem() {
            var result = DateUtil.orCurrentYearAndMonth(Y, M, null);

            assertThat(result).isEqualTo(new YearAndMonth(Y, M));
        }

        @Test
        @DisplayName("without year, fills in the current year and keeps the month")
        void givenNoYear_shouldUseCurrentYear() {
            var result = DateUtil.orCurrentYearAndMonth(null, M, TestClock.DEFAULT);

            assertThat(result).isEqualTo(new YearAndMonth((short) 2025, M));
        }

        @Test
        @DisplayName("without month, fills in the current month and keeps the year")
        void givenNoMonth_shouldUseCurrentMonth() {
            var result = DateUtil.orCurrentYearAndMonth(Y, null, TestClock.DEFAULT);

            assertThat(result).isEqualTo(new YearAndMonth(Y, (short) 7));
        }

        @Test
        @DisplayName("without either, fills in both from the same date")
        void givenNeither_shouldUseCurrentBoth() {
            var result = DateUtil.orCurrentYearAndMonth(null, null, TestClock.fixedAt(LocalDate.of(2031, 12, 31)));

            assertThat(result).isEqualTo(new YearAndMonth((short) 2031, (short) 12));
        }
    }
}
