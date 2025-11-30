package com.onurbcd.eruservice.command;

import static com.onurbcd.eruservice.util.Constant.OPERATION_CANCELLED;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.dto.category.CategoryDto;
import com.onurbcd.eruservice.dto.category.CategoryPatchDto;
import com.onurbcd.eruservice.dto.category.CategorySaveDto;
import com.onurbcd.eruservice.dto.filter.CategoryFilter;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.factory.FlowFactory;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.CategorySaveFlowParam;
import com.onurbcd.eruservice.service.CategoryService;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.ValidatorUtil;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@ShellComponent
@ShellCommandGroup("Category")
@RequiredArgsConstructor
public class CategoryCommand {

    private final CategoryService service;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;

    @ShellMethod(key = "category-save", value = "Create or update a category.")
    public String save(@ShellOption(value = { "id",
            "-i" }, help = "The category's id.", defaultValue = ShellOption.NULL) UUID id) {

        var categorySaveDto = runSaveFlow(id);

        if (categorySaveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(categorySaveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = service.save(categorySaveDto, id);
        return shellHelper.success("Category with id: '%s' saved with success.".formatted(returnId));
    }

    @ShellMethod(key = "category-delete", value = "Delete category by id.")
    public String delete(@ShellOption(value = { "id", "-i" }, help = "The category's id.") @NotNull UUID id) {
        service.delete(id);
        return shellHelper.success("Category with id: '%s' deleted with success.".formatted(id));
    }

    @ShellMethod(key = "category-get", value = "Get category by id.")
    public String get(@ShellOption(value = { "id", "-i" }, help = "The category's id.") @NotNull UUID id)
            throws JsonProcessingException {

        return shellHelper.printJson(service.getById(id));
    }

    @ShellMethod(key = "category-get-all", value = "Get category's list.")
    public String getAll(
            @ShellOption(value = { "pageNumber",
                    "-n" }, help = "The page's number.", defaultValue = "1") @Min(1) Integer pageNumber,

            @ShellOption(value = { "pageSize",
                    "-s" }, help = "The page's size.", defaultValue = "10") @Min(1) Integer pageSize,

            @ShellOption(value = { "direction",
                    "-d" }, help = "The page's sort direction.", defaultValue = "ASC") Sort.Direction direction,

            @ShellOption(value = { "property",
                    "-p" }, help = "The page's sort property.", defaultValue = "name") String property,

            @ShellOption(value = { "active",
                    "-a" }, help = "Filter's active option.", defaultValue = ShellOption.NULL) Boolean active,

            @ShellOption(value = { "search",
                    "-f" }, help = "Filter's search option.", defaultValue = ShellOption.NULL) String search,

            @ShellOption(value = { "parentId",
                    "-i" }, help = "Filter's parent id.", defaultValue = ShellOption.NULL) UUID parentId,

            @ShellOption(value = { "level",
                    "-l" }, help = "Filter's level.", defaultValue = ShellOption.NULL) Short level,

            @ShellOption(value = { "lastBranch",
                    "-b" }, help = "Filter's last branch.", defaultValue = ShellOption.NULL) Boolean lastBranch) {

        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        CategoryFilter.of(active, search, parentId, level, lastBranch)),
                EruTable.CATEGORY);
    }

    @ShellMethod(key = "category-update", value = "Update category's status by id.")
    public String update(
            @ShellOption(value = { "id", "-i" }, help = "The category's id.") @NotNull UUID id,

            @ShellOption(value = { "active",
                    "-a" }, help = "The category's status.", defaultValue = "false") Boolean active) {

        service.update(CategoryPatchDto.of(active), id);
        return shellHelper.success("Category with id: '%s' updated with success.".formatted(id));
    }

    @Nullable
    private CategorySaveDto runSaveFlow(@Nullable UUID id) {
        var category = Optional.ofNullable(id).map(i -> (CategoryDto) service.getById(i)).orElse(null);
        var params = CategorySaveFlowParam.of(category, service.getCategoryItems(id, false));
        var flowResult = FlowUtil.runFlowSafely(FlowFactory.createCategorySaveFlow(flowBuilder, params));
        return flowResult != null ? CategorySaveDto.of(flowResult.getContext(), category) : null;
    }
}
