package com.onurbcd.cli.command;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.balance.BalancePatchDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.SelectItem;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.BALANCE;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceCommandTest {

    private BalanceCommand balanceCommand;

    @Mock
    private BalanceService balanceService;
    @Mock
    private SourceService sourceService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AdminProperties adminProperties;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Mock
    private ShellHelper shellHelper;

    @BeforeEach
    void setUp() {
        balanceCommand = new BalanceCommand(balanceService, sourceService, categoryService, flowBuilder, shellHelper,
                adminProperties);
    }

    // constructor

    @Test
    void constructor_createsInstance() {
        assertNotNull(balanceCommand);
        assertInstanceOf(BaseCommand.class, balanceCommand);
    }

    @Test
    void constructor_assignsOwnDependencies() {
        assertSame(balanceService, ReflectionTestUtils.getField(balanceCommand, "service"));
        assertSame(sourceService, ReflectionTestUtils.getField(balanceCommand, "sourceService"));
        assertSame(categoryService, ReflectionTestUtils.getField(balanceCommand, "categoryService"));
        assertSame(adminProperties, ReflectionTestUtils.getField(balanceCommand, "config"));
    }

    @Test
    void constructor_passesArgumentsToSuperclass() {
        assertSame(balanceService, ReflectionTestUtils.getField(balanceCommand, BaseCommand.class, "crudService"));
        assertSame(flowBuilder, ReflectionTestUtils.getField(balanceCommand, BaseCommand.class, "flowBuilder"));
        assertSame(shellHelper, ReflectionTestUtils.getField(balanceCommand, BaseCommand.class, "shellHelper"));
        assertEquals(BALANCE, ReflectionTestUtils.getField(balanceCommand, BaseCommand.class, "name"));
        assertEquals(EruTable.BALANCE, ReflectionTestUtils.getField(balanceCommand, BaseCommand.class, "table"));
    }

    // save

    @Test
    void save_whenNoSourceExists_throwsSourceRequired() {
        when(sourceService.getItems(null)).thenReturn(List.of());

        var ex = assertThrows(ApiException.class, () -> balanceCommand.save(null));

        assertEquals(Error.SOURCE_REQUIRED, ex.getError());
        verifyNoInteractions(categoryService);
    }

    @Test
    void save_whenNoCategoryExists_throwsCategoryRequired() {
        when(sourceService.getItems(null)).thenReturn(List.of(SelectItem.of("source", "1")));
        when(categoryService.getCategoryItems(null, true)).thenReturn(List.of());

        var ex = assertThrows(ApiException.class, () -> balanceCommand.save(null));

        assertEquals(Error.CATEGORY_REQUIRED, ex.getError());
    }

    @Test
    void save_whenFlowIsInterrupted_returnsCancelledWarning(@TempDir Path filesPath) {
        when(sourceService.getItems(null)).thenReturn(List.of(SelectItem.of("source", "1")));
        when(categoryService.getCategoryItems(null, true)).thenReturn(List.of(SelectItem.of("category", "2")));
        when(adminProperties.getFilesPath()).thenReturn(filesPath.toString());
        when(flowBuilder.clone()).thenReturn(flowBuilder);
        when(flowBuilder.reset()).thenReturn(flowBuilder);
        when(flowBuilder.build().run())
                .thenThrow(new RuntimeException(new InterruptedIOException()));
        when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

        assertEquals("cancelled", balanceCommand.save(null));
    }

    @Test
    void preSaveFlow_whenSourcesAndCategoriesExist_returnsBalanceFlowParam() {
        var sources = List.of(SelectItem.of("source", "1"));
        var categories = List.of(SelectItem.of("category", "2"));
        when(sourceService.getItems(null)).thenReturn(sources);
        when(categoryService.getCategoryItems(null, true)).thenReturn(categories);
        when(adminProperties.getFilesPath()).thenReturn("/files");

        var result = balanceCommand.preSaveFlow(CommandParam.of(null));

        assertEquals(FlowType.BALANCE, result.getType());
        assertSame(sources, result.getSourceItems());
        assertSame(categories, result.getCategoryItems());
        assertEquals("/files", result.getFilesPath());
    }

    @Test
    void delete_delegatesToServiceAndReturnsSuccessMessage() {
        var id = UUID.randomUUID();
        when(shellHelper.success(any())).thenReturn("ok");

        assertEquals("ok", balanceCommand.delete(id));

        verify(balanceService).delete(id);
    }

    @Test
    void get_returnsPrintedJson() {
        var id = UUID.randomUUID();
        var dto = mock(BalanceDto.class);
        when(balanceService.getById(id)).thenReturn(dto);
        when(shellHelper.printJson(dto)).thenReturn("json");

        assertEquals("json", balanceCommand.get(id));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAll_withDefaultProperties_usesSequenceSortAndBuildsFilter() {
        var sourceId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        Page<Dtoable> page = mock(Page.class);
        when(balanceService.getAll(any(Pageable.class), any())).thenReturn(page);
        when(shellHelper.printTable(page, EruTable.BALANCE)).thenReturn("table");

        var result = balanceCommand.getAll(2, 5, Sort.Direction.DESC, true, "abc", sourceId, categoryId,
                BalanceType.INCOME, PaymentType.CASH, (short) 2024, (short) 3, (short) 15, (String[]) null);

        assertEquals("table", result);

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        var filterCaptor = ArgumentCaptor.forClass(BalanceFilter.class);
        verify(balanceService).getAll(pageableCaptor.capture(), filterCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "sequence"), pageable.getSort());

        var filter = filterCaptor.getValue();
        assertEquals(sourceId, filter.getSourceId());
        assertEquals(categoryId, filter.getCategoryId());
        assertEquals(BalanceType.INCOME, filter.getBalanceType());
        assertEquals(PaymentType.CASH, filter.getPaymentType());
        assertEquals((short) 2024, filter.getDayCalendarYear());
        assertEquals((short) 3, filter.getDayCalendarMonth());
        assertEquals((short) 15, filter.getDayCalendarDayInMonth());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAll_withCustomProperties_usesGivenSortProperties() {
        Page<Dtoable> page = mock(Page.class);
        when(balanceService.getAll(any(Pageable.class), any())).thenReturn(page);
        when(shellHelper.printTable(page, EruTable.BALANCE)).thenReturn("table");

        balanceCommand.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, null, null, null, null,
                "name", "value");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(balanceService).getAll(pageableCaptor.capture(), any());
        assertEquals(Sort.by(Sort.Direction.ASC, "name", "value"), pageableCaptor.getValue().getSort());
    }

    @Test
    void update_delegatesToServiceAndReturnsSuccessMessage() {
        var id = UUID.randomUUID();
        when(shellHelper.success(any())).thenReturn("ok");

        assertEquals("ok", balanceCommand.update(id, true));

        verify(balanceService).update(any(BalancePatchDto.class), eq(id));
    }

    @Test
    void updateSequence_delegatesToServiceAndReturnsSuccessMessage() {
        var id = UUID.randomUUID();
        when(shellHelper.success(any())).thenReturn("ok");

        assertEquals("ok", balanceCommand.updateSequence(id, Direction.UP));

        verify(balanceService).updateSequence(id, Direction.UP);
        verify(shellHelper).success("Balance with id: '%s' updated with success (sequence).".formatted(id));
    }

    @Test
    void swapPosition_delegatesToServiceAndReturnsSuccessMessage() {
        var id = UUID.randomUUID();
        when(shellHelper.success(any())).thenReturn("ok");

        assertEquals("ok", balanceCommand.swapPosition(id, (short) 4));

        verify(balanceService).swapPosition(id, (short) 4);
        verify(shellHelper).success("Balance with id: '%s' updated with success (position).".formatted(id));
    }

    @Test
    void getSum_buildsFilterAndReturnsPrintedTable() {
        var sourceId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        List<BalanceSumDto> sums = List.of();
        when(balanceService.getSum(any(BalanceFilter.class))).thenReturn(sums);
        when(shellHelper.printTable(sums, EruTable.BALANCE_SUM)).thenReturn("sum");

        var result = balanceCommand.getSum(true, "abc", sourceId, categoryId, BalanceType.OUTCOME, PaymentType.CASH,
                (short) 2023, (short) 12, (short) 1);

        assertEquals("sum", result);

        var filterCaptor = ArgumentCaptor.forClass(BalanceFilter.class);
        verify(balanceService).getSum(filterCaptor.capture());
        var filter = filterCaptor.getValue();
        assertEquals(sourceId, filter.getSourceId());
        assertEquals(categoryId, filter.getCategoryId());
        assertEquals(BalanceType.OUTCOME, filter.getBalanceType());
        assertEquals((short) 2023, filter.getDayCalendarYear());
        assertEquals((short) 12, filter.getDayCalendarMonth());
        assertEquals((short) 1, filter.getDayCalendarDayInMonth());
    }
}
