package com.onurbcd.cli.dto.filter;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractFilterableTest {

    @SuperBuilder
    private static final class TestFilter extends AbstractFilterable {
    }

    @Nested
    @DisplayName("active")
    class Active {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @NullSource
        @DisplayName("the builder value is returned by isActive")
        void givenBuilderActive_shouldReturnSameValue(Boolean active) {
            var filter = TestFilter.builder().active(active).build();

            assertThat(filter).isInstanceOf(Filterable.class);
            assertThat(filter.isActive()).isEqualTo(active);
        }

        @Test
        @DisplayName("setActive replaces the flag")
        void givenSetter_shouldReplaceFlag() {
            var filter = TestFilter.builder().active(true).build();

            filter.setActive(false);

            assertThat(filter.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("without a search term, should return null")
        void givenNoSearch_shouldReturnNull() {
            assertThat(TestFilter.builder().build().getSearch()).isNull();
        }

        @Test
        @DisplayName("should return the builder term trimmed")
        void givenPaddedSearch_shouldReturnTrimmed() {
            assertThat(TestFilter.builder().search("  term ").build().getSearch()).isEqualTo("term");
        }

        @Test
        @DisplayName("setSearch replaces the term, which is still trimmed on read")
        void givenSetter_shouldReplaceAndTrim() {
            var filter = TestFilter.builder().search("old").build();

            filter.setSearch("  new ");

            assertThat(filter.getSearch()).isEqualTo("new");
        }

        @Test
        @DisplayName("setSearch(null) clears the term")
        void givenNullSetter_shouldClear() {
            var filter = TestFilter.builder().search("old").build();

            filter.setSearch(null);

            assertThat(filter.getSearch()).isNull();
        }
    }
}
