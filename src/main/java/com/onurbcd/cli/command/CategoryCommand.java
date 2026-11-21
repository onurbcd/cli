package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.category.CategoryPatchDto;
import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.CategoryService;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.CATEGORY;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = CATEGORY)
public class CategoryCommand extends BaseCommand {

    private final CategoryService service;

    public CategoryCommand(CategoryService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                           Validator validator) {
        super(service, flowBuilder, shellHelper, CATEGORY, EruTable.CATEGORY, validator);
        this.service = service;
    }

    @Command(name = "category-save", description = "Create or update a category.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The category's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "category-delete", description = "Delete category by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The category's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "category-get", description = "Get category by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The category's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "category-get-all", description = "Get category's list.")
    public String getAll(
            @Option(shortName = 'n', longName = "pageNumber", description = "The page's number.", defaultValue = "1")
            @Min(1)
            Integer pageNumber,

            @Option(shortName = 's', longName = "pageSize", description = "The page's size.", defaultValue = "10")
            @Min(1)
            Integer pageSize,

            @Option(shortName = 'd', longName = "direction", description = "The page's sort direction.", defaultValue = "ASC")
            Sort.Direction direction,

            @Option(shortName = 'a', longName = "active", description = "Filter's active option.")
            Boolean active,

            @Option(shortName = 'f', longName = "search", description = "Filter's search option.")
            String search,

            @Option(shortName = 'i', longName = "parentId", description = "Filter's parent id.")
            UUID parentId,

            @Option(shortName = 'l', longName = "level", description = "Filter's level.")
            Short level,

            @Option(shortName = 'b', longName = "lastBranch", description = "Filter's last branch.")
            Boolean lastBranch,

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = CategoryFilter.of(active, search, parentId, level, lastBranch);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "level", "name"));
    }

    @Command(name = "category-update", description = "Update category's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The category's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The category's status.", defaultValue = "false")
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
