package com.onurbcd.cli.formatter;

import com.onurbcd.cli.enums.Codeable;
import com.onurbcd.cli.enums.CurrencyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CodeableFormatterTest {

    private final CodeableFormatter formatter = new CodeableFormatter();

    @Nested
    @DisplayName("format")
    class Format {

        @Test
        @DisplayName("when value is Codeable, should return its code")
        void givenCodeable_whenFormat_shouldReturnCode() {
            Codeable codeable = () -> "CODE";

            var result = formatter.format(codeable);

            assertThat(result).containsExactly("CODE");
        }

        @ParameterizedTest
        @EnumSource(CurrencyType.class)
        @DisplayName("when value is a Codeable enum, should return the constant code")
        void givenCodeableEnum_whenFormat_shouldReturnConstantCode(CurrencyType currencyType) {
            var result = formatter.format(currencyType);

            assertThat(result).containsExactly(currencyType.getCode());
        }

        @ParameterizedTest
        @ValueSource(strings = {"CODE", ""})
        @DisplayName("when value is not Codeable, should return a single empty string")
        void givenNonCodeable_whenFormat_shouldReturnEmpty(String value) {
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
}
