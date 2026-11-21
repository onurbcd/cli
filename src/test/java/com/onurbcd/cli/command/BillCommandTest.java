package com.onurbcd.cli.command;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.dto.bill.BillSaveDto;
import com.onurbcd.cli.enums.*;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.service.BillService;
import com.onurbcd.cli.service.BudgetService;
import com.onurbcd.cli.service.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static com.onurbcd.cli.TestFactory.createBillDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillCommandTest {

    private static final UUID BUDGET_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID BILL_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> BUDGETS = List.of(SelectItem.of("budget", BUDGET_ID.toString()));
    private static final List<SelectItem> SOURCES = List.of(SelectItem.of("source", SOURCE_ID.toString()));
    private static final List<SelectItem> BILLS = List.of(SelectItem.of("bill", BILL_ID.toString()));

    @Mock
    private BillService billService;
    @Mock
    private BudgetService budgetService;
    @Mock
    private SourceService sourceService;
    @Mock
    private AdminProperties adminProperties;
    @Mock
    private Clock clock;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private BillCommand command;

    @BeforeEach
    void setUp() {
        command = new BillCommand(billService, flowBuilder, shellHelper, budgetService, sourceService,
                adminProperties, TestValidator.create(), clock);
    }

    private void stubFlowBuilder() {
        when(flowBuilder.clone()).thenReturn(flowBuilder);
        when(flowBuilder.reset()).thenReturn(flowBuilder);
    }

    private void stubFlowResult(ComponentContext<?> context) {
        var result = mock(ComponentFlow.ComponentFlowResult.class);
        doReturn(context).when(result).getContext();
        stubFlowBuilder();
        when(flowBuilder.build().run()).thenReturn(result);
    }

    private void stubFlowInterrupted() {
        stubFlowBuilder();
        when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
    }

    private static ComponentContext<?> openContext() {
        var context = ComponentContext.empty();
        context.put("referenceDay", "2024-03-15");
        context.put("documentDate", "2024-03-10");
        context.put("dueDate", "2024-03-20");
        context.put("observation", "  some   note ");
        context.put("installment", "2");
        context.put("documentType", "BILLET");
        context.put("budget", BUDGET_ID.toString());
        context.put("referenceType", "MONTH");
        return context;
    }

    private static ComponentContext<?> closeContext() {
        var context = ComponentContext.empty();
        context.put("billId", BILL_ID.toString());
        context.put("paymentDate", "2024-03-25");
        context.put("paymentType", "PIX");
        context.put("sourceId", SOURCE_ID.toString());
        return context;
    }

    private static ComponentContext<?> saveContext() {
        var context = openContext();
        context.put("paymentDate", "2024-03-25");
        context.put("paymentType", "CASH");
        context.put("sourceId", SOURCE_ID.toString());
        return context;
    }

    private static final LocalDate TODAY = TestClock.DEFAULT_DATE;

    private void stubClockAt(LocalDate first, LocalDate... next) {
        var instants = Stream.of(next).map(date -> date.atStartOfDay(ZoneOffset.UTC).toInstant())
                .toArray(Instant[]::new);

        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(first.atStartOfDay(ZoneOffset.UTC).toInstant(), instants);
    }

    private void verifyClockReadOnce() {
        verify(clock).instant();
        verify(clock).getZone();
        verifyNoMoreInteractions(clock);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when there is no budget, should throw BUDGET_REQUIRED for the bill's reference month")
        void givenNoBudget_whenSave_shouldThrowBudgetRequired() {
            when(billService.getById(ID)).thenReturn(createBillDto());
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.BUDGET_REQUIRED);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.BUDGET_REQUIRED.format((short) 3, (short) 2024));
                    });

            verify(billService).getById(ID);
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verifyNoMoreInteractions(billService, budgetService);
            verifyNoInteractions(sourceService, shellHelper, adminProperties);
        }

        @Test
        @DisplayName("when there is no source, should throw SOURCE_REQUIRED")
        void givenNoSource_whenSave_shouldThrowSourceRequired() {
            when(billService.getById(ID)).thenReturn(createBillDto());
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(sourceService.getItems(null)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.SOURCE_REQUIRED));

            verify(billService).getById(ID);
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verifyNoMoreInteractions(billService, budgetService, sourceService);
            verifyNoInteractions(shellHelper, adminProperties);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning(@TempDir Path filesPath) {
            when(billService.getById(ID)).thenReturn(createBillDto());
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowInterrupted();
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(ID);

            assertThat(result).isEqualTo("cancelled");
            verify(billService).getById(ID);
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(billService, budgetService, sourceService, adminProperties, shellHelper);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors(@TempDir Path filesPath) {
            when(billService.getById(ID)).thenReturn(createBillDto());
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(ID);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Reference day is required.",
                    "Due date is required.",
                    "Document type is required.",
                    "Budget is required.",
                    "Reference type is required.",
                    "Payment type is required.",
                    "Source id is required.");
            verify(billService).getById(ID);
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(billService, budgetService, sourceService, adminProperties, shellHelper);
        }

        @Test
        @DisplayName("when the flow result is valid, should update the bill and return the success message")
        void givenValidFlowResult_whenSave_shouldUpdateBill(@TempDir Path filesPath) {
            when(billService.getById(ID)).thenReturn(createBillDto());
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(saveContext());
            when(billService.save(any(), eq(ID))).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Bill with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            var captor = ArgumentCaptor.forClass(com.onurbcd.cli.dto.PrimeSaveDto.class);
            verify(billService).save(captor.capture(), eq(ID));
            assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(
                    BillSaveDto.builder()
                            .name("bogus")
                            .active(false)
                            .referenceDayCalendarDate(LocalDate.of(2024, 3, 15))
                            .documentDateCalendarDate(LocalDate.of(2024, 3, 10))
                            .dueDateCalendarDate(LocalDate.of(2024, 3, 20))
                            .paymentDateCalendarDate(LocalDate.of(2024, 3, 25))
                            .observation("some note")
                            .installment((short) 2)
                            .documentType(DocumentType.BILLET)
                            .budgetId(BUDGET_ID)
                            .referenceType(ReferenceType.MONTH)
                            .closed(false)
                            .paymentType(PaymentType.CASH)
                            .sourceId(SOURCE_ID)
                            .build());
            verify(billService).getById(ID);
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).success("Bill with id %s saved with success.".formatted(RETURNED_ID));
            verifyNoMoreInteractions(billService, budgetService, sourceService, adminProperties, shellHelper);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the bill found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createBillDto();
            when(billService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(billService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(billService, shellHelper);
            verifyNoInteractions(budgetService, sourceService, adminProperties);
        }
    }

    @Nested
    @DisplayName("openBill")
    class OpenBill {

        @Test
        @DisplayName("when there is no budget for the given month, should throw BUDGET_REQUIRED")
        void givenNoBudget_whenOpenBill_shouldThrowBudgetRequired() {
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(List.of());

            assertThatThrownBy(() -> command.openBill((short) 2024, (short) 3))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.BUDGET_REQUIRED);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.BUDGET_REQUIRED.format((short) 3, (short) 2024));
                    });

            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verifyNoMoreInteractions(budgetService);
            verifyNoInteractions(billService, sourceService, shellHelper, adminProperties);
        }

        @Test
        @DisplayName("when year and month are absent, should look up the budget of the current month")
        void givenNoYearAndMonth_whenOpenBill_shouldUseCurrentMonth() {
            when(budgetService.getMonthlyBudget((short) 2025, (short) 7)).thenReturn(List.of());

            stubClockAt(TODAY);

            assertThatThrownBy(() -> command.openBill(null, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.BUDGET_REQUIRED));

            verify(budgetService).getMonthlyBudget((short) 2025, (short) 7);
            verifyNoMoreInteractions(budgetService);
            verifyClockReadOnce();
        }

        @Test
        @DisplayName("when the clock crosses New Year between year and month lookup, should keep a consistent month")
        void givenClockCrossingNewYear_whenOpenBill_shouldUseConsistentYearAndMonth() {
            when(budgetService.getMonthlyBudget(any(), any())).thenReturn(List.of());

            stubClockAt(LocalDate.of(2024, 12, 31), LocalDate.of(2025, 1, 1));

            assertThatThrownBy(() -> command.openBill(null, null)).isInstanceOf(ApiException.class);

            verify(budgetService).getMonthlyBudget((short) 2024, (short) 12);
            verifyClockReadOnce();
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenOpenBill_shouldReturnCancelledWarning(@TempDir Path filesPath) {
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowInterrupted();
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.openBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("cancelled");
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(budgetService, adminProperties, shellHelper);
            verifyNoInteractions(billService, sourceService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenOpenBill_shouldReturnValidationErrors(@TempDir Path filesPath) {
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.openBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Reference day is required.",
                    "Due date is required.",
                    "Document type is required.",
                    "Budget is required.",
                    "Reference type is required.");
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(budgetService, adminProperties, shellHelper);
            verifyNoInteractions(billService, sourceService);
        }

        @Test
        @DisplayName("when the flow result is valid, should create a new bill and return the success message")
        void givenValidFlowResult_whenOpenBill_shouldSaveNewBill(@TempDir Path filesPath) {
            when(budgetService.getMonthlyBudget((short) 2024, (short) 3)).thenReturn(BUDGETS);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(openContext());
            when(billService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Bill with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.openBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("saved");
            var captor = ArgumentCaptor.forClass(com.onurbcd.cli.dto.PrimeSaveDto.class);
            verify(billService).save(captor.capture(), isNull());
            assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(
                    BillOpenDto.builder()
                            .name("bogus")
                            .active(true)
                            .referenceDayCalendarDate(LocalDate.of(2024, 3, 15))
                            .documentDateCalendarDate(LocalDate.of(2024, 3, 10))
                            .dueDateCalendarDate(LocalDate.of(2024, 3, 20))
                            .observation("some note")
                            .installment((short) 2)
                            .documentType(DocumentType.BILLET)
                            .budgetId(BUDGET_ID)
                            .referenceType(ReferenceType.MONTH)
                            .build());
            verify(budgetService).getMonthlyBudget((short) 2024, (short) 3);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).success("Bill with id %s saved with success.".formatted(RETURNED_ID));
            verifyNoMoreInteractions(billService, budgetService, adminProperties, shellHelper);
            verifyNoInteractions(sourceService);
        }
    }

    @Nested
    @DisplayName("closeBill")
    class CloseBill {

        @Test
        @DisplayName("when there are no open bills for the given month, should throw OPEN_BILLS_REQUIRED")
        void givenNoOpenBills_whenCloseBill_shouldThrowOpenBillsRequired() {
            when(billService.getOpenBills((short) 2024, (short) 3)).thenReturn(List.of());

            assertThatThrownBy(() -> command.closeBill((short) 2024, (short) 3))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.OPEN_BILLS_REQUIRED);
                        assertThat(e.getMessage())
                                .isEqualTo(ErrorCode.OPEN_BILLS_REQUIRED.format((short) 3, (short) 2024));
                    });

            verify(billService).getOpenBills((short) 2024, (short) 3);
            verifyNoMoreInteractions(billService);
            verifyNoInteractions(budgetService, sourceService, shellHelper, adminProperties);
        }

        @Test
        @DisplayName("when year and month are absent, should look up the open bills of the current month")
        void givenNoYearAndMonth_whenCloseBill_shouldUseCurrentMonth() {
            when(billService.getOpenBills((short) 2025, (short) 7)).thenReturn(List.of());

            stubClockAt(TODAY);

            assertThatThrownBy(() -> command.closeBill(null, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.OPEN_BILLS_REQUIRED));

            verify(billService).getOpenBills((short) 2025, (short) 7);
            verifyNoMoreInteractions(billService);
            verifyClockReadOnce();
        }

        @Test
        @DisplayName("when the clock crosses New Year between year and month lookup, should keep a consistent month")
        void givenClockCrossingNewYear_whenCloseBill_shouldUseConsistentYearAndMonth() {
            when(billService.getOpenBills(any(), any())).thenReturn(List.of());

            stubClockAt(LocalDate.of(2024, 12, 31), LocalDate.of(2025, 1, 1));

            assertThatThrownBy(() -> command.closeBill(null, null)).isInstanceOf(ApiException.class);

            verify(billService).getOpenBills((short) 2024, (short) 12);
            verifyClockReadOnce();
        }

        @Test
        @DisplayName("when there is no source, should throw SOURCE_REQUIRED")
        void givenNoSource_whenCloseBill_shouldThrowSourceRequired() {
            when(billService.getOpenBills((short) 2024, (short) 3)).thenReturn(BILLS);
            when(sourceService.getItems(null)).thenReturn(List.of());

            assertThatThrownBy(() -> command.closeBill((short) 2024, (short) 3))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.SOURCE_REQUIRED));

            verify(billService).getOpenBills((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verifyNoMoreInteractions(billService, sourceService);
            verifyNoInteractions(budgetService, shellHelper, adminProperties);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenCloseBill_shouldReturnCancelledWarning(@TempDir Path filesPath) {
            when(billService.getOpenBills((short) 2024, (short) 3)).thenReturn(BILLS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowInterrupted();
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.closeBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("cancelled");
            verify(billService).getOpenBills((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(billService, sourceService, adminProperties, shellHelper);
            verifyNoInteractions(budgetService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenCloseBill_shouldReturnValidationErrors(@TempDir Path filesPath) {
            when(billService.getOpenBills((short) 2024, (short) 3)).thenReturn(BILLS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.closeBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Bill id is required.",
                    "Payment date is required.",
                    "Payment type is required.",
                    "Source id is required.");
            verify(billService).getOpenBills((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(billService, sourceService, adminProperties, shellHelper);
            verifyNoInteractions(budgetService);
        }

        @Test
        @DisplayName("when the flow result is valid, should close the bill and return the success message")
        void givenValidFlowResult_whenCloseBill_shouldSaveClosedBill(@TempDir Path filesPath) {
            when(billService.getOpenBills((short) 2024, (short) 3)).thenReturn(BILLS);
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
            stubFlowResult(closeContext());
            when(billService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Bill with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.closeBill((short) 2024, (short) 3);

            assertThat(result).isEqualTo("saved");
            var captor = ArgumentCaptor.forClass(com.onurbcd.cli.dto.PrimeSaveDto.class);
            verify(billService).save(captor.capture(), isNull());
            assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(
                    BillCloseDto.builder()
                            .name("bogus")
                            .billId(BILL_ID)
                            .paymentDateCalendarDate(LocalDate.of(2024, 3, 25))
                            .paymentType(PaymentType.PIX)
                            .sourceId(SOURCE_ID)
                            .build());
            verify(billService).getOpenBills((short) 2024, (short) 3);
            verify(sourceService).getItems(null);
            verify(adminProperties).getFilesPath();
            verify(shellHelper).success("Bill with id %s saved with success.".formatted(RETURNED_ID));
            verifyNoMoreInteractions(billService, sourceService, adminProperties, shellHelper);
            verifyNoInteractions(budgetService);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @ParameterizedTest(name = "[{index}] {0}")
        @EnumSource(value = FlowType.class, mode = EnumSource.Mode.EXCLUDE,
                names = {"BILL", "BILL_OPEN", "BILL_CLOSE"})
        @DisplayName("when the flow type is not a bill flow, should throw IllegalStateException")
        void givenUnsupportedFlowType_whenPreSaveFlow_shouldThrow(FlowType flowType) {
            var params = CommandParam.of(flowType, ID);

            assertThatThrownBy(() -> command.preSaveFlow(params))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Unexpected value: " + flowType);

            verifyNoInteractions(billService, budgetService, sourceService, adminProperties, shellHelper);
        }
    }
}
