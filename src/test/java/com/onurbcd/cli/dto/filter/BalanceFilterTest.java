package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceFilterTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should carry every value in its own field and leave the date parts null")
        void givenValues_shouldCarryThem() {
            var result = BalanceFilter.of(true, "  term ", SOURCE_ID, CATEGORY_ID, BalanceType.INCOME, PaymentType.PIX);

            assertThat(result.isActive()).isTrue();
            assertThat(result.getSearch()).isEqualTo("term");
            assertThat(result.getSourceId()).isEqualTo(SOURCE_ID);
            assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
            assertThat(result.getBalanceType()).isEqualTo(BalanceType.INCOME);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.PIX);
            assertThat(result.getDayCalendarDate()).isNull();
            assertThat(result.getDayCalendarYear()).isNull();
            assertThat(result.getDayCalendarMonth()).isNull();
            assertThat(result.getDayCalendarDayInMonth()).isNull();
        }
    }

    @Nested
    @DisplayName("and")
    class And {

        @Test
        @DisplayName("with year, month and day, should store them and return the same filter")
        void givenAllParts_shouldStoreThem() {
            var filter = BalanceFilter.builder().build();

            var result = filter.and((short) 2024, (short) 3, (short) 15, TestClock.DEFAULT);

            assertThat(result).isSameAs(filter);
            assertThat(filter.getDayCalendarYear()).isEqualTo((short) 2024);
            assertThat(filter.getDayCalendarMonth()).isEqualTo((short) 3);
            assertThat(filter.getDayCalendarDayInMonth()).isEqualTo((short) 15);
        }

        @Test
        @DisplayName("without year and month, should use the clock's current ones and keep a null day")
        void givenNoYearAndMonth_shouldUseClock() {
            var filter = BalanceFilter.builder().build();

            filter.and(null, null, null, TestClock.DEFAULT);

            assertThat(filter.getDayCalendarYear()).isEqualTo((short) 2025);
            assertThat(filter.getDayCalendarMonth()).isEqualTo((short) 7);
            assertThat(filter.getDayCalendarDayInMonth()).isNull();
        }
    }

    @Test
    @DisplayName("setters replace the fields")
    void givenSetters_shouldReplaceFields() {
        var filter = BalanceFilter.builder().build();

        filter.setDayCalendarDate(LocalDate.of(2024, 3, 15));
        filter.setSourceId(SOURCE_ID);
        filter.setCategoryId(CATEGORY_ID);
        filter.setBalanceType(BalanceType.OUTCOME);
        filter.setPaymentType(PaymentType.CASH);
        filter.setDayCalendarYear((short) 2020);
        filter.setDayCalendarMonth((short) 5);
        filter.setDayCalendarDayInMonth((short) 6);

        assertThat(filter.getDayCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(filter.getSourceId()).isEqualTo(SOURCE_ID);
        assertThat(filter.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(filter.getBalanceType()).isEqualTo(BalanceType.OUTCOME);
        assertThat(filter.getPaymentType()).isEqualTo(PaymentType.CASH);
        assertThat(filter.getDayCalendarYear()).isEqualTo((short) 2020);
        assertThat(filter.getDayCalendarMonth()).isEqualTo((short) 5);
        assertThat(filter.getDayCalendarDayInMonth()).isEqualTo((short) 6);
    }
}
