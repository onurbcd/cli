package com.onurbcd.cli.command;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.dto.source.BalanceSumDto;
import com.onurbcd.cli.dto.source.SourcePatchDto;
import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.IncomeSourceService;
import com.onurbcd.cli.service.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestFactory.createSourceDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> INCOME_SOURCES = List.of(SelectItem.of("income", PARENT_ID.toString()));

    @Mock
    private SourceService sourceService;
    @Mock
    private IncomeSourceService incomeSourceService;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<SourceSaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    @Captor
    private ArgumentCaptor<Iterable<BalanceSumDto>> tableCaptor;

    private SourceCommand command;

    @BeforeEach
    void setUp() {
        command = new SourceCommand(sourceService, flowBuilder, shellHelper, incomeSourceService,
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
        context.put("name", "  Main   Wallet ");
        context.put("incomeSourceId", PARENT_ID.toString());
        context.put("sourceType", "UNUSABLE");
        context.put("currencyType", "MONEY");
        context.put("balance", "250.75");
        return context;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when there is no income source, should throw INCOME_SOURCE_REQUIRED and not run the flow")
        void givenNoIncomeSource_whenSave_shouldThrowIncomeSourceRequired() {
            when(incomeSourceService.getItems(null)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.INCOME_SOURCE_REQUIRED));

            verify(incomeSourceService).getItems(null);
            verifyNoMoreInteractions(incomeSourceService);
            verifyNoInteractions(sourceService, shellHelper, flowBuilder);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning() {
            when(incomeSourceService.getItems(null)).thenReturn(INCOME_SOURCES);
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(incomeSourceService).getItems(null);
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(incomeSourceService, shellHelper);
            verifyNoInteractions(sourceService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors() {
            when(incomeSourceService.getItems(null)).thenReturn(INCOME_SOURCES);
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(incomeSourceService).getItems(null);
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Name is required.",
                    "Income source id is required.",
                    "Source type is required.",
                    "Currency type is required.",
                    "Balance is required.");
            verifyNoMoreInteractions(incomeSourceService, shellHelper);
            verifyNoInteractions(sourceService);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new active source")
        void givenNoId_whenSave_shouldSaveNewSource() {
            when(incomeSourceService.getItems(null)).thenReturn(INCOME_SOURCES);
            stubFlowResult(completeContext());
            when(sourceService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Source with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(incomeSourceService).getItems(null);
            verify(sourceService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    SourceSaveDto.builder()
                            .name("Main Wallet")
                            .active(true)
                            .incomeSourceId(PARENT_ID)
                            .sourceType(SourceType.UNUSABLE)
                            .currencyType(CurrencyType.MONEY)
                            .balance(new BigDecimal("250.75"))
                            .build());
            verify(shellHelper).success("Source with id %s saved with success.".formatted(RETURNED_ID));
            verifyNoMoreInteractions(sourceService, incomeSourceService, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the source, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingSource() {
            when(sourceService.getById(ID)).thenReturn(createSourceDto());
            when(incomeSourceService.getItems(null)).thenReturn(INCOME_SOURCES);
            stubFlowResult(completeContext());
            when(sourceService.save(any(), eq(ID))).thenReturn(ID.toString());
            when(shellHelper.success("Source with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(sourceService).getById(ID);
            verify(incomeSourceService).getItems(null);
            verify(sourceService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    SourceSaveDto.builder()
                            .name("Main Wallet")
                            .active(false)
                            .incomeSourceId(PARENT_ID)
                            .sourceType(SourceType.UNUSABLE)
                            .currencyType(CurrencyType.MONEY)
                            .balance(new BigDecimal("250.75"))
                            .build());
            verify(shellHelper).success("Source with id %s saved with success.".formatted(ID));
            verifyNoMoreInteractions(sourceService, incomeSourceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("when there are no income sources, should throw INCOME_SOURCE_REQUIRED")
        void givenNoIncomeSources_whenPreSaveFlow_shouldThrowIncomeSourceRequired() {
            when(incomeSourceService.getItems(null)).thenReturn(List.of());
            var params = CommandParam.of(ID);

            assertThatThrownBy(() -> command.preSaveFlow(params))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.INCOME_SOURCE_REQUIRED));

            verify(incomeSourceService).getItems(null);
            verifyNoMoreInteractions(incomeSourceService);
            verifyNoInteractions(sourceService, shellHelper);
        }

        @Test
        @DisplayName("should build the source flow params with the income source items")
        void givenIncomeSources_whenPreSaveFlow_shouldReturnSourceParams() {
            when(incomeSourceService.getItems(null)).thenReturn(INCOME_SOURCES);

            var result = command.preSaveFlow(CommandParam.of(ID));

            assertThat(result.getType()).isEqualTo(FlowType.SOURCE);
            assertThat(result).usingRecursiveComparison().isEqualTo(SaveFlowParam.source(INCOME_SOURCES));
            verify(incomeSourceService).getItems(null);
            verifyNoMoreInteractions(incomeSourceService);
            verifyNoInteractions(sourceService, shellHelper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Source with id %s deleted with success.".formatted(ID)))
                    .thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(sourceService).delete(ID);
            verify(shellHelper).success("Source with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the source found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createSourceDto();
            when(sourceService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(sourceService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        static Stream<Arguments> sortProperties() {
            return Stream.of(
                    Arguments.of("null properties use the default", null, new String[]{"name"}),
                    Arguments.of("empty properties use the default", new String[0], new String[]{"name"}),
                    Arguments.of("blank properties use the default", new String[]{" ", ""}, new String[]{"name"}),
                    Arguments.of("given properties replace the default", new String[]{"active", "id"},
                            new String[]{"active", "id"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(sourceService.getAll(any(Pageable.class), any(SourceFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.SOURCE)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", PARENT_ID, SourceType.USABLE,
                    CurrencyType.DIGITAL, properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(SourceFilter.class);
            verify(sourceService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            var filter = filterCaptor.getValue();
            assertThat(filter).usingRecursiveComparison()
                    .isEqualTo(SourceFilter.of(true, " abc ", PARENT_ID, SourceType.USABLE, CurrencyType.DIGITAL));
            assertThat(filter.isActive()).isTrue();
            assertThat(filter.getSearch()).isEqualTo("abc");
            assertThat(filter.getIncomeSourceId()).isEqualTo(PARENT_ID);
            assertThat(filter.getSourceType()).isEqualTo(SourceType.USABLE);
            assertThat(filter.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
            verify(shellHelper).printTable(page, EruTable.SOURCE);
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and use the first page")
        void givenNoOptionalFilters_whenGetAll_shouldPassNulls() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(sourceService.getAll(any(Pageable.class), any(SourceFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.SOURCE)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, (String[]) null);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(SourceFilter.class);
            verify(sourceService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(SourceFilter.of(null, null, null, null, null));
            verify(shellHelper).printTable(page, EruTable.SOURCE);
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @ParameterizedTest(name = "[{index}] active={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("should patch the status through the service and return the success message")
        void givenIdAndStatus_whenUpdate_shouldPatchAndReturnMessage(boolean active) {
            when(shellHelper.success("Source with id %s updated with success.".formatted(ID)))
                    .thenReturn("updated");

            var result = command.update(ID, active);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(sourceService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOf(SourcePatchDto.class);
            assertThat(dtoCaptor.getValue().isActive()).isEqualTo(active);
            verify(shellHelper).success("Source with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }
    }

    @Nested
    @DisplayName("getBalanceSum")
    class GetBalanceSum {

        @Test
        @DisplayName("should query the sum with the filter and print it as a single-row table")
        void givenFilters_whenGetBalanceSum_shouldPrintSum() {
            var sum = new BalanceSumDto(new BigDecimal("10.50"), new BigDecimal("99.99"));
            when(sourceService.getBalanceSum(any(SourceFilter.class))).thenReturn(sum);
            doReturn("table").when(shellHelper).printTable(
                    ArgumentMatchers.<Iterable<BalanceSumDto>>any(), eq(EruTable.SOURCE_BALANCE_SUM));

            var result = command.getBalanceSum(false, " abc ", PARENT_ID, SourceType.UNUSABLE, CurrencyType.MONEY);

            assertThat(result).isEqualTo("table");
            var filterCaptor = ArgumentCaptor.forClass(SourceFilter.class);
            verify(sourceService).getBalanceSum(filterCaptor.capture());
            var filter = filterCaptor.getValue();
            assertThat(filter).usingRecursiveComparison()
                    .isEqualTo(SourceFilter.of(false, " abc ", PARENT_ID, SourceType.UNUSABLE, CurrencyType.MONEY));
            assertThat(filter.isActive()).isFalse();
            assertThat(filter.getSearch()).isEqualTo("abc");
            assertThat(filter.getIncomeSourceId()).isEqualTo(PARENT_ID);
            assertThat(filter.getSourceType()).isEqualTo(SourceType.UNUSABLE);
            assertThat(filter.getCurrencyType()).isEqualTo(CurrencyType.MONEY);
            verify(shellHelper).printTable(tableCaptor.capture(), eq(EruTable.SOURCE_BALANCE_SUM));
            assertThat(tableCaptor.getValue()).containsExactly(sum);
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }

        @Test
        @DisplayName("when filters are absent, should pass null filter fields")
        void givenNoFilters_whenGetBalanceSum_shouldPassNulls() {
            var sum = new BalanceSumDto(null, null);
            when(sourceService.getBalanceSum(any(SourceFilter.class))).thenReturn(sum);
            doReturn("table").when(shellHelper).printTable(
                    ArgumentMatchers.<Iterable<BalanceSumDto>>any(), eq(EruTable.SOURCE_BALANCE_SUM));

            var result = command.getBalanceSum(null, null, null, null, null);

            assertThat(result).isEqualTo("table");
            var filterCaptor = ArgumentCaptor.forClass(SourceFilter.class);
            verify(sourceService).getBalanceSum(filterCaptor.capture());
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(SourceFilter.of(null, null, null, null, null));
            verify(shellHelper).printTable(ArgumentMatchers.<Iterable<BalanceSumDto>>any(),
                    eq(EruTable.SOURCE_BALANCE_SUM));
            verifyNoMoreInteractions(sourceService, shellHelper);
            verifyNoInteractions(incomeSourceService);
        }
    }
}
