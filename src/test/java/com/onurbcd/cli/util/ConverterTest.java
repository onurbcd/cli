package com.onurbcd.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class ConverterTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "not-a-uuid", "1234", "11111111-1111-1111-1111-11111111111Z"})
    @DisplayName("blank or malformed values convert to null")
    void givenBlankOrMalformed_whenToUUID_shouldReturnNull(String value) {
        assertThat(Converter.toUUID(value)).isNull();
    }

    @Test
    @DisplayName("a valid textual UUID is parsed")
    void givenValidUuid_whenToUUID_shouldParse() {
        assertThat(Converter.toUUID(ID.toString())).isEqualTo(ID);
    }
}
