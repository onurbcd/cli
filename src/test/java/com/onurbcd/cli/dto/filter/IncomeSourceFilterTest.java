package com.onurbcd.cli.dto.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceFilterTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should carry the active flag and the trimmed search term")
        void givenValues_shouldCarryThem() {
            var result = IncomeSourceFilter.of(true, "  term ");

            assertThat(result).isInstanceOf(AbstractFilterable.class);
            assertThat(result.isActive()).isTrue();
            assertThat(result.getSearch()).isEqualTo("term");
        }

        @Test
        @DisplayName("should keep null values null")
        void givenNulls_shouldKeepNulls() {
            var result = IncomeSourceFilter.of(null, null);

            assertThat(result.isActive()).isNull();
            assertThat(result.getSearch()).isNull();
        }
    }
}
