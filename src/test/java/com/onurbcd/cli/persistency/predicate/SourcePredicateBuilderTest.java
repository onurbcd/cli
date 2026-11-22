package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.persistency.entity.QSource;
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

import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class SourcePredicateBuilderTest {

    private static final QSource PATH = QSource.source;

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the source id")
        void givenId_whenId_shouldFilterById() {
            assertThat(SourcePredicateBuilder.id(ID)).isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
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
            var builder = new SourcePredicateBuilder();

            var returned = builder.search(search);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder());
        }

        @Test
        @DisplayName("when search has text, should match source or income source name")
        void givenText_whenSearch_shouldMatchSourceOrIncomeSourceName() {
            var builder = new SourcePredicateBuilder();

            var returned = builder.search("abc");

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder().and(
                    PATH.name.containsIgnoreCase("abc").or(PATH.incomeSource.name.containsIgnoreCase("abc"))));
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        static Stream<Arguments> singleCriterion() {
            return Stream.of(
                    Arguments.of("empty", SourceFilter.of(null, null, null, null, null), new BooleanBuilder()),
                    Arguments.of("incomeSourceId", SourceFilter.of(null, null, SOURCE_ID, null, null),
                            new BooleanBuilder().and(PATH.incomeSource.id.eq(SOURCE_ID))),
                    Arguments.of("sourceType", SourceFilter.of(null, null, null, SourceType.USABLE, null),
                            new BooleanBuilder().and(PATH.sourceType.eq(SourceType.USABLE))),
                    Arguments.of("currencyType", SourceFilter.of(null, null, null, null, CurrencyType.DIGITAL),
                            new BooleanBuilder().and(PATH.currencyType.eq(CurrencyType.DIGITAL))),
                    Arguments.of("search", SourceFilter.of(null, " abc ", null, null, null),
                            new BooleanBuilder().and(PATH.name.containsIgnoreCase("abc")
                                    .or(PATH.incomeSource.name.containsIgnoreCase("abc")))),
                    Arguments.of("active", SourceFilter.of(false, null, null, null, null),
                            new BooleanBuilder().and(PATH.active.eq(false))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("singleCriterion")
        @DisplayName("should apply only the informed criterion")
        void givenSingleCriterion_whenAll_shouldApplyOnlyIt(String name, SourceFilter filter, Predicate expected) {
            assertThat(SourcePredicateBuilder.all(filter)).isEqualTo(expected);
        }

        @Test
        @DisplayName("with all criteria, should combine them in order")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = SourceFilter.of(true, "abc", SOURCE_ID, SourceType.UNUSABLE, CurrencyType.MONEY);

            assertThat(SourcePredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.incomeSource.id.eq(SOURCE_ID))
                    .and(PATH.sourceType.eq(SourceType.UNUSABLE))
                    .and(PATH.currencyType.eq(CurrencyType.MONEY))
                    .and(PATH.name.containsIgnoreCase("abc").or(PATH.incomeSource.name.containsIgnoreCase("abc")))
                    .and(PATH.active.eq(true)));
        }
    }
}
