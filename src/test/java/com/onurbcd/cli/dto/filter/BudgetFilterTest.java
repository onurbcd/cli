package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.TestClock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetFilterTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("with year and month, should keep them and carry every other value")
        void givenYearAndMonth_shouldKeepThem() {
            var result = BudgetFilter.of(true, "  term ", (short) 2024, (short) 3, BILL_TYPE_ID, false, TestClock.DEFAULT);

            assertThat(result.isActive()).isTrue();
            assertThat(result.getSearch()).isEqualTo("term");
            assertThat(result.getRefYear()).isEqualTo((short) 2024);
            assertThat(result.getRefMonth()).isEqualTo((short) 3);
            assertThat(result.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
            assertThat(result.getPaid()).isFalse();
        }

        @Test
        @DisplayName("without year and month, should use the clock's current ones")
        void givenNoYearAndMonth_shouldUseClock() {
            var result = BudgetFilter.of(null, null, null, null, null, null, TestClock.DEFAULT);

            assertThat(result.getRefYear()).isEqualTo((short) 2025);
            assertThat(result.getRefMonth()).isEqualTo((short) 7);
            assertThat(result.isActive()).isNull();
            assertThat(result.getSearch()).isNull();
            assertThat(result.getBillTypeId()).isNull();
            assertThat(result.getPaid()).isNull();
        }
    }

    @Test
    @DisplayName("setters replace the fields")
    void givenSetters_shouldReplaceFields() {
        var filter = BudgetFilter.builder().build();

        filter.setRefYear((short) 2020);
        filter.setRefMonth((short) 5);
        filter.setBillTypeId(BILL_TYPE_ID);
        filter.setPaid(true);

        assertThat(filter.getRefYear()).isEqualTo((short) 2020);
        assertThat(filter.getRefMonth()).isEqualTo((short) 5);
        assertThat(filter.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
        assertThat(filter.getPaid()).isTrue();
    }
}
