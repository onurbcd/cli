package com.onurbcd.cli.command;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.category.CategoryPatchDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.service.CategoryService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestFactory.createCategoryDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
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
class CategoryCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final List<SelectItem> CATEGORIES = List.of(SelectItem.of("parent", PARENT_ID.toString()));

    @Mock
    private CategoryService categoryService;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<CategorySaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private CategoryCommand command;

    @BeforeEach
    void setUp() {
        command = new CategoryCommand(categoryService, flowBuilder, shellHelper, TestValidator.create());
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
        context.put("name", "  Home   Expenses ");
        context.put("description", "  Everyday   costs ");
        context.put("parentId", PARENT_ID.toString());
        return context;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning() {
            when(categoryService.getCategoryItems(null, false)).thenReturn(CATEGORIES);
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(categoryService).getCategoryItems(null, false);
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors() {
            when(categoryService.getCategoryItems(null, false)).thenReturn(CATEGORIES);
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Name is required.",
                    "Parent id is required.");
            verify(categoryService).getCategoryItems(null, false);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new active category")
        void givenNoId_whenSave_shouldSaveNewCategory() {
            when(categoryService.getCategoryItems(null, false)).thenReturn(CATEGORIES);
            stubFlowResult(completeContext());
            when(categoryService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Category with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(categoryService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    CategorySaveDto.builder()
                            .name("Home Expenses")
                            .active(true)
                            .parentId(PARENT_ID)
                            .description("Everyday costs")
                            .build());
            verify(shellHelper).success("Category with id %s saved with success.".formatted(RETURNED_ID));
            verify(categoryService).getCategoryItems(null, false);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the category, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingCategory() {
            when(categoryService.getCategoryItems(ID, false)).thenReturn(CATEGORIES);
            when(categoryService.getById(ID)).thenReturn(createCategoryDto());
            stubFlowResult(completeContext());
            when(categoryService.save(any(), eq(ID))).thenReturn(ID.toString());
            when(shellHelper.success("Category with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(categoryService).getById(ID);
            verify(categoryService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    CategorySaveDto.builder()
                            .name("Home Expenses")
                            .active(false)
                            .parentId(PARENT_ID)
                            .description("Everyday costs")
                            .build());
            verify(shellHelper).success("Category with id %s saved with success.".formatted(ID));
            verify(categoryService).getCategoryItems(ID, false);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("when no id is given, should build the category flow params with all categories")
        void givenNoId_whenPreSaveFlow_shouldReturnCategoryParams() {
            when(categoryService.getCategoryItems(null, false)).thenReturn(CATEGORIES);

            var result = command.preSaveFlow(CommandParam.of(null));

            assertThat(result.getType()).isEqualTo(FlowType.CATEGORY);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            verify(categoryService).getCategoryItems(null, false);
            verifyNoMoreInteractions(categoryService);
            verifyNoInteractions(shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should request the items for that category")
        void givenId_whenPreSaveFlow_shouldRequestItemsForId() {
            when(categoryService.getCategoryItems(ID, false)).thenReturn(CATEGORIES);

            var result = command.preSaveFlow(CommandParam.of(ID));

            assertThat(result.getType()).isEqualTo(FlowType.CATEGORY);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            verify(categoryService).getCategoryItems(ID, false);
            verifyNoMoreInteractions(categoryService);
            verifyNoInteractions(shellHelper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Category with id %s deleted with success.".formatted(ID)))
                    .thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(categoryService).delete(ID);
            verify(shellHelper).success("Category with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(categoryService, shellHelper);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the category found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createCategoryDto();
            when(categoryService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(categoryService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        static Stream<Arguments> sortProperties() {
            return Stream.of(
                    Arguments.of("null properties use the default", null, new String[]{"level", "name"}),
                    Arguments.of("empty properties use the default", new String[0], new String[]{"level", "name"}),
                    Arguments.of("blank properties use the default", new String[]{" ", ""},
                            new String[]{"level", "name"}),
                    Arguments.of("given properties replace the default", new String[]{"description", "active"},
                            new String[]{"description", "active"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(categoryService.getAll(any(Pageable.class), any(CategoryFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.CATEGORY)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", PARENT_ID, (short) 3, false,
                    properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(categoryService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(CategoryFilter.of(true, " abc ", PARENT_ID, (short) 3, false));
            assertThat(filterCaptor.getValue().isActive()).isTrue();
            assertThat(filterCaptor.getValue().getSearch()).isEqualTo("abc");
            assertThat(filterCaptor.getValue().getParentId()).isEqualTo(PARENT_ID);
            assertThat(filterCaptor.getValue().getLevel()).isEqualTo((short) 3);
            assertThat(filterCaptor.getValue().getLastBranch()).isFalse();
            verify(shellHelper).printTable(page, EruTable.CATEGORY);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and use the first page")
        void givenNoOptionalFilters_whenGetAll_shouldPassNulls() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(categoryService.getAll(any(Pageable.class), any(CategoryFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.CATEGORY)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, null, null, null, (String[]) null);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(CategoryFilter.class);
            verify(categoryService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "level", "name"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(CategoryFilter.of(null, null, null, null, null));
            verify(shellHelper).printTable(page, EruTable.CATEGORY);
            verifyNoMoreInteractions(categoryService, shellHelper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @ParameterizedTest(name = "[{index}] active={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("should patch the status through the service and return the success message")
        void givenIdAndStatus_whenUpdate_shouldPatchAndReturnMessage(boolean active) {
            when(shellHelper.success("Category with id %s updated with success.".formatted(ID)))
                    .thenReturn("updated");

            var result = command.update(ID, active);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(categoryService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOf(CategoryPatchDto.class);
            assertThat(dtoCaptor.getValue().isActive()).isEqualTo(active);
            verify(shellHelper).success("Category with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(categoryService, shellHelper);
        }
    }
}
