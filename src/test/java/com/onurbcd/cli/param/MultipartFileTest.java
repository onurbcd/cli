package com.onurbcd.cli.param;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartFileTest {

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @ParameterizedTest(name = "size {0} -> empty {1}")
        @CsvSource({"0, true", "1, false", "1024, false"})
        void givenSize_whenIsEmpty_shouldBeEmptyOnlyForZero(long size, boolean expected) {
            var file = MultipartFile.builder().size(size).build();

            assertThat(file.isEmpty()).isEqualTo(expected);
        }

        @Test
        @DisplayName("when size is unknown (null), should be empty instead of throwing")
        void givenNullSize_whenIsEmpty_shouldBeEmpty() {
            var file = MultipartFile.builder().size(null).build();

            assertThat(file.isEmpty()).isTrue();
        }
    }
}
