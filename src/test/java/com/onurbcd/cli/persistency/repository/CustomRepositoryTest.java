package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import com.onurbcd.cli.persistency.entity.QIncomeSource;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CustomRepositoryTest {

    private static final QIncomeSource PATH = QIncomeSource.incomeSource;
    private static final Predicate PREDICATE = Expressions.booleanTemplate("true");

    private final List<OrderSpecifier<?>> capturedOrders = new ArrayList<>();
    private CustomRepository<IncomeSourceDto, IncomeSource> repository;
    private JPAQuery<IncomeSourceDto> columnsQuery;
    private JPAQuery<Object> mainQuery;
    private JPAQuery<Object> countQuery;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        repository = mock(CustomRepository.class, Mockito.CALLS_REAL_METHODS);
        columnsQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);
        mainQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);
        countQuery = mock(JPAQuery.class, Mockito.RETURNS_SELF);

        doReturnEntityPath(repository);
        when(repository.columns()).thenReturn(Projections.bean(IncomeSourceDto.class, PATH.name));
        when(mainQuery.select(ArgumentMatchers.<Expression<IncomeSourceDto>>any()))
                .thenReturn(columnsQuery);
        when(columnsQuery.orderBy(any(OrderSpecifier[].class))).thenAnswer(invocation -> {
            capturedOrders.clear();
            for (var arg : invocation.getRawArguments()) {
                capturedOrders.addAll(List.of((OrderSpecifier<?>[]) arg));
            }
            return columnsQuery;
        });
    }

    private static void doReturnEntityPath(CustomRepository<IncomeSourceDto, IncomeSource> repo) {
        EntityPathBase<IncomeSource> path = PATH;
        when(repo.entityPathBase()).thenReturn(path);
    }

    private static List<IncomeSourceDto> dtos(int size) {
        var result = new ArrayList<IncomeSourceDto>();

        for (var i = 0; i < size; i++) {
            result.add(new IncomeSourceDto());
        }

        return result;
    }

    @Nested
    @DisplayName("getAll(predicate)")
    class GetAllPredicate {

        @Test
        @DisplayName("should fetch the columns query of the main query without paging")
        void givenPredicate_whenGetAll_shouldReturnFetchedRows() {
            var rows = dtos(3);
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery);
            when(columnsQuery.fetch()).thenReturn(rows);

            var result = repository.getAll(PREDICATE);

            assertThat(result).isEqualTo(rows);
            verify(repository).mainQuery(PREDICATE);
            verify(mainQuery).select(repository.columns());
            verify(columnsQuery).fetch();
            verifyNoMoreInteractions(columnsQuery);
        }
    }

    @Nested
    @DisplayName("getAll(predicate, pageable)")
    class GetAllPageable {

        @Test
        @DisplayName("when the page is not full, should skip the count query and use the row count as total")
        void givenPartialPage_whenGetAll_shouldNotCount() {
            var rows = dtos(2);
            var pageable = PageRequest.of(0, 5, Sort.by("name"));
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery);
            when(columnsQuery.fetch()).thenReturn(rows);

            var result = repository.getAll(PREDICATE, pageable);

            assertThat(result.getContent()).containsExactlyElementsOf(rows);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPageable()).isEqualTo(pageable);
            verify(repository, times(1)).mainQuery(PREDICATE);
            verify(columnsQuery).offset(0L);
            verify(columnsQuery).limit(5L);
            verify(countQuery, never()).fetchFirst();
        }

        @Test
        @DisplayName("when the page is full, should run the count query and use its result as total")
        void givenFullPage_whenGetAll_shouldUseCountResult() {
            var rows = dtos(2);
            var pageable = PageRequest.of(1, 2);
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery, countQuery);
            when(columnsQuery.fetch()).thenReturn(rows);
            when(countQuery.fetchFirst()).thenReturn(17L);

            var result = repository.getAll(PREDICATE, pageable);

            assertThat(result.getContent()).containsExactlyElementsOf(rows);
            assertThat(result.getTotalElements()).isEqualTo(17);
            verify(repository, times(2)).mainQuery(PREDICATE);
            verify(columnsQuery).offset(2L);
            verify(columnsQuery).limit(2L);
            verify(countQuery).select(any(Expression.class));
            verify(countQuery).fetchFirst();
        }

        @Test
        @DisplayName("when the count query returns null, should treat the total as zero")
        void givenNullCount_whenGetAll_shouldTreatTotalAsZero() {
            var rows = dtos(2);
            var pageable = PageRequest.of(3, 2);
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery, countQuery);
            when(columnsQuery.fetch()).thenReturn(rows);
            when(countQuery.fetchFirst()).thenReturn(null);

            var result = repository.getAll(PREDICATE, pageable);

            // Spring corrects a total smaller than the visible rows to offset + size
            assertThat(result.getTotalElements()).isEqualTo(8);
            verify(countQuery).fetchFirst();
        }

        @Test
        @DisplayName("without sort, should not order the query")
        void givenUnsorted_whenGetAll_shouldPassNoOrder() {
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery);
            when(columnsQuery.fetch()).thenReturn(dtos(1));

            repository.getAll(PREDICATE, PageRequest.of(0, 5));

            verify(columnsQuery).orderBy(any(OrderSpecifier[].class));
            assertThat(capturedOrders).isEmpty();
        }

        @Test
        @DisplayName("with ascending and descending sort, should order by each property in the given direction")
        void givenSort_whenGetAll_shouldOrderByEachProperty() {
            var sort = Sort.by(Sort.Order.asc("name"), Sort.Order.desc("active"));
            when(repository.mainQuery(PREDICATE)).thenReturn(mainQuery);
            when(columnsQuery.fetch()).thenReturn(dtos(1));

            repository.getAll(PREDICATE, PageRequest.of(0, 5, sort));

            assertThat(capturedOrders).hasSize(2);
            assertThat(capturedOrders.get(0).getOrder()).isEqualTo(Order.ASC);
            assertThat(capturedOrders.get(0).getTarget().toString()).isEqualTo("incomeSource.name");
            assertThat(capturedOrders.get(1).getOrder()).isEqualTo(Order.DESC);
            assertThat(capturedOrders.get(1).getTarget().toString()).isEqualTo("incomeSource.active");
        }
    }
}
