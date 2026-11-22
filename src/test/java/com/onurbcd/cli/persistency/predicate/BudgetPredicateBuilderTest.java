package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.budget.RefDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.persistency.entity.QBudget;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetPredicateBuilderTest {

    private static final QBudget PATH = QBudget.budget;

    private static BudgetFilter filter() {
        return BudgetFilter.builder().build();
    }

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the budget id")
        void givenId_whenId_shouldFilterById() {
            assertThat(BudgetPredicateBuilder.id(ID)).isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
        }
    }

    @Nested
    @DisplayName("ref")
    class Ref {

        @Test
        @DisplayName("should filter by reference year then month")
        void givenRef_whenRef_shouldFilterYearAndMonth() {
            var result = BudgetPredicateBuilder.ref(RefDto.of((short) 2025, (short) 7));

            assertThat(result).isEqualTo(new BooleanBuilder()
                    .and(PATH.refYear.eq((short) 2025))
                    .and(PATH.refMonth.eq((short) 7)));
        }

        @Test
        @DisplayName("when year and month are null, should not add criteria")
        void givenNullYearAndMonth_whenRef_shouldReturnEmptyPredicate() {
            var result = BudgetPredicateBuilder.ref(RefDto.of(null, null));

            assertThat(result).isEqualTo(new BooleanBuilder());
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        static Stream<Arguments> singleCriterion() {
            var year = filter();
            year.setRefYear((short) 2025);
            var month = filter();
            month.setRefMonth((short) 7);
            var billType = filter();
            billType.setBillTypeId(BILL_TYPE_ID);
            var paid = filter();
            paid.setPaid(false);
            var search = filter();
            search.setSearch(" abc ");
            var active = filter();
            active.setActive(false);

            return Stream.of(
                    Arguments.of("empty", filter(), new BooleanBuilder()),
                    Arguments.of("refYear", year, new BooleanBuilder().and(PATH.refYear.eq((short) 2025))),
                    Arguments.of("refMonth", month, new BooleanBuilder().and(PATH.refMonth.eq((short) 7))),
                    Arguments.of("billTypeId", billType, new BooleanBuilder().and(PATH.billType.id.eq(BILL_TYPE_ID))),
                    Arguments.of("paid", paid, new BooleanBuilder().and(PATH.paid.eq(false))),
                    Arguments.of("search", search, new BooleanBuilder().and(PATH.name.containsIgnoreCase("abc"))),
                    Arguments.of("active", active, new BooleanBuilder().and(PATH.active.eq(false))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("singleCriterion")
        @DisplayName("should apply only the informed criterion")
        void givenSingleCriterion_whenAll_shouldApplyOnlyIt(String name, BudgetFilter filter, Predicate expected) {
            assertThat(BudgetPredicateBuilder.all(filter)).isEqualTo(expected);
        }

        @Test
        @DisplayName("with all criteria, should combine them in order")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = BudgetFilter.builder()
                    .refYear((short) 2025).refMonth((short) 7).billTypeId(BILL_TYPE_ID).paid(true)
                    .search("abc").active(true).build();

            assertThat(BudgetPredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.refYear.eq((short) 2025))
                    .and(PATH.refMonth.eq((short) 7))
                    .and(PATH.billType.id.eq(BILL_TYPE_ID))
                    .and(PATH.paid.eq(true))
                    .and(PATH.name.containsIgnoreCase("abc"))
                    .and(PATH.active.eq(true)));
        }
    }
}
