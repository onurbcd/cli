package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.persistency.entity.QBalance;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BalancePredicateBuilderTest {

    private static final QBalance PATH = QBalance.balance;

    private static final LocalDate DATE = LocalDate.of(2025, 7, 15);

    private static BalanceFilter filter() {
        return BalanceFilter.builder().build();
    }

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the balance id")
        void givenId_whenId_shouldFilterById() {
            assertThat(BalancePredicateBuilder.id(ID)).isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "  "})
        @DisplayName("when search is null or blank, should not add criteria")
        void givenNullOrBlank_whenSearch_shouldNotAddCriteria(String search) {
            var builder = new BalancePredicateBuilder();

            var returned = builder.search(search);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder());
        }

        @Test
        @DisplayName("when search has text, should match code or description")
        void givenText_whenSearch_shouldMatchCodeOrDescription() {
            var builder = new BalancePredicateBuilder();

            var returned = builder.search("abc");

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder().and(
                    PATH.code.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc"))));
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        static Stream<Arguments> singleCriterion() {
            var date = filter();
            date.setDayCalendarDate(DATE);
            var year = filter();
            year.setDayCalendarYear((short) 2025);
            var month = filter();
            month.setDayCalendarMonth((short) 7);
            var dayInMonth = filter();
            dayInMonth.setDayCalendarDayInMonth((short) 15);
            var source = filter();
            source.setSourceId(SOURCE_ID);
            var category = filter();
            category.setCategoryId(CATEGORY_ID);
            var balanceType = filter();
            balanceType.setBalanceType(BalanceType.OUTCOME);
            var paymentType = filter();
            paymentType.setPaymentType(PaymentType.CREDIT_CARD);
            var search = filter();
            search.setSearch(" abc ");
            var active = filter();
            active.setActive(false);

            return Stream.of(
                    Arguments.of("empty", filter(), new BooleanBuilder()),
                    Arguments.of("dayCalendarDate", date,
                            new BooleanBuilder().and(PATH.day.calendarDate.eq(DATE))),
                    Arguments.of("dayCalendarYear", year,
                            new BooleanBuilder().and(PATH.day.calendarYear.eq((short) 2025))),
                    Arguments.of("dayCalendarMonth", month,
                            new BooleanBuilder().and(PATH.day.calendarMonth.eq((short) 7))),
                    Arguments.of("dayCalendarDayInMonth", dayInMonth,
                            new BooleanBuilder().and(PATH.day.calendarDayInMonth.eq((short) 15))),
                    Arguments.of("sourceId", source, new BooleanBuilder().and(PATH.source.id.eq(SOURCE_ID))),
                    Arguments.of("categoryId", category, new BooleanBuilder().and(PATH.category.id.eq(CATEGORY_ID))),
                    Arguments.of("balanceType", balanceType,
                            new BooleanBuilder().and(PATH.balanceType.eq(BalanceType.OUTCOME))),
                    Arguments.of("paymentType", paymentType,
                            new BooleanBuilder().and(PATH.paymentType.eq(PaymentType.CREDIT_CARD))),
                    Arguments.of("search", search, new BooleanBuilder().and(
                            PATH.code.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc")))),
                    Arguments.of("active", active, new BooleanBuilder().and(PATH.active.eq(false))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("singleCriterion")
        @DisplayName("should apply only the informed criterion")
        void givenSingleCriterion_whenAll_shouldApplyOnlyIt(String name, BalanceFilter filter, Predicate expected) {
            assertThat(BalancePredicateBuilder.all(filter)).isEqualTo(expected);
        }

        @Test
        @DisplayName("with all criteria, should combine them in order")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = BalanceFilter.builder()
                    .dayCalendarDate(DATE).dayCalendarYear((short) 2025).dayCalendarMonth((short) 7)
                    .dayCalendarDayInMonth((short) 15).sourceId(SOURCE_ID).categoryId(CATEGORY_ID)
                    .balanceType(BalanceType.INCOME).paymentType(PaymentType.CREDIT_CARD)
                    .search("abc").active(true).build();

            assertThat(BalancePredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.day.calendarDate.eq(DATE))
                    .and(PATH.day.calendarYear.eq((short) 2025))
                    .and(PATH.day.calendarMonth.eq((short) 7))
                    .and(PATH.day.calendarDayInMonth.eq((short) 15))
                    .and(PATH.source.id.eq(SOURCE_ID))
                    .and(PATH.category.id.eq(CATEGORY_ID))
                    .and(PATH.balanceType.eq(BalanceType.INCOME))
                    .and(PATH.paymentType.eq(PaymentType.CREDIT_CARD))
                    .and(PATH.code.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc")))
                    .and(PATH.active.eq(true)));
        }
    }
}
