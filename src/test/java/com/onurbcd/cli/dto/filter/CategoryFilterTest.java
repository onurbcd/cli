package com.onurbcd.cli.dto.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryFilterTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should carry every value in its own field")
        void givenValues_shouldCarryThem() {
            var result = CategoryFilter.of(true, "  term ", PARENT_ID, (short) 2, false);

            assertThat(result.isActive()).isTrue();
            assertThat(result.getSearch()).isEqualTo("term");
            assertThat(result.getParentId()).isEqualTo(PARENT_ID);
            assertThat(result.getLevel()).isEqualTo((short) 2);
            assertThat(result.getLastBranch()).isFalse();
        }

        @Test
        @DisplayName("should keep null values null")
        void givenNulls_shouldKeepNulls() {
            var result = CategoryFilter.of(null, null, null, null, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(CategoryFilter.builder().build());
        }
    }

    @Test
    @DisplayName("setters replace the fields")
    void givenSetters_shouldReplaceFields() {
        var filter = CategoryFilter.builder().build();

        filter.setParentId(PARENT_ID);
        filter.setLevel((short) 3);
        filter.setLastBranch(true);

        assertThat(filter.getParentId()).isEqualTo(PARENT_ID);
        assertThat(filter.getLevel()).isEqualTo((short) 3);
        assertThat(filter.getLastBranch()).isTrue();
    }
}
