package com.onurbcd.cli.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EruConfigTest {

    private EruConfig config;

    @BeforeEach
    void setUp() {
        config = new EruConfig();
    }

    @Nested
    @DisplayName("eruMapper")
    class EruMapper {

        @Test
        @DisplayName("should serialize LocalDateTime with the project pattern")
        void givenLocalDateTime_whenWrite_shouldUseProjectPattern() {
            var result = config.eruMapper().writeValueAsString(LocalDateTime.of(2025, 7, 10, 8, 5, 3));

            assertThat(result).isEqualTo("\"2025-07-10 08:05:03\"");
        }

        @Test
        @DisplayName("should serialize LocalDate as ISO local date")
        void givenLocalDate_whenWrite_shouldUseIsoFormat() {
            var result = config.eruMapper().writeValueAsString(LocalDate.of(2025, 7, 10));

            assertThat(result).isEqualTo("\"2025-07-10\"");
        }
    }

    @Nested
    @DisplayName("localDateTimeFormatter")
    class LocalDateTimeFormatterBean {

        @Test
        @DisplayName("should format LocalDateTime with the project pattern")
        void givenLocalDateTime_whenFormat_shouldUseProjectPattern() {
            var result = config.localDateTimeFormatter().format(LocalDateTime.of(2025, 7, 10, 8, 5, 3));

            assertThat(result).containsExactly("2025-07-10 08:05:03");
        }
    }

    @Nested
    @DisplayName("bigDecimalFormatter")
    class BigDecimalFormatterBean {

        @Test
        @DisplayName("should format BigDecimal as Brazilian currency")
        void givenBigDecimal_whenFormat_shouldUseBrazilianCurrency() {
            var result = config.bigDecimalFormatter().format(new BigDecimal("1234.5"));

            assertThat(result).hasSize(1);
            assertThat(result[0]).isEqualTo("R$ 1.234,50");
        }
    }
}
