package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetSequenceValidatorTest {

    @Mock
    private BudgetRepository repository;

    @Test
    @DisplayName("is registered as the BUDGET_SEQUENCE_VALIDATION prime service")
    void shouldBeRegisteredForItsDomain() {
        var annotation = BudgetSequenceValidator.class.getAnnotation(PrimeService.class);

        assertThat(annotation.value()).isEqualTo(Domain.BUDGET_SEQUENCE_VALIDATION);
    }

    @Test
    @DisplayName("delegates the sequence rules to the shared validator backed by the Budget repository")
    void givenValidSwap_whenValidateSwapPosition_shouldQueryTheRepository() {
        var validator = new BudgetSequenceValidator(repository);
        var param = SequenceParam.of((short) 2025, (short) 7, (short) 1, (short) 2, BalanceType.INCOME);
        when(repository.getMaxSequence(param)).thenReturn((short) 2);

        assertThatCode(() -> validator.validateSwapPosition(param)).doesNotThrowAnyException();

        verify(repository).getMaxSequence(param);
        verifyNoMoreInteractions(repository);
    }
}
