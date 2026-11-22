package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.persistency.entity.QIncomeSource;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BasePredicateBuilderTest {

    private static final QIncomeSource PATH = QIncomeSource.incomeSource;

    private static final class TestBuilder extends BasePredicateBuilder {

        TestBuilder() {
            super(PATH.name, PATH.active);
        }
    }

    @Nested
    @DisplayName("build")
    class Build {

        @Test
        @DisplayName("without any criteria, should return an empty predicate")
        void givenNoCriteria_whenBuild_shouldReturnEmptyPredicate() {
            Predicate result = new TestBuilder().build();

            assertThat(result).isEqualTo(new BooleanBuilder());
            assertThat(((BooleanBuilder) result).hasValue()).isFalse();
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "\t\n"})
        @DisplayName("when search is null or blank, should not add criteria")
        void givenNullOrBlank_whenSearch_shouldNotAddCriteria(String search) {
            var builder = new TestBuilder();

            var returned = builder.search(search);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder());
        }

        @Test
        @DisplayName("when search has text, should filter the name ignoring case")
        void givenText_whenSearch_shouldFilterNameIgnoringCase() {
            var builder = new TestBuilder();

            var returned = builder.search("abc");

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build())
                    .isEqualTo(new BooleanBuilder().and(PATH.name.containsIgnoreCase("abc")));
        }
    }

    @Nested
    @DisplayName("active")
    class Active {

        @Test
        @DisplayName("when active is null, should not add criteria")
        void givenNull_whenActive_shouldNotAddCriteria() {
            var builder = new TestBuilder();

            var returned = builder.active(null);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("when active has a value, should filter by it")
        void givenValue_whenActive_shouldFilterByIt(boolean active) {
            var builder = new TestBuilder();

            var returned = builder.active(active);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder().and(PATH.active.eq(active)));
        }

        @Test
        @DisplayName("when combined with search, should AND both criteria in order")
        void givenSearchAndActive_whenBuild_shouldCombineCriteria() {
            var result = new TestBuilder().search("abc").active(true).build();

            assertThat(result).isEqualTo(new BooleanBuilder()
                    .and(PATH.name.containsIgnoreCase("abc"))
                    .and(PATH.active.eq(true)));
        }
    }
}
