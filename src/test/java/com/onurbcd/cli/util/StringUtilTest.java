package com.onurbcd.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {

    private static Stream<Arguments> texts() {
        return Stream.of(
                Arguments.of("abc", "abc"),
                Arguments.of("  abc  ", "abc"),
                Arguments.of("a   b", "a b"),
                Arguments.of("  a \t b \n c  ", "a b c"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n \t"})
    @DisplayName("blank values normalize to null")
    void givenBlank_whenNormalizeSpace_shouldReturnNull(String value) {
        assertThat(StringUtil.normalizeSpace(value)).isNull();
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @MethodSource("texts")
    @DisplayName("non-blank values are trimmed and have inner whitespace collapsed")
    void givenText_whenNormalizeSpace_shouldCollapseWhitespace(String value, String expected) {
        assertThat(StringUtil.normalizeSpace(value)).isEqualTo(expected);
    }

    @Test
    @DisplayName("a value that is already normalized is returned unchanged")
    void givenNormalized_whenNormalizeSpace_shouldReturnEqualValue() {
        assertThat(StringUtil.normalizeSpace("already normal")).isEqualTo("already normal");
    }
}
