package com.onurbcd.cli.command;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.dto.budget.CopyBudgetDto;
import com.onurbcd.cli.dto.budget.SumDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.service.BillTypeService;
import com.onurbcd.cli.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestFactory.createBudgetDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> BILL_TYPES = List.of(SelectItem.of("bill type", BILL_TYPE_ID.toString()));

    @Mock
    private BudgetService budgetService;
    @Mock
    private BillTypeService billTypeService;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<BudgetSaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private BudgetCommand command;

    @BeforeEach
    void setUp() {
        command = new BudgetCommand(budgetService, flowBuilder, shellHelper, billTypeService,
                TestValidator.create());
    }

    private void stubFlowResult(ComponentContext<?> context) {
        var result = mock(ComponentFlow.ComponentFlowResult.class);
        doReturn(context).when(result).getContext();
        when(flowBuilder.clone()).thenReturn(flowBuilder);
        when(flowBuilder.reset()).thenReturn(flowBuilder);
        when(flowBuilder.build().run()).thenReturn(result);
    }

    private static ComponentContext<?> completeContext() {
        var context = ComponentContext.empty();
        context.put("name", "  Monthly   Water ");
        context.put("refYear", "2025");
        context.put("refMonth", "7");
        context.put("billTypeId", BILL_TYPE_ID.toString());
        context.put("amount", "250.75");
        context.put("paid", Boolean.TRUE);
        return context;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when there is no bill type, should throw BILL_TYPE_REQUIRED")
        void givenNoBillType_whenSave_shouldThrowBillTypeRequired() {
            when(billTypeService.getItems(null)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(Error.BILL_TYPE_REQUIRED));

            verify(billTypeService).getItems(null);
            verifyNoMoreInteractions(billTypeService);
            verifyNoInteractions(budgetService, shellHelper);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning() {
            when(billTypeService.getItems(null)).thenReturn(BILL_TYPES);
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(billTypeService).getItems(null);
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(budgetService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors() {
            when(billTypeService.getItems(null)).thenReturn(BILL_TYPES);
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Name is required.",
                    "Reference year is mandatory",
                    "Reference month is mandatory",
                    "Bill type is mandatory",
                    "Amount is mandatory",
                    "Paid status is mandatory");
            verify(billTypeService).getItems(null);
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(budgetService);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new budget")
        void givenNoId_whenSave_shouldSaveNewBudget() {
            when(billTypeService.getItems(null)).thenReturn(BILL_TYPES);
            stubFlowResult(completeContext());
            when(budgetService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Budget with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(budgetService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetSaveDto.builder()
                            .name("Monthly Water")
                            .active(true)
                            .refYear((short) 2025)
                            .refMonth((short) 7)
                            .billTypeId(BILL_TYPE_ID)
                            .amount(new BigDecimal("250.75"))
                            .paid(true)
                            .build());
            verify(shellHelper).success("Budget with id %s saved with success.".formatted(RETURNED_ID));
            verify(billTypeService).getItems(null);
            verifyNoMoreInteractions(budgetService, billTypeService, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the budget, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingBudget() {
            when(billTypeService.getItems(null)).thenReturn(BILL_TYPES);
            when(budgetService.getById(ID)).thenReturn(createBudgetDto());
            stubFlowResult(completeContext());
            when(budgetService.save(any(), eq(ID))).thenReturn(ID.toString());
            when(shellHelper.success("Budget with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(budgetService).getById(ID);
            verify(budgetService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetSaveDto.builder()
                            .name("Monthly Water")
                            .active(false)
                            .sequence((short) 4)
                            .refYear((short) 2025)
                            .refMonth((short) 7)
                            .billTypeId(BILL_TYPE_ID)
                            .amount(new BigDecimal("250.75"))
                            .paid(true)
                            .build());
            verify(shellHelper).success("Budget with id %s saved with success.".formatted(ID));
            verify(billTypeService).getItems(null);
            verifyNoMoreInteractions(budgetService, billTypeService, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("when bill types exist, should build the budget flow params")
        void givenBillTypes_whenPreSaveFlow_shouldReturnBudgetParams() {
            when(billTypeService.getItems(null)).thenReturn(BILL_TYPES);

            var result = command.preSaveFlow(CommandParam.of(null));

            assertThat(result.getType()).isEqualTo(FlowType.BUDGET);
            assertThat(result.getBillTypeItems()).isSameAs(BILL_TYPES);
            verify(billTypeService).getItems(null);
            verifyNoMoreInteractions(billTypeService);
            verifyNoInteractions(budgetService, shellHelper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Budget with id %s deleted with success.".formatted(ID))).thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(budgetService).delete(ID);
            verify(shellHelper).success("Budget with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the budget found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createBudgetDto();
            when(budgetService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(budgetService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        static Stream<Arguments> sortProperties() {
            return Stream.of(
                    Arguments.of("null properties use the default", null, new String[]{"sequence"}),
                    Arguments.of("empty properties use the default", new String[0], new String[]{"sequence"}),
                    Arguments.of("blank properties use the default", new String[]{" ", ""}, new String[]{"sequence"}),
                    Arguments.of("given properties replace the default", new String[]{"amount", "name"},
                            new String[]{"amount", "name"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(budgetService.getAll(any(Pageable.class), any(BudgetFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BUDGET)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", (short) 2024, (short) 3,
                    BILL_TYPE_ID, false, properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BudgetFilter.class);
            verify(budgetService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetFilter.of(true, " abc ", (short) 2024, (short) 3, BILL_TYPE_ID, false));
            verify(shellHelper).printTable(page, EruTable.BUDGET);
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and default the reference to today")
        void givenNoOptionalFilters_whenGetAll_shouldPassNullsAndCurrentReference() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(budgetService.getAll(any(Pageable.class), any(BudgetFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BUDGET)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, null,
                    (String[]) null);

            assertThat(result).isEqualTo("table");
            var today = LocalDate.now();
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BudgetFilter.class);
            verify(budgetService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "sequence"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetFilter.of(null, null, (short) today.getYear(), (short) today.getMonthValue(), null, null));
            verify(shellHelper).printTable(page, EruTable.BUDGET);
            verifyNoMoreInteractions(budgetService, shellHelper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should patch active and paid through the service and return the success message")
        void givenIdAndStatuses_whenUpdate_shouldPatchAndReturnMessage() {
            when(shellHelper.success("Budget with id %s updated with success.".formatted(ID))).thenReturn("updated");

            var result = command.update(ID, true, false);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(budgetService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOfSatisfying(BudgetPatchDto.class, dto -> {
                assertThat(dto.isActive()).isTrue();
                assertThat(dto.getPaid()).isFalse();
            });
            verify(shellHelper).success("Budget with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }

        @Test
        @DisplayName("when statuses are absent, should patch them as null")
        void givenNoStatuses_whenUpdate_shouldPatchNulls() {
            when(shellHelper.success("Budget with id %s updated with success.".formatted(ID))).thenReturn("updated");

            var result = command.update(ID, null, null);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(budgetService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOfSatisfying(BudgetPatchDto.class, dto -> {
                assertThat(dto.isActive()).isNull();
                assertThat(dto.getPaid()).isNull();
            });
            verify(shellHelper).success("Budget with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(budgetService, shellHelper);
        }
    }

    @Nested
    @DisplayName("updateSequence")
    class UpdateSequence {

        @ParameterizedTest
        @EnumSource(Direction.class)
        @DisplayName("should move the sequence through the service and return the success message")
        void givenIdAndDirection_whenUpdateSequence_shouldUpdateAndReturnMessage(Direction direction) {
            when(shellHelper.success("Budget with id: '%s' updated with success (sequence).".formatted(ID)))
                    .thenReturn("sequence updated");

            var result = command.updateSequence(ID, direction);

            assertThat(result).isEqualTo("sequence updated");
            verify(budgetService).updateSequence(ID, direction);
            verify(shellHelper).success("Budget with id: '%s' updated with success (sequence).".formatted(ID));
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }

    @Nested
    @DisplayName("swapPosition")
    class SwapPosition {

        @Test
        @DisplayName("should swap the position through the service and return the success message")
        void givenIdAndTarget_whenSwapPosition_shouldSwapAndReturnMessage() {
            when(shellHelper.success("Budget with id: '%s' updated with success (position).".formatted(ID)))
                    .thenReturn("position updated");

            var result = command.swapPosition(ID, (short) 7);

            assertThat(result).isEqualTo("position updated");
            verify(budgetService).swapPosition(ID, (short) 7);
            verify(shellHelper).success("Budget with id: '%s' updated with success (position).".formatted(ID));
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }

    @Nested
    @DisplayName("getSumByMonth")
    class GetSumByMonth {

        @Test
        @DisplayName("should filter the sums through the service and print them as a table")
        void givenFilters_whenGetSumByMonth_shouldPrintTable() {
            List<SumDto> sums = List.of();
            when(budgetService.getSumByMonth(any(BudgetFilter.class))).thenReturn(sums);
            when(shellHelper.printTable(sums, EruTable.BUDGET_SUM_MONTH)).thenReturn("sums");

            var result = command.getSumByMonth(true, " abc ", (short) 2024, (short) 3, BILL_TYPE_ID, false);

            assertThat(result).isEqualTo("sums");
            var filterCaptor = ArgumentCaptor.forClass(BudgetFilter.class);
            verify(budgetService).getSumByMonth(filterCaptor.capture());
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetFilter.of(true, " abc ", (short) 2024, (short) 3, BILL_TYPE_ID, false));
            verify(shellHelper).printTable(sums, EruTable.BUDGET_SUM_MONTH);
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }

        @Test
        @DisplayName("when filters are absent, should default the reference to today")
        void givenNoFilters_whenGetSumByMonth_shouldUseCurrentReference() {
            List<SumDto> sums = List.of();
            when(budgetService.getSumByMonth(any(BudgetFilter.class))).thenReturn(sums);
            when(shellHelper.printTable(sums, EruTable.BUDGET_SUM_MONTH)).thenReturn("sums");

            var result = command.getSumByMonth(null, null, null, null, null, null);

            assertThat(result).isEqualTo("sums");
            var today = LocalDate.now();
            var filterCaptor = ArgumentCaptor.forClass(BudgetFilter.class);
            verify(budgetService).getSumByMonth(filterCaptor.capture());
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BudgetFilter.of(null, null, (short) today.getYear(), (short) today.getMonthValue(), null, null));
            verify(shellHelper).printTable(sums, EruTable.BUDGET_SUM_MONTH);
            verifyNoMoreInteractions(budgetService, shellHelper);
        }
    }

    @Nested
    @DisplayName("copy")
    class Copy {

        @Test
        @DisplayName("should copy through the service and return the zero-padded success message")
        void givenReferences_whenCopy_shouldCopyAndReturnMessage() {
            when(shellHelper.success("Budget copied from 2024/03 to 2025/12.")).thenReturn("copied");

            var result = command.copy((short) 2024, (short) 3, (short) 2025, (short) 12);

            assertThat(result).isEqualTo("copied");
            var dtoCaptor = ArgumentCaptor.forClass(CopyBudgetDto.class);
            verify(budgetService).copy(dtoCaptor.capture());
            assertThat(dtoCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(CopyBudgetDto.of((short) 2024, (short) 3, (short) 2025, (short) 12));
            verify(shellHelper).success("Budget copied from 2024/03 to 2025/12.");
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }

    @Nested
    @DisplayName("deleteAll")
    class DeleteAll {

        @Test
        @DisplayName("should delete the month through the service and return the zero-padded success message")
        void givenReference_whenDeleteAll_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("All budgets for 2024/03 deleted.")).thenReturn("deleted all");

            var result = command.deleteAll((short) 2024, (short) 3);

            assertThat(result).isEqualTo("deleted all");
            verify(budgetService).deleteAll((short) 2024, (short) 3);
            verify(shellHelper).success("All budgets for 2024/03 deleted.");
            verifyNoMoreInteractions(budgetService, shellHelper);
            verifyNoInteractions(billTypeService);
        }
    }
}
