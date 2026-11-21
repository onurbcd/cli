package com.onurbcd.cli.command;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.balance.BalancePatchDto;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.balance.BalanceSumDto;
import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.enums.*;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.service.BalanceService;
import com.onurbcd.cli.service.CategoryService;
import com.onurbcd.cli.service.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.*;
import static com.onurbcd.cli.TestFactory.createBalanceDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> SOURCES = List.of(SelectItem.of("source", SOURCE_ID.toString()));
    private static final List<SelectItem> CATEGORIES = List.of(SelectItem.of("category", CATEGORY_ID.toString()));

    @Mock
    private BalanceService balanceService;
    @Mock
    private SourceService sourceService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AdminProperties adminProperties;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<BalanceSaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private BalanceCommand command;

    @BeforeEach
    void setUp() {
        command = new BalanceCommand(balanceService, sourceService, categoryService, flowBuilder, shellHelper,
                adminProperties);
    }

    private void stubPreSaveFlow(Path filesPath) {
        when(sourceService.getItems(null)).thenReturn(SOURCES);
        when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);
        when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
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
        context.put("day", "2024-03-15");
        context.put("source", SOURCE_ID.toString());
        context.put("category", CATEGORY_ID.toString());
        context.put("amount", "12.50");
        context.put("code", "  CODE   X ");
        context.put("description", "description");
        context.put("balanceType", "INCOME");
        context.put("paymentType", "CASH");
        return context;
    }

    private static BalanceFilter.BalanceFilterBuilder<?, ?> filterBuilder() {
        return BalanceFilter.builder()
                .active(true)
                .search("abc")
                .sourceId(SOURCE_ID)
                .categoryId(CATEGORY_ID)
                .balanceType(BalanceType.INCOME)
                .paymentType(PaymentType.PIX)
                .dayCalendarYear((short) 2024)
                .dayCalendarMonth((short) 3)
                .dayCalendarDayInMonth((short) 15);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when there is no source, should throw SOURCE_REQUIRED before looking for categories")
        void givenNoSource_whenSave_shouldThrowSourceRequired() {
            when(sourceService.getItems(null)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(Error.SOURCE_REQUIRED));

            verify(sourceService).getItems(null);
            verifyNoMoreInteractions(sourceService, balanceService, shellHelper);
            verifyNoInteractions(categoryService);
        }

        @Test
        @DisplayName("when there is no category, should throw CATEGORY_REQUIRED")
        void givenNoCategory_whenSave_shouldThrowCategoryRequired() {
            when(sourceService.getItems(null)).thenReturn(SOURCES);
            when(categoryService.getCategoryItems(null, true)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(Error.CATEGORY_REQUIRED));

            verify(sourceService).getItems(null);
            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(sourceService, categoryService, balanceService, shellHelper);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(sourceService, categoryService, adminProperties, balanceService, shellHelper);
        }

        @Test
        @DisplayName("when the flow fails for another reason, should propagate the exception")
        void givenUnexpectedFlowFailure_whenSave_shouldPropagate(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);
            var failure = new IllegalStateException("boom");
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(failure);

            assertThatThrownBy(() -> command.save(null)).isSameAs(failure);

            verifyNoInteractions(balanceService, shellHelper);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Day calendar date is required.",
                    "Source is required.",
                    "Category is required.",
                    "Amount is required.",
                    "Balance type is required.",
                    "Payment type is required.");
            verifyNoInteractions(balanceService);
            verifyNoMoreInteractions(shellHelper);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new balance")
        void givenNoId_whenSave_shouldSaveNewBalance(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);
            var context = completeContext();
            context.put("documentsIds", List.of(DOCUMENT_ID.toString()));
            stubFlowResult(context);
            when(balanceService.save(any(), isNull()))
                    .thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Balance with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(balanceService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BalanceSaveDto.builder()
                            .name("bogus")
                            .active(true)
                            .dayCalendarDate(LocalDate.of(2024, 3, 15))
                            .sourceId(SOURCE_ID)
                            .categoryId(CATEGORY_ID)
                            .amount(new BigDecimal("12.50"))
                            .code("CODE X")
                            .description("description")
                            .balanceType(BalanceType.INCOME)
                            .paymentType(PaymentType.CASH)
                            .documentsIds(Set.of(DOCUMENT_ID))
                            .multipartFiles(List.of())
                            .build());
            verify(shellHelper).success("Balance with id %s saved with success.".formatted(RETURNED_ID));
            verify(sourceService).getItems(null);
            verify(categoryService).getCategoryItems(null, true);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(balanceService, sourceService, categoryService, adminProperties, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the balance, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingBalance(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);
            var existing = createBalanceDto();
            when(balanceService.getById(ID)).thenReturn(existing);
            stubFlowResult(completeContext());
            when(balanceService.save(any(), eq(ID)))
                    .thenReturn(ID.toString());
            when(shellHelper.success("Balance with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(balanceService).getById(ID);
            verify(balanceService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BalanceSaveDto.builder()
                            .name("bogus")
                            .active(false)
                            .sequence((short) 3)
                            .dayCalendarDate(LocalDate.of(2024, 3, 15))
                            .sourceId(SOURCE_ID)
                            .categoryId(CATEGORY_ID)
                            .amount(new BigDecimal("12.50"))
                            .code("CODE X")
                            .description("description")
                            .balanceType(BalanceType.INCOME)
                            .paymentType(PaymentType.CASH)
                            .documentsIds(Set.of(DOCUMENT_ID))
                            .multipartFiles(List.of())
                            .build());
            verify(shellHelper).success("Balance with id %s saved with success.".formatted(ID));
            verify(sourceService).getItems(null);
            verify(categoryService).getCategoryItems(null, true);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(balanceService, sourceService, categoryService, adminProperties, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("when sources and categories exist, should build the balance flow params")
        void givenSourcesAndCategories_whenPreSaveFlow_shouldReturnBalanceParams(@TempDir Path filesPath) {
            stubPreSaveFlow(filesPath);

            var result = command.preSaveFlow(CommandParam.of(null));

            assertThat(result.getType()).isEqualTo(FlowType.BALANCE);
            assertThat(result.getSourceItems()).isSameAs(SOURCES);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            assertThat(result.getFilesPath()).isEqualTo(filesPath.toString());
            verify(sourceService).getItems(null);
            verify(categoryService).getCategoryItems(null, true);
            verify(adminProperties).getFilesPath();
            verifyNoMoreInteractions(sourceService, categoryService, adminProperties);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Balance with id %s deleted with success.".formatted(ID))).thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(balanceService).delete(ID);
            verify(shellHelper).success("Balance with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the balance found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createBalanceDto();
            when(balanceService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(balanceService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        static Stream<Arguments> sortProperties() {
            return Stream.of(
                    Arguments.of("null properties use the default", null, new String[]{"sequence"}),
                    Arguments.of("empty properties use the default", new String[0], new String[]{"sequence"}),
                    Arguments.of("given properties replace the default", new String[]{"name", "amount"},
                            new String[]{"name", "amount"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(balanceService.getAll(any(Pageable.class),
                    any(BalanceFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BALANCE)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", SOURCE_ID, CATEGORY_ID,
                    BalanceType.INCOME, PaymentType.PIX, (short) 2024, (short) 3, (short) 15, properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BalanceFilter.class);
            verify(balanceService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(filterBuilder().search(" abc ").build());
            verify(shellHelper).printTable(page, EruTable.BALANCE);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }

        @Test
        @DisplayName("when properties are only blank, should fall back to the default sort property")
        void givenBlankProperties_whenGetAll_shouldUseDefaultSort() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(balanceService.getAll(any(Pageable.class), any(BalanceFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BALANCE)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, null,
                    (short) 2024, (short) 3, null, " ", "");

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(balanceService).getAll(pageableCaptor.capture(), any(BalanceFilter.class));
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "sequence"));
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and use the first page")
        void givenNoOptionalFilters_whenGetAll_shouldPassNulls() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(balanceService.getAll(any(Pageable.class),
                    any(BalanceFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BALANCE)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, null,
                    (short) 2024, (short) 3, null);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BalanceFilter.class);
            verify(balanceService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "sequence"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BalanceFilter.builder().dayCalendarYear((short) 2024).dayCalendarMonth((short) 3).build());
            verify(shellHelper).printTable(page, EruTable.BALANCE);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should patch the status through the service and return the success message")
        void givenIdAndStatus_whenUpdate_shouldPatchAndReturnMessage() {
            when(shellHelper.success("Balance with id %s updated with success.".formatted(ID))).thenReturn("updated");

            var result = command.update(ID, true);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(balanceService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOf(BalancePatchDto.class);
            assertThat(dtoCaptor.getValue().isActive()).isTrue();
            verify(shellHelper).success("Balance with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("updateSequence")
    class UpdateSequence {

        @ParameterizedTest(name = "[{index}] {0}")
        @EnumSource(Direction.class)
        @DisplayName("should move the balance in the given direction and return the success message")
        void givenDirection_whenUpdateSequence_shouldDelegateAndReturnMessage(Direction direction) {
            var message = "Balance with id: '%s' updated with success (sequence).".formatted(ID);
            when(shellHelper.success(message)).thenReturn("moved");

            var result = command.updateSequence(ID, direction);

            assertThat(result).isEqualTo("moved");
            verify(balanceService).updateSequence(ID, direction);
            verify(shellHelper).success(message);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("swapPosition")
    class SwapPosition {

        @Test
        @DisplayName("should swap with the target sequence and return the success message")
        void givenTargetSequence_whenSwapPosition_shouldDelegateAndReturnMessage() {
            var message = "Balance with id: '%s' updated with success (position).".formatted(ID);
            when(shellHelper.success(message)).thenReturn("swapped");

            var result = command.swapPosition(ID, (short) 4);

            assertThat(result).isEqualTo("swapped");
            verify(balanceService).swapPosition(ID, (short) 4);
            verify(shellHelper).success(message);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("getSum")
    class GetSum {

        @Test
        @DisplayName("should sum with the built filter and print the sum table")
        void givenFilters_whenGetSum_shouldQueryServiceAndPrintTable() {
            List<BalanceSumDto> sums = List.of();
            when(balanceService.getSum(any(BalanceFilter.class))).thenReturn(sums);
            when(shellHelper.printTable(sums, EruTable.BALANCE_SUM)).thenReturn("sum");

            var result = command.getSum(true, "abc", SOURCE_ID, CATEGORY_ID, BalanceType.INCOME, PaymentType.PIX,
                    (short) 2024, (short) 3, (short) 15);

            assertThat(result).isEqualTo("sum");
            var filterCaptor = ArgumentCaptor.forClass(BalanceFilter.class);
            verify(balanceService).getSum(filterCaptor.capture());
            assertThat(filterCaptor.getValue()).usingRecursiveComparison().isEqualTo(filterBuilder().build());
            verify(shellHelper).printTable(sums, EruTable.BALANCE_SUM);
            verifyNoMoreInteractions(balanceService, shellHelper);
        }
    }
}
