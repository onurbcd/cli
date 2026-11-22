package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.TestConstants;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.persistency.entity.Entityable;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MiddlewareRepositoryTest {

    @SuppressWarnings("rawtypes")
    private final MiddlewareRepository<Entityable, Dtoable> repository =
            Mockito.mock(MiddlewareRepository.class, Mockito.CALLS_REAL_METHODS);

    @Nested
    @DisplayName("getSingle")
    class GetSingle {

        @Test
        @DisplayName("default implementation returns null")
        void givenId_whenGetSingle_shouldReturnNull() {
            assertThat(repository.getSingle(TestConstants.ID)).isNull();
        }
    }

    @Nested
    @DisplayName("mainQuery")
    class MainQuery {

        @Test
        @DisplayName("default implementation returns a new empty query on every call")
        void givenPredicate_whenMainQuery_shouldReturnNewQuery() {
            BooleanTemplate predicate = Expressions.booleanTemplate("true");

            var first = repository.mainQuery(predicate);
            var second = repository.mainQuery(predicate);

            assertThat(first).isNotNull().isNotSameAs(second);
            assertThat(second).isNotNull();
        }
    }

    @Nested
    @DisplayName("columns")
    class Columns {

        @Test
        @DisplayName("default implementation projects the Dtoable type")
        void whenColumns_shouldProjectDtoable() {
            var result = repository.columns();

            assertThat(result.getType()).isEqualTo(Dtoable.class);
        }
    }

    @Nested
    @DisplayName("entityPathBase")
    class EntityPath {

        @Test
        @DisplayName("default implementation returns a blank path over Entityable")
        void whenEntityPathBase_shouldReturnBlankEntityablePath() {
            var result = repository.entityPathBase();

            assertThat(result.getType()).isEqualTo(Entityable.class);
            assertThat(result.getMetadata().getName()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSum")
    class GetSum {

        @Test
        @DisplayName("default implementation returns zero")
        void givenPredicate_whenGetSum_shouldReturnZero() {
            var result = repository.getSum(Expressions.booleanTemplate("true"));

            assertThat(result).isSameAs(BigDecimal.ZERO);
        }
    }
}
