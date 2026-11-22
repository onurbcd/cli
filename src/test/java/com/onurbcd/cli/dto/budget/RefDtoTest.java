package com.onurbcd.cli.dto.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefDtoTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should keep year and month in their own fields")
        void givenYearAndMonth_shouldKeepFields() {
            var dto = RefDto.of((short) 2024, (short) 3);

            assertThat(dto.getYear()).isEqualTo((short) 2024);
            assertThat(dto.getMonth()).isEqualTo((short) 3);
        }
    }

    @Test
    @DisplayName("setters replace year and month")
    void givenSetters_shouldReplaceFields() {
        var dto = RefDto.of((short) 2024, (short) 3);

        dto.setYear((short) 2025);
        dto.setMonth((short) 4);

        assertThat(dto.getYear()).isEqualTo((short) 2025);
        assertThat(dto.getMonth()).isEqualTo((short) 4);
    }
}
