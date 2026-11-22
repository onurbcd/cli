package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.persistency.entity.QCategory;
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
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryPredicateBuilderTest {

    private static final QCategory PATH = QCategory.category;

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the category id")
        void givenId_whenId_shouldFilterById() {
            assertThat(CategoryPredicateBuilder.id(ID)).isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
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
            var builder = new CategoryPredicateBuilder();

            var returned = builder.search(search);

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder());
        }

        @Test
        @DisplayName("when search has text, should match name or description")
        void givenText_whenSearch_shouldMatchNameOrDescription() {
            var builder = new CategoryPredicateBuilder();

            var returned = builder.search("abc");

            assertThat(returned).isSameAs(builder);
            assertThat(builder.build()).isEqualTo(new BooleanBuilder().and(
                    PATH.name.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc"))));
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        static Stream<Arguments> singleCriterion() {
            return Stream.of(
                    Arguments.of("empty", CategoryFilter.of(null, null, null, null, null), new BooleanBuilder()),
                    Arguments.of("parentId", CategoryFilter.of(null, null, PARENT_ID, null, null),
                            new BooleanBuilder().and(PATH.parent.id.eq(PARENT_ID))),
                    Arguments.of("level", CategoryFilter.of(null, null, null, (short) 2, null),
                            new BooleanBuilder().and(PATH.level.eq((short) 2))),
                    Arguments.of("lastBranch", CategoryFilter.of(null, null, null, null, false),
                            new BooleanBuilder().and(PATH.lastBranch.eq(false))),
                    Arguments.of("search", CategoryFilter.of(null, " abc ", null, null, null),
                            new BooleanBuilder().and(
                                    PATH.name.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc")))),
                    Arguments.of("active", CategoryFilter.of(false, null, null, null, null),
                            new BooleanBuilder().and(PATH.active.eq(false))));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("singleCriterion")
        @DisplayName("should apply only the informed criterion")
        void givenSingleCriterion_whenAll_shouldApplyOnlyIt(String name, CategoryFilter filter, Predicate expected) {
            assertThat(CategoryPredicateBuilder.all(filter)).isEqualTo(expected);
        }

        @Test
        @DisplayName("with all criteria, should combine them in order")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = CategoryFilter.of(true, "abc", PARENT_ID, (short) 3, true);

            assertThat(CategoryPredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.parent.id.eq(PARENT_ID))
                    .and(PATH.level.eq((short) 3))
                    .and(PATH.lastBranch.eq(true))
                    .and(PATH.name.containsIgnoreCase("abc").or(PATH.description.containsIgnoreCase("abc")))
                    .and(PATH.active.eq(true)));
        }
    }
}
