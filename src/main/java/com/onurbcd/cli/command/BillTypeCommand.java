package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.billtype.BillTypePatchDto;
import com.onurbcd.cli.dto.filter.BillTypeFilter;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.BillTypeService;
import com.onurbcd.cli.service.CategoryService;
import com.onurbcd.cli.validator.Action;
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

import static com.onurbcd.cli.util.Constant.BILL_TYPE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = BILL_TYPE)
public class BillTypeCommand extends BaseCommand {

    private final CategoryService categoryService;

    public BillTypeCommand(BillTypeService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                           CategoryService categoryService, Validator validator) {

        super(service, flowBuilder, shellHelper, BILL_TYPE, EruTable.BILL_TYPE, validator);
        this.categoryService = categoryService;
    }

    @Command(name = "bill-type-save", description = "Create or update a bill type.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The bill type's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "bill-type-delete", description = "Delete bill type by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The bill type's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "bill-type-get", description = "Get bill type by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The bill type's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "bill-type-get-all", description = "Get bill type's list.")
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

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = BillTypeFilter.of(active, search);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @Command(name = "bill-type-update", description = "Update bill type's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The bill type's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The bill type's status.", defaultValue = "false", required = true)
            Boolean active
    ) {
        return baseUpdate(BillTypePatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(ErrorCode.CATEGORY_REQUIRED);
        return SaveFlowParam.billType(categoryItems);
    }
}
