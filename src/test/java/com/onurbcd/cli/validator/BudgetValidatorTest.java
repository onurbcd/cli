package com.onurbcd.cli.validator;

import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.dto.budget.CopyBudgetDto;
import com.onurbcd.cli.dto.budget.RefDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.predicate.BudgetPredicateBuilder;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetValidatorTest {

    private static final short SEQUENCE = 2;
    private static final short YEAR = 2025;
    private static final short MONTH = 6;

    @Mock
    private BudgetRepository repository;

    @InjectMocks
    private BudgetValidator validator;

    private static BudgetSaveDto saveDto() {
        return BudgetSaveDto.builder().sequence(SEQUENCE).refYear(YEAR).refMonth(MONTH).build();
    }

    private static Budget budget() {
        return Budget.builder().id(ID).sequence(SEQUENCE).refYear(YEAR).refMonth(MONTH).build();
    }

    private static BudgetDto budgetDto() {
        var dto = new BudgetDto();
        dto.setId(ID);
        return dto;
    }

    static Stream<Arguments> changedFields() {
        return Stream.of(
                Arguments.of("sequence", (Consumer<BudgetSaveDto>) d -> d.setSequence((short) 3),
                        ErrorCode.SEQUENCE_CHANGED),
                Arguments.of("null sequence", (Consumer<BudgetSaveDto>) d -> d.setSequence(null),
                        ErrorCode.SEQUENCE_CHANGED),
                Arguments.of("reference year", (Consumer<BudgetSaveDto>) d -> d.setRefYear((short) (YEAR + 1)),
                        ErrorCode.REFERENCE_CHANGED),
                Arguments.of("reference month", (Consumer<BudgetSaveDto>) d -> d.setRefMonth((short) (MONTH + 1)),
                        ErrorCode.REFERENCE_CHANGED));
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("accepts a new budget without touching the repository")
        void givenNoId_shouldNotThrow() {
            var dto = saveDto();
            dto.setSequence(null);

            assertThatCode(() -> validator.validate(dto, null, null)).doesNotThrowAnyException();

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("rejects an id that has no entity")
        void givenIdWithoutEntity_shouldThrowNotFound() {
            var dto = saveDto();

            assertThatThrownBy(() -> validator.validate(dto, null, ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST.format(ID));
                    });
        }

        @Test
        @DisplayName("accepts an unchanged budget and leaves both arguments untouched")
        void givenUnchangedBudget_shouldNotThrow() {
            var dto = saveDto();
            var budget = budget();

            assertThatCode(() -> validator.validate(dto, budget, ID)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(saveDto());
            assertThat(budget).usingRecursiveComparison().isEqualTo(budget());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.BudgetValidatorTest#changedFields")
        @DisplayName("rejects a changed immutable field with the matching error")
        void givenChangedField_shouldThrow(String label, Consumer<BudgetSaveDto> change, ErrorCode expected) {
            var dto = saveDto();
            change.accept(dto);

            assertThatThrownBy(() -> validator.validate(dto, budget(), ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(expected));
        }

        @Test
        @DisplayName("reports the current and the new sequence when it changed")
        void givenChangedSequence_shouldReportBothValues() {
            var dto = saveDto();
            dto.setSequence((short) 3);

            assertThatThrownBy(() -> validator.validate(dto, budget(), ID))
                    .isInstanceOfSatisfying(ApiException.class, e ->
                            assertThat(e.getMessage())
                                    .isEqualTo(ErrorCode.SEQUENCE_CHANGED.format(SEQUENCE, (short) 3)));
        }
    }

    static Stream<Arguments> invalidCopies() {
        return Stream.of(
                Arguments.of("null from", (Supplier<CopyBudgetDto>) () -> {
                    var copy = CopyBudgetDto.of((short) 2025, (short) 1, (short) 2025, (short) 2);
                    copy.setFrom(null);
                    return copy;
                }, ErrorCode.COPY_BUDGET_FROM_IS_NULL),
                Arguments.of("null to", (Supplier<CopyBudgetDto>) () -> {
                    var copy = CopyBudgetDto.of((short) 2025, (short) 1, (short) 2025, (short) 2);
                    copy.setTo(null);
                    return copy;
                }, ErrorCode.COPY_BUDGET_TO_IS_NULL),
                Arguments.of("null from year",
                        (Supplier<CopyBudgetDto>) () -> CopyBudgetDto.of(null, (short) 1, (short) 2025, (short) 2),
                        ErrorCode.COPY_BUDGET_FROM_YEAR_IS_NULL),
                Arguments.of("null from month",
                        (Supplier<CopyBudgetDto>) () -> CopyBudgetDto.of((short) 2025, null, (short) 2025, (short) 2),
                        ErrorCode.COPY_BUDGET_FROM_MONTH_IS_NULL),
                Arguments.of("null to year",
                        (Supplier<CopyBudgetDto>) () -> CopyBudgetDto.of((short) 2025, (short) 1, null, (short) 2),
                        ErrorCode.COPY_BUDGET_TO_YEAR_IS_NULL),
                Arguments.of("null to month",
                        (Supplier<CopyBudgetDto>) () -> CopyBudgetDto.of((short) 2025, (short) 1, (short) 2025, null),
                        ErrorCode.COPY_BUDGET_TO_MONTH_IS_NULL),
                Arguments.of("same month",
                        (Supplier<CopyBudgetDto>) () -> CopyBudgetDto.of((short) 2025, (short) 1, (short) 2025,
                                (short) 1),
                        ErrorCode.COPY_BUDGET_EQUAL_MONTH));
    }

    @Nested
    @DisplayName("validateCopy")
    class ValidateCopy {

        private final CopyBudgetDto copy = CopyBudgetDto.of((short) 2025, (short) 1, (short) 2025, (short) 2);
        private final RefDto from = RefDto.of((short) 2025, (short) 1);
        private final RefDto to = RefDto.of((short) 2025, (short) 2);

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.BudgetValidatorTest#invalidCopies")
        @DisplayName("rejects an incomplete or same-month copy before reaching the repository")
        void givenInvalidCopy_shouldThrowWithoutQuerying(String label, Supplier<CopyBudgetDto> invalid,
                                                         ErrorCode expected) {

            var dto = invalid.get();

            assertThatThrownBy(() -> validator.validateCopy(dto))
                    .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(expected));

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("rejects a source month without budgets and does not look at the target")
        void givenEmptySource_shouldThrowFromIsEmpty() {
            when(repository.getAll(BudgetPredicateBuilder.ref(from))).thenReturn(List.of());

            assertThatThrownBy(() -> validator.validateCopy(copy))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.COPY_BUDGET_FROM_IS_EMPTY);
                        assertThat(e.getMessage())
                                .isEqualTo(ErrorCode.COPY_BUDGET_FROM_IS_EMPTY.format((short) 1, (short) 2025));
                    });

            verify(repository).getAll(BudgetPredicateBuilder.ref(from));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("rejects a target month that already has budgets")
        void givenExistingTarget_shouldThrowToAlreadyExists() {
            when(repository.getAll(BudgetPredicateBuilder.ref(from))).thenReturn(List.of(budgetDto()));
            when(repository.exists(BudgetPredicateBuilder.ref(to))).thenReturn(true);

            assertThatThrownBy(() -> validator.validateCopy(copy))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.COPY_BUDGET_TO_ALREADY_EXISTS);
                        assertThat(e.getMessage())
                                .isEqualTo(ErrorCode.COPY_BUDGET_TO_ALREADY_EXISTS.format((short) 2, (short) 2025));
                    });

            verify(repository).getAll(BudgetPredicateBuilder.ref(from));
            verify(repository).exists(BudgetPredicateBuilder.ref(to));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("returns the source budgets when the target month is free")
        void givenValidCopy_shouldReturnSourceBudgets() {
            var fromBudgets = List.of(budgetDto(), budgetDto());
            when(repository.getAll(BudgetPredicateBuilder.ref(from))).thenReturn(fromBudgets);
            when(repository.exists(BudgetPredicateBuilder.ref(to))).thenReturn(false);

            var result = validator.validateCopy(copy);

            assertThat(result).isSameAs(fromBudgets);
            verify(repository).getAll(BudgetPredicateBuilder.ref(from));
            verify(repository).exists(BudgetPredicateBuilder.ref(to));
            verifyNoMoreInteractions(repository);
        }
    }
}
