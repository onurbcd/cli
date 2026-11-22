package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import com.onurbcd.cli.validator.SequenceValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetSequenceServiceTest {

    @Mock
    private BudgetRepository repository;
    @Mock
    private SequenceValidator validator;

    @Test
    @DisplayName("should be registered as the budget sequence service")
    void shouldBeQualifiedAsBudgetSequence() {
        var annotation = BudgetSequenceService.class.getAnnotation(PrimeService.class);

        assertThat(annotation.value()).isEqualTo(Domain.BUDGET_SEQUENCE);
    }

    @Test
    @DisplayName("should read the next sequence from the budget repository")
    void givenMaxSequence_whenGetNextSequence_shouldUseBudgetRepository() {
        var param = SequenceParam.of((short) 2024, (short) 3, null, null, null);
        when(repository.getMaxSequence(param)).thenReturn((short) 7);
        var service = new BudgetSequenceService(repository, validator);

        var result = service.getNextSequence(param);

        assertThat(result).isEqualTo((short) 8);
        verify(repository).getMaxSequence(param);
        verifyNoMoreInteractions(repository, validator);
    }

    @Test
    @DisplayName("should validate swaps with the budget sequence validator")
    void givenSwap_whenSwapSequence_shouldUseInjectedValidator() {
        var param = SequenceParam.of((short) 2024, (short) 3, (short) 5, null, null);
        var service = new BudgetSequenceService(repository, validator);

        service.swapSequence(param, Direction.UP);

        verify(validator).validate(same(param), any(SequenceParam.class),
                eq(Direction.UP));
        verify(repository, times(3)).updateSequence(any(SequenceParam.class));
        verifyNoMoreInteractions(repository, validator);
    }
}
