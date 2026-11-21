package com.onurbcd.cli.command;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.billtype.BillTypePatchDto;
import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.dto.filter.BillTypeFilter;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.service.BillTypeService;
import com.onurbcd.cli.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestFactory.createBillTypeDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillTypeCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> CATEGORIES = List.of(SelectItem.of("category", CATEGORY_ID.toString()));

    @Mock
    private BillTypeService billTypeService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<BillTypeSaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private BillTypeCommand command;

    @BeforeEach
    void setUp() {
        command = new BillTypeCommand(billTypeService, flowBuilder, shellHelper, categoryService,
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
        context.put("name", "  Water   Bill ");
        context.put("path", "  bills/water  ");
        context.put("categoryId", CATEGORY_ID.toString());
        return context;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when there is no category, should throw CATEGORY_REQUIRED")
        void givenNoCategory_whenSave_shouldThrowCategoryRequired() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(List.of());

            assertThatThrownBy(() -> command.save(null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.CATEGORY_REQUIRED));

            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(categoryService);
            verifyNoInteractions(billTypeService, shellHelper);
        }

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(categoryService).getCategoryItems(null, true);
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(categoryService, shellHelper);
            verifyNoInteractions(billTypeService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Name is required.",
                    "Path is required",
                    "Category is required");
            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(categoryService, shellHelper);
            verifyNoInteractions(billTypeService);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new bill type")
        void givenNoId_whenSave_shouldSaveNewBillType() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);
            stubFlowResult(completeContext());
            when(billTypeService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Bill Type with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(billTypeService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BillTypeSaveDto.builder()
                            .name("Water Bill")
                            .active(true)
                            .path("bills/water")
                            .categoryId(CATEGORY_ID)
                            .build());
            verify(shellHelper).success("Bill Type with id %s saved with success.".formatted(RETURNED_ID));
            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(billTypeService, categoryService, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the bill type, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingBillType() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);
            when(billTypeService.getById(ID)).thenReturn(createBillTypeDto());
            stubFlowResult(completeContext());
            when(billTypeService.save(any(), eq(ID))).thenReturn(ID.toString());
            when(shellHelper.success("Bill Type with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(billTypeService).getById(ID);
            verify(billTypeService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    BillTypeSaveDto.builder()
                            .name("Water Bill")
                            .active(false)
                            .path("bills/water")
                            .categoryId(CATEGORY_ID)
                            .build());
            verify(shellHelper).success("Bill Type with id %s saved with success.".formatted(ID));
            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(billTypeService, categoryService, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("when categories exist, should build the bill type flow params")
        void givenCategories_whenPreSaveFlow_shouldReturnBillTypeParams() {
            when(categoryService.getCategoryItems(null, true)).thenReturn(CATEGORIES);

            var result = command.preSaveFlow(CommandParam.of(null));

            assertThat(result.getType()).isEqualTo(FlowType.BILL_TYPE);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            verify(categoryService).getCategoryItems(null, true);
            verifyNoMoreInteractions(categoryService);
            verifyNoInteractions(billTypeService, shellHelper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Bill Type with id %s deleted with success.".formatted(ID)))
                    .thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(billTypeService).delete(ID);
            verify(shellHelper).success("Bill Type with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(categoryService);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the bill type found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createBillTypeDto();
            when(billTypeService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(billTypeService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(categoryService);
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
                    Arguments.of("given properties replace the default", new String[]{"path", "active"},
                            new String[]{"path", "active"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(billTypeService.getAll(any(Pageable.class), any(BillTypeFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BILL_TYPE)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BillTypeFilter.class);
            verify(billTypeService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(BillTypeFilter.of(true, " abc "));
            verify(shellHelper).printTable(page, EruTable.BILL_TYPE);
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(categoryService);
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and use the first page")
        void givenNoOptionalFilters_whenGetAll_shouldPassNulls() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(billTypeService.getAll(any(Pageable.class), any(BillTypeFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.BILL_TYPE)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, (String[]) null);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(BillTypeFilter.class);
            verify(billTypeService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(BillTypeFilter.of(null, null));
            verify(shellHelper).printTable(page, EruTable.BILL_TYPE);
            verifyNoMoreInteractions(billTypeService, shellHelper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should patch the status through the service and return the success message")
        void givenIdAndStatus_whenUpdate_shouldPatchAndReturnMessage() {
            when(shellHelper.success("Bill Type with id %s updated with success.".formatted(ID)))
                    .thenReturn("updated");

            var result = command.update(ID, true);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(billTypeService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOf(BillTypePatchDto.class);
            assertThat(dtoCaptor.getValue().isActive()).isTrue();
            verify(shellHelper).success("Bill Type with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(billTypeService, shellHelper);
            verifyNoInteractions(categoryService);
        }
    }
}
