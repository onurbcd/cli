package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.persistency.entity.QIncomeSource;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourcePredicateBuilderTest {

    private static final QIncomeSource PATH = QIncomeSource.incomeSource;

    @Test
    @DisplayName("should search by name and filter by the active flag of the income source")
    void givenSearchAndActive_whenBuild_shouldUseIncomeSourcePaths() {
        var result = new IncomeSourcePredicateBuilder().search("abc").active(false).build();

        assertThat(result).isEqualTo(new BooleanBuilder()
                .and(PATH.name.containsIgnoreCase("abc"))
                .and(PATH.active.eq(false)));
    }
}
