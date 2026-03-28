package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.dto.category.CategoryPatchDto;
import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.CategoryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.CATEGORY;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@ShellComponent
@ShellCommandGroup(CATEGORY)
public class CategoryCommand extends BaseCommand {

    private final CategoryService service;

    public CategoryCommand(CategoryService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper) {
        super(service, flowBuilder, shellHelper, CATEGORY, EruTable.CATEGORY);
        this.service = service;
    }

    @ShellMethod(key = "category-save", value = "Create or update a category.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The category's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @ShellMethod(key = "category-delete", value = "Delete category by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The category's id.")
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "category-get", value = "Get category by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The category's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "category-get-all", value = "Get category's list.")
    public String getAll(
            @ShellOption(value = {"pageNumber", "-n"}, help = "The page's number.", defaultValue = "1")
            @Min(1)
            Integer pageNumber,

            @ShellOption(value = {"pageSize", "-s"}, help = "The page's size.", defaultValue = "10")
            @Min(1)
            Integer pageSize,

            @ShellOption(value = {"direction", "-d"}, help = "The page's sort direction.", defaultValue = "ASC")
            Sort.Direction direction,

            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search,

            @ShellOption(value = {"parentId", "-i"}, help = "Filter's parent id.", defaultValue = ShellOption.NULL)
            UUID parentId,

            @ShellOption(value = {"level", "-l"}, help = "Filter's level.", defaultValue = ShellOption.NULL)
            Short level,

            @ShellOption(value = {"lastBranch", "-b"}, help = "Filter's last branch.", defaultValue = ShellOption.NULL)
            Boolean lastBranch,

            @ShellOption(value = {"properties", "-p"}, help = "The page's sort properties.", defaultValue = ShellOption.NULL)
            String... properties
    ) {
        var filter = CategoryFilter.of(active, search, parentId, level, lastBranch);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "level", "name"));
    }

    @ShellMethod(key = "category-update", value = "Update category's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The category's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The category's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(CategoryPatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var categoryItems = service.getCategoryItems(params.getId(), false);
        return SaveFlowParam.category(categoryItems);
    }
}
