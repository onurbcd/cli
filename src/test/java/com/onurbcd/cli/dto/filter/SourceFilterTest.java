package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class SourceFilterTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should carry every value in its own field")
        void givenValues_shouldCarryThem() {
            var result = SourceFilter.of(true, "  term ", PARENT_ID, SourceType.USABLE, CurrencyType.DIGITAL);

            assertThat(result.isActive()).isTrue();
            assertThat(result.getSearch()).isEqualTo("term");
            assertThat(result.getIncomeSourceId()).isEqualTo(PARENT_ID);
            assertThat(result.getSourceType()).isEqualTo(SourceType.USABLE);
            assertThat(result.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
        }

        @Test
        @DisplayName("should keep null values null")
        void givenNulls_shouldKeepNulls() {
            var result = SourceFilter.of(null, null, null, null, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(SourceFilter.builder().build());
        }
    }

    @Test
    @DisplayName("setters replace the fields")
    void givenSetters_shouldReplaceFields() {
        var filter = SourceFilter.builder().build();

        filter.setIncomeSourceId(PARENT_ID);
        filter.setSourceType(SourceType.UNUSABLE);
        filter.setCurrencyType(CurrencyType.MONEY);

        assertThat(filter.getIncomeSourceId()).isEqualTo(PARENT_ID);
        assertThat(filter.getSourceType()).isEqualTo(SourceType.UNUSABLE);
        assertThat(filter.getCurrencyType()).isEqualTo(CurrencyType.MONEY);
    }
}
