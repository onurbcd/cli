package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.BillTypeFilter;
import com.onurbcd.cli.persistency.entity.QBillType;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypePredicateBuilderTest {

    private static final QBillType PATH = QBillType.billType;

    @Nested
    @DisplayName("id")
    class Id {

        @Test
        @DisplayName("should filter by the bill type id")
        void givenId_whenId_shouldFilterById() {
            assertThat(BillTypePredicateBuilder.id(ID))
                    .isEqualTo(new BooleanBuilder().and(PATH.id.eq(ID)));
        }
    }

    @Nested
    @DisplayName("all")
    class All {

        @Test
        @DisplayName("without criteria, should return an empty predicate")
        void givenEmptyFilter_whenAll_shouldReturnEmptyPredicate() {
            var filter = BillTypeFilter.of(null, null);

            assertThat(BillTypePredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder());
        }

        @Test
        @DisplayName("with all criteria, should apply trimmed search then active")
        void givenFullFilter_whenAll_shouldCombineCriteria() {
            var filter = BillTypeFilter.of(true, "  abc  ");

            assertThat(BillTypePredicateBuilder.all(filter)).isEqualTo(new BooleanBuilder()
                    .and(PATH.name.containsIgnoreCase("abc"))
                    .and(PATH.active.eq(true)));
        }
    }
}
