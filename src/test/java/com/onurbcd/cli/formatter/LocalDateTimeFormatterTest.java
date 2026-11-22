package com.onurbcd.cli.formatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeFormatterTest {

    private LocalDateTimeFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new LocalDateTimeFormatter(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Nested
    @DisplayName("format")
    class Format {

        @Test
        @DisplayName("when value is a LocalDateTime, should format it with the configured pattern")
        void givenLocalDateTime_whenFormat_shouldReturnFormattedValue() {
            var value = LocalDateTime.of(2026, 11, 21, 9, 5, 3);

            var result = formatter.format(value);

            assertThat(result).containsExactly("21/11/2026 09:05:03");
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.formatter.LocalDateTimeFormatterTest#otherValues")
        @DisplayName("when value is not a LocalDateTime, should return a single empty string")
        void givenNonLocalDateTime_whenFormat_shouldReturnEmpty(Object value) {
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
    }

    static Stream<Object> otherValues() {
        return Stream.of("2026-11-21T09:05:03", LocalDate.of(2026, 11, 21), 1L);
    }
}
