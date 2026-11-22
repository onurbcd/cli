package com.onurbcd.cli.service;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.dto.budget.BudgetSumDto;
import com.onurbcd.cli.dto.budget.BudgetValuesDto;
import com.onurbcd.cli.dto.budget.CopyBudgetDto;
import com.onurbcd.cli.dto.budget.MonthlyBudgetDto;
import com.onurbcd.cli.dto.budget.SumDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BudgetToEntityMapper;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.predicate.BudgetPredicateBuilder;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import com.onurbcd.cli.validator.BudgetValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final short YEAR = 2024;
    private static final short MONTH = 3;
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 7, 10, 0, 0);

    @Mock
    private BudgetRepository repository;
    @Mock
    private BudgetToEntityMapper toEntityMapper;
    @Mock
    private BudgetValidator validator;
    @Mock
    private SequenceService sequenceService;
    @Mock
    private SourceService sourceService;
    @Captor
    private ArgumentCaptor<SequenceParam> paramCaptor;
    @Captor
    private ArgumentCaptor<List<Budget>> budgetsCaptor;

    private BudgetService service;

    @BeforeEach
    void setUp() {
        service = new BudgetService(repository, toEntityMapper, validator, sequenceService, sourceService,
                TestClock.DEFAULT);
    }

    private static Budget budget(Short sequence) {
        return Budget.builder().id(ID).name("name").active(true).refYear((short) YEAR).refMonth((short) MONTH)
                .sequence(sequence).build();
    }

    private static void assertErrorCode(Throwable thrown, ErrorCode code) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(code));
    }

    private static void assertSequenceParam(SequenceParam param, Short sequence, Short target) {
        assertThat(param.getYear()).isEqualTo(YEAR);
        assertThat(param.getMonth()).isEqualTo(MONTH);
        assertThat(param.getSequence()).isEqualTo(sequence);
        assertThat(param.getTargetSequence()).isEqualTo(target);
    }

    @Nested
    @DisplayName("getSumByMonth")
    class GetSumByMonth {

        private BudgetFilter filter() {
            return BudgetFilter.of(null, null, (short) YEAR, (short) MONTH, null, null, TestClock.DEFAULT);
        }

        private void assertSums(List<SumDto> result, String total, String paid, String unpaid, String balance) {
            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    SumDto.total(new BigDecimal(total)), SumDto.paid(new BigDecimal(paid)),
                    SumDto.unpaid(new BigDecimal(unpaid)), SumDto.balance(new BigDecimal(balance))));
        }

        @Test
        @DisplayName("without reference year, should fail and not query")
        void givenNoYear_shouldThrow() {
            var filter = filter();
            filter.setRefYear(null);

            assertThatThrownBy(() -> service.getSumByMonth(filter))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.BUDGET_REF_YEAR_IS_NULL));

            verifyNoInteractions(repository, sourceService);
        }

        @Test
        @DisplayName("without reference month, should fail and not query")
        void givenNoMonth_shouldThrow() {
            var filter = filter();
            filter.setRefMonth(null);

            assertThatThrownBy(() -> service.getSumByMonth(filter))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.BUDGET_REF_MONTH_IS_NULL));

            verifyNoInteractions(repository, sourceService);
        }

        @Test
        @DisplayName("with paid and unpaid budgets, should total both and subtract the unpaid from the usable balance")
        void givenPaidAndUnpaid_shouldSumAll() {
            var sums = Set.of(new BudgetSumDto(new BigDecimal("100.00"), true, 2L),
                    new BudgetSumDto(new BigDecimal("40.00"), false, 1L));
            when(repository.getSumByMonth((short) YEAR, (short) MONTH)).thenReturn(sums);
            when(sourceService.getUsableBalanceSum()).thenReturn(new BigDecimal("500.00"));

            var result = service.getSumByMonth(filter());

            assertSums(result, "140.00", "100.00", "40.00", "460.00");
            verify(repository).getSumByMonth((short) YEAR, (short) MONTH);
            verify(sourceService).getUsableBalanceSum();
            verifyNoMoreInteractions(repository, sourceService);
        }

        @Test
        @DisplayName("with only paid budgets, should report zero unpaid and keep the usable balance")
        void givenOnlyPaid_shouldReportZeroUnpaid() {
            var sums = Set.of(new BudgetSumDto(new BigDecimal("100.00"), true, 2L));
            when(repository.getSumByMonth((short) YEAR, (short) MONTH)).thenReturn(sums);
            when(sourceService.getUsableBalanceSum()).thenReturn(new BigDecimal("500.00"));

            var result = service.getSumByMonth(filter());

            assertSums(result, "100.00", "100.00", "0", "500.00");
        }

        @Test
        @DisplayName("with only unpaid budgets, should report zero paid")
        void givenOnlyUnpaid_shouldReportZeroPaid() {
            var sums = Set.of(new BudgetSumDto(new BigDecimal("40.00"), false, 1L));
            when(repository.getSumByMonth((short) YEAR, (short) MONTH)).thenReturn(sums);
            when(sourceService.getUsableBalanceSum()).thenReturn(new BigDecimal("500.00"));

            var result = service.getSumByMonth(filter());

            assertSums(result, "40.00", "0", "40.00", "460.00");
        }

        @Test
        @DisplayName("without budgets, should report zeros and the usable balance")
        void givenNoBudgets_shouldReportZeros() {
            when(repository.getSumByMonth((short) YEAR, (short) MONTH)).thenReturn(Set.of());
            when(sourceService.getUsableBalanceSum()).thenReturn(new BigDecimal("500.00"));

            var result = service.getSumByMonth(filter());

            assertSums(result, "0", "0", "0", "500.00");
        }
    }

    @Nested
    @DisplayName("copy")
    class Copy {

        @Test
        @DisplayName("should copy every validated budget into the target month and save them together")
        void givenBudgets_shouldCopyAndSaveAll() {
            var copyDto = CopyBudgetDto.of((short) 2024, (short) 2, (short) 2024, (short) 3);
            var first = new BudgetDto();
            first.setId(ID);
            var second = new BudgetDto();
            second.setId(PARENT_ID);
            var copiedFirst = budget((short) 1);
            var copiedSecond = budget((short) 2);
            when(validator.validateCopy(copyDto)).thenReturn(List.of(first, second));
            when(toEntityMapper.copy(first, (short) 2024, (short) 3)).thenReturn(copiedFirst);
            when(toEntityMapper.copy(second, (short) 2024, (short) 3)).thenReturn(copiedSecond);

            service.copy(copyDto);

            verify(validator).validateCopy(copyDto);
            verify(toEntityMapper).copy(first, (short) 2024, (short) 3);
            verify(toEntityMapper).copy(second, (short) 2024, (short) 3);
            verify(repository).saveAll(budgetsCaptor.capture());
            assertThat(budgetsCaptor.getValue()).containsExactly(copiedFirst, copiedSecond);
            verifyNoMoreInteractions(validator, toEntityMapper, repository);
        }

        @Test
        @DisplayName("when validation fails, should propagate and save nothing")
        void givenValidationFailure_shouldNotSave() {
            var copyDto = CopyBudgetDto.of((short) 2024, (short) 3, (short) 2024, (short) 3);
            var failure = new ApiException(ErrorCode.COPY_BUDGET_EQUAL_MONTH);
            when(validator.validateCopy(copyDto)).thenThrow(failure);

            assertThatThrownBy(() -> service.copy(copyDto)).isSameAs(failure);

            verify(validator).validateCopy(copyDto);
            verifyNoMoreInteractions(validator);
            verifyNoInteractions(repository, toEntityMapper);
        }
    }

    @Nested
    @DisplayName("deleteAll")
    class DeleteAll {

        @ParameterizedTest(name = "[{index}] {0} rows deleted")
        @ValueSource(ints = {1, 7})
        @DisplayName("when rows are deleted, should not throw")
        void givenDeletedRows_shouldNotThrow(int rows) {
            when(repository.deleteAll((short) YEAR, (short) MONTH)).thenReturn(rows);

            service.deleteAll((short) YEAR, (short) MONTH);

            verify(repository).deleteAll((short) YEAR, (short) MONTH);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when no rows are deleted, should fail with no rows deleted")
        void givenNoRows_shouldThrow() {
            when(repository.deleteAll((short) YEAR, (short) MONTH)).thenReturn(0);

            assertThatThrownBy(() -> service.deleteAll((short) YEAR, (short) MONTH))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.NO_ROWS_DELETED));

            verify(repository).deleteAll((short) YEAR, (short) MONTH);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getBudgetValues")
    class GetBudgetValues {

        @Test
        @DisplayName("when found, should return the values")
        void givenFound_shouldReturn() {
            var values = new BudgetValuesDto(new BigDecimal("10"), BILL_TYPE_ID, "bills/water", false);
            when(repository.getBudgetValues(ID)).thenReturn(Optional.of(values));

            assertThat(service.getBudgetValues(ID)).isSameAs(values);

            verify(repository).getBudgetValues(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("when missing, should fail with not found")
        void givenMissing_shouldThrowNotFound() {
            when(repository.getBudgetValues(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getBudgetValues(ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).getBudgetValues(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getMonthlyBudget")
    class GetMonthlyBudget {

        @Test
        @DisplayName("should map each budget to a select item named '<sequence> - <name>' valued by its id")
        void shouldMapToSelectItems() {
            when(repository.getMonthlyBudget((short) YEAR, (short) MONTH)).thenReturn(List.of(
                    new MonthlyBudgetDto(ID, "Water", (short) 1), new MonthlyBudgetDto(PARENT_ID, "Rent", (short) 2)));

            var result = service.getMonthlyBudget((short) YEAR, (short) MONTH);

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    SelectItem.of("1 - Water", ID.toString()), SelectItem.of("2 - Rent", PARENT_ID.toString())));
            verify(repository).getMonthlyBudget((short) YEAR, (short) MONTH);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should delegate to the budget validator with the typed dto and entity")
        void shouldDelegateToValidator() {
            var saveDto = BudgetSaveDto.builder().name("name").build();
            var current = budget((short) 1);

            service.validate(saveDto, current, ID);

            verify(validator).validate(saveDto, current, ID);
            verifyNoMoreInteractions(validator);
            verifyNoInteractions(repository, sequenceService);
        }
    }

    @Nested
    @DisplayName("fillValues")
    class FillValues {

        private final BudgetSaveDto saveDto = BudgetSaveDto.builder().name("name").build();

        @Test
        @DisplayName("for a new budget, should take the next sequence of its month")
        void givenNoCurrent_shouldUseNextSequence() {
            var mapped = budget(null);
            when(toEntityMapper.apply(saveDto)).thenReturn(mapped);
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 6);

            var result = service.fillValues(saveDto, null);

            assertThat(result).isSameAs(mapped);
            assertThat(mapped.getSequence()).isEqualTo((short) 6);
            verify(sequenceService).getNextSequence(paramCaptor.capture());
            assertSequenceParam(paramCaptor.getValue(), null, null);
            verify(toEntityMapper).apply(saveDto);
            verifyNoMoreInteractions(sequenceService, toEntityMapper);
        }

        @Test
        @DisplayName("for an existing budget with a sequence, should keep it without asking for a new one")
        void givenCurrentWithSequence_shouldKeepSequence() {
            var mapped = budget(null);
            when(toEntityMapper.apply(saveDto)).thenReturn(mapped);

            var result = service.fillValues(saveDto, budget((short) 4));

            assertThat(result).isSameAs(mapped);
            assertThat(mapped.getSequence()).isEqualTo((short) 4);
            verify(toEntityMapper).apply(saveDto);
            verifyNoMoreInteractions(toEntityMapper);
            verifyNoInteractions(sequenceService);
        }

        @Test
        @DisplayName("for an existing budget without a sequence, should take the next sequence")
        void givenCurrentWithoutSequence_shouldUseNextSequence() {
            var mapped = budget(null);
            when(toEntityMapper.apply(saveDto)).thenReturn(mapped);
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 9);

            service.fillValues(saveDto, budget(null));

            assertThat(mapped.getSequence()).isEqualTo((short) 9);
            verify(sequenceService).getNextSequence(any(SequenceParam.class));
            verify(toEntityMapper).apply(saveDto);
            verifyNoMoreInteractions(sequenceService, toEntityMapper);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should query with the budget predicate through the custom query")
        void shouldUseBudgetPredicate() {
            var filter = BudgetFilter.of(true, "abc", (short) YEAR, (short) MONTH, BILL_TYPE_ID, false,
                    TestClock.DEFAULT);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(new BudgetDto()), pageable, 1);
            when(repository.getAll(BudgetPredicateBuilder.all(filter), pageable)).thenReturn(page);

            var result = service.getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(BudgetPredicateBuilder.all(filter), pageable);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("with active and paid, should update both with the current time")
        void givenActiveAndPaid_shouldUpdateBoth() {
            when(repository.updateActiveAndPaid(ID, true, false, NOW)).thenReturn(1);

            service.update(BudgetPatchDto.of(true, false), ID);

            verify(repository).updateActiveAndPaid(ID, true, false, NOW);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("with only active, should update only the active flag")
        void givenOnlyActive_shouldUpdateActive() {
            when(repository.updateActive(ID, false)).thenReturn(1);

            service.update(BudgetPatchDto.builder().active(false).build(), ID);

            verify(repository).updateActive(ID, false);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("with only paid, should update the paid flag with the current time")
        void givenOnlyPaid_shouldUpdatePaid() {
            when(repository.updatePaid(ID, true, NOW)).thenReturn(1);

            service.update(BudgetPatchDto.of(true), ID);

            verify(repository).updatePaid(ID, true, NOW);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("with nothing to update, should fail with not found without touching the repository")
        void givenNothing_shouldThrowNotFound() {
            var patch = BudgetPatchDto.builder().build();

            assertThatThrownBy(() -> service.update(patch, ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verifyNoInteractions(repository);
        }

        @ParameterizedTest(name = "[{index}] {0} rows")
        @ValueSource(ints = {0, 2})
        @DisplayName("when the updated row count is not one, should fail with not found (every kind of patch)")
        void givenWrongRowCount_shouldThrowNotFound(int rows) {
            when(repository.updateActiveAndPaid(ID, true, true, NOW)).thenReturn(rows);
            when(repository.updateActive(ID, true)).thenReturn(rows);
            when(repository.updatePaid(ID, true, NOW)).thenReturn(rows);
            var activeAndPaid = BudgetPatchDto.of(true, true);
            var onlyActive = BudgetPatchDto.builder().active(true).build();
            var onlyPaid = BudgetPatchDto.of(true);

            for (var patch : List.of(activeAndPaid, onlyActive, onlyPaid)) {
                assertThatThrownBy(() -> service.update(patch, ID))
                        .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));
            }
        }
    }

    @Nested
    @DisplayName("updateSequence")
    class UpdateSequence {

        @ParameterizedTest(name = "[{index}] {0}")
        @EnumSource(Direction.class)
        @DisplayName("should swap the sequence of the budget's month in the given direction")
        void givenBudget_shouldSwapSequence(Direction direction) {
            when(repository.findById(ID)).thenReturn(Optional.of(budget((short) 3)));

            service.updateSequence(ID, direction);

            verify(repository).findById(ID);
            verify(sequenceService).swapSequence(paramCaptor.capture(), eq(direction));
            assertSequenceParam(paramCaptor.getValue(), (short) 3, null);
            verifyNoMoreInteractions(repository, sequenceService);
        }

        @Test
        @DisplayName("when the budget does not exist, should fail with not found and not swap")
        void givenUnknownBudget_shouldThrowNotFound() {
            when(repository.findById(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateSequence(ID, Direction.UP))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete the budget and then close the gap in the following sequences")
        void givenBudget_shouldDeleteThenUpdateNextSequences() {
            when(repository.findById(ID)).thenReturn(Optional.of(budget((short) 3)));

            service.delete(ID);

            InOrder order = inOrder(repository, sequenceService);
            order.verify(repository).findById(ID);
            order.verify(repository).deleteById(ID);
            order.verify(sequenceService).updateNextSequences(paramCaptor.capture());
            assertSequenceParam(paramCaptor.getValue(), (short) 3, null);
            verifyNoMoreInteractions(repository, sequenceService);
        }

        @Test
        @DisplayName("when the budget does not exist, should fail with not found and delete nothing")
        void givenUnknownBudget_shouldThrowNotFound() {
            when(repository.findById(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService);
        }
    }

    @Nested
    @DisplayName("swapPosition")
    class SwapPosition {

        @Test
        @DisplayName("should swap the budget to the target sequence")
        void givenBudget_shouldSwapPosition() {
            when(repository.findById(ID)).thenReturn(Optional.of(budget((short) 3)));

            service.swapPosition(ID, (short) 7);

            verify(repository).findById(ID);
            verify(sequenceService).swapPosition(paramCaptor.capture());
            assertSequenceParam(paramCaptor.getValue(), (short) 3, (short) 7);
            verifyNoMoreInteractions(repository, sequenceService);
        }

        @Test
        @DisplayName("when the budget does not exist, should fail with not found and not swap")
        void givenUnknownBudget_shouldThrowNotFound() {
            when(repository.findById(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.swapPosition(ID, (short) 7))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).findById(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService);
        }
    }
}
