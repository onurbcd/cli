package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.dto.source.BalanceSumDto;
import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.mapper.SourceToEntityMapper;
import com.onurbcd.cli.param.UpdateSourceBalance;
import com.onurbcd.cli.persistency.entity.Source;
import com.onurbcd.cli.persistency.predicate.SourcePredicateBuilder;
import com.onurbcd.cli.persistency.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceServiceTest {

    private static final BigDecimal CURRENT = new BigDecimal("100.50");
    private static final BigDecimal VALUE = new BigDecimal("20.25");
    private static final BigDecimal RESULT = new BigDecimal("120.75");

    @Mock
    private SourceRepository repository;
    @Mock
    private SourceToEntityMapper toEntityMapper;

    private SourceService service;

    @BeforeEach
    void setUp() {
        service = new SourceService(repository, toEntityMapper);
    }

    @Nested
    @DisplayName("getUsableBalanceSum")
    class GetUsableBalanceSum {

        @Test
        @DisplayName("when the repository has a sum, should return it")
        void givenSum_shouldReturnIt() {
            when(repository.getBalanceSum(SourceType.USABLE)).thenReturn(CURRENT);

            var result = service.getUsableBalanceSum();

            assertThat(result).isSameAs(CURRENT);
            verify(repository).getBalanceSum(SourceType.USABLE);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when the repository returns null, should return zero")
        void givenNull_shouldReturnZero() {
            when(repository.getBalanceSum(SourceType.USABLE)).thenReturn(null);

            var result = service.getUsableBalanceSum();

            assertThat(result).isSameAs(BigDecimal.ZERO);
            verify(repository).getBalanceSum(SourceType.USABLE);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getBalanceSum")
    class GetBalanceSum {

        private final SourceFilter filter = SourceFilter.of(true, "abc", PARENT_ID, SourceType.USABLE,
                CurrencyType.DIGITAL);

        @Test
        @DisplayName("should sum with the source predicate for the partial and without it for the total")
        void shouldReturnPartialAndTotal() {
            var predicate = SourcePredicateBuilder.all(filter);
            when(repository.getSum(predicate)).thenReturn(VALUE);
            when(repository.getBalanceSum()).thenReturn(CURRENT);

            var result = service.getBalanceSum(filter);

            assertThat(result).usingRecursiveComparison().isEqualTo(new BalanceSumDto(VALUE, CURRENT));
            verify(repository).getSum(predicate);
            verify(repository).getBalanceSum();
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when the sums are null, should report zeros")
        void givenNullSums_shouldReportZeros() {
            var predicate = SourcePredicateBuilder.all(filter);
            when(repository.getSum(predicate)).thenReturn(null);
            when(repository.getBalanceSum()).thenReturn(null);

            var result = service.getBalanceSum(filter);

            assertThat(result.getPartial()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.getTotal()).isEqualTo(BigDecimal.ZERO);
            verify(repository).getSum(predicate);
            verify(repository).getBalanceSum();
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("updateBalance")
    class UpdateBalance {

        private final List<BigDecimal> operands = new ArrayList<>();
        private final BinaryOperator<BigDecimal> func = this::recordOperands;

        private BigDecimal recordOperands(BigDecimal current, BigDecimal value) {
            operands.add(current);
            operands.add(value);
            return RESULT;
        }

        @Test
        @DisplayName("when the source has a balance, should apply the function to it without reading the repository")
        void givenSourceBalance_shouldUseIt() {
            var source = Source.builder().id(ID).balance(CURRENT).build();

            service.updateBalance(new UpdateSourceBalance(source, func, VALUE));

            assertThat(operands).containsExactly(CURRENT, VALUE);
            verify(repository).updateBalance(ID, RESULT);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when the source has no balance, should read it from the repository")
        void givenNoSourceBalance_shouldReadRepository() {
            var source = Source.builder().id(ID).balance(null).build();
            when(repository.getBalance(ID)).thenReturn(CURRENT);

            service.updateBalance(new UpdateSourceBalance(source, func, VALUE));

            assertThat(operands).containsExactly(CURRENT, VALUE);
            verify(repository).getBalance(ID);
            verify(repository).updateBalance(ID, RESULT);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should query with the source predicate (not the generic one) through the custom query")
        void shouldUseSourcePredicate() {
            var filter = SourceFilter.of(true, "abc", PARENT_ID, SourceType.USABLE, CurrencyType.DIGITAL);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(new SourceDto()), pageable, 1);
            when(repository.getAll(SourcePredicateBuilder.all(filter), pageable)).thenReturn(page);

            var result = service.getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(SourcePredicateBuilder.all(filter), pageable);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(toEntityMapper);
        }
    }
}
