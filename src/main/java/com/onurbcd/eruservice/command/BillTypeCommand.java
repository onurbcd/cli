package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.dto.billtype.BillTypePatchDto;
import com.onurbcd.eruservice.dto.filter.BillTypeFilter;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.SaveFlowParam;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.service.CategoryService;
import com.onurbcd.eruservice.validator.Action;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.BILL_TYPE;

@ShellComponent
@ShellCommandGroup(BILL_TYPE)
public class BillTypeCommand extends BaseCommand {

    private final CategoryService categoryService;

    public BillTypeCommand(BillTypeService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                           CategoryService categoryService) {

        super(service, flowBuilder, shellHelper, BILL_TYPE, EruTable.BILL_TYPE);
        this.categoryService = categoryService;
    }

    @ShellMethod(key = "bill-type-save", value = "Create or update a bill type.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The bill type's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(id);
    }

    @ShellMethod(key = "bill-type-delete", value = "Delete bill type by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The bill type's id.")
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "bill-type-get", value = "Get bill type by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The bill type's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "bill-type-get-all", value = "Get bill type's list.")
    public String getAll(
            @ShellOption(value = {"pageNumber", "-n"}, help = "The page's number.", defaultValue = "1")
            @Min(1)
            Integer pageNumber,

            @ShellOption(value = {"pageSize", "-s"}, help = "The page's size.", defaultValue = "10")
            @Min(1)
            Integer pageSize,

            @ShellOption(value = {"direction", "-d"}, help = "The page's sort direction.", defaultValue = "ASC")
            Sort.Direction direction,

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "name")
            String property,

            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search
    ) {
        return baseGetAll(pageNumber, pageSize, direction, property, BillTypeFilter.of(active, search));
    }

    @ShellMethod(key = "bill-type-update", value = "Update bill type's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The bill type's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The bill type's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(BillTypePatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow() {
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(Error.CATEGORY_REQUIRED);
        return SaveFlowParam.billType(categoryItems);
    }
}
