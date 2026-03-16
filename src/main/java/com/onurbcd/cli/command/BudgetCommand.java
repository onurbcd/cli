package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.budget.CopyBudgetDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.SaveFlowParam;
import com.onurbcd.cli.service.BillTypeService;
import com.onurbcd.cli.service.BudgetService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.BUDGET;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;
import static com.onurbcd.cli.validator.Action.checkIfNotEmpty;

@ShellComponent
@ShellCommandGroup(BUDGET)
public class BudgetCommand extends BaseCommand {

    private final BudgetService service;
    private final BillTypeService billTypeService;

    public BudgetCommand(BudgetService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                         BillTypeService billTypeService) {

        super(service, flowBuilder, shellHelper, BUDGET, EruTable.BUDGET);
        this.service = service;
        this.billTypeService = billTypeService;
    }

    @ShellMethod(key = "budget-save", value = "Create or update a budget.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(id);
    }

    @ShellMethod(key = "budget-delete", value = "Delete budget by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "budget-get", value = "Get budget by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "budget-get-all", value = "Get budget's list.")
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

            @ShellOption(value = {"refYear", "-y"}, help = "Filter's reference year.", defaultValue = ShellOption.NULL)
            Short refYear,

            @ShellOption(value = {"refMonth", "-m"}, help = "Filter's reference month.", defaultValue = ShellOption.NULL)
            Short refMonth,

            @ShellOption(value = {"billTypeId", "-i"}, help = "Filter's bill type id.", defaultValue = ShellOption.NULL)
            UUID billTypeId,

            @ShellOption(value = {"paid", "-e"}, help = "Filter's paid option.", defaultValue = ShellOption.NULL)
            Boolean paid,

            @ShellOption(value = {"properties", "-p"}, help = "The page's sort properties.", defaultValue = ShellOption.NULL)
            String... properties
    ) {
        var filter = BudgetFilter.of(active, search, refYear, refMonth, billTypeId, paid);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "sequence"));
    }

    @ShellMethod(key = "budget-update", value = "Update budget's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The budget's active status.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"paid", "-p"}, help = "The budget's paid status.", defaultValue = ShellOption.NULL)
            Boolean paid
    ) {
        return baseUpdate(BudgetPatchDto.of(active, paid), id);
    }

    @ShellMethod(key = "budget-update-sequence", value = "Update budget's sequence by id.")
    public String updateSequence(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"direction", "-d"}, help = "The sequence's direction.")
            @NotNull
            Direction direction
    ) {
        service.updateSequence(id, direction);
        return shellHelper.success("Budget with id: '%s' updated with success (sequence).".formatted(id));
    }

    @ShellMethod(key = "budget-swap-position", value = "Swap budget's position by id.")
    public String swapPosition(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"targetSequence", "-t"}, help = "The target sequence.")
            @NotNull
            Short targetSequence
    ) {
        service.swapPosition(id, targetSequence);
        return shellHelper.success("Budget with id: '%s' updated with success (position).".formatted(id));
    }

    @ShellMethod(key = "budget-sum-month", value = "Get budget's sum by month.")
    public String getSumByMonth(
            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-s"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search,

            @ShellOption(value = {"refYear", "-y"}, help = "Filter's reference year.", defaultValue = ShellOption.NULL)
            Short refYear,

            @ShellOption(value = {"refMonth", "-m"}, help = "Filter's reference month.", defaultValue = ShellOption.NULL)
            Short refMonth,

            @ShellOption(value = {"billTypeId", "-b"}, help = "Filter's bill type id.", defaultValue = ShellOption.NULL)
            UUID billTypeId,

            @ShellOption(value = {"paid", "-p"}, help = "Filter's paid option.", defaultValue = ShellOption.NULL)
            Boolean paid
    ) {
        return shellHelper.printTable(
                service.getSumByMonth(BudgetFilter.of(active, search, refYear, refMonth, billTypeId, paid)),
                EruTable.BUDGET_SUM_MONTH
        );
    }

    @ShellMethod(key = "budget-copy", value = "Copy budget.")
    public String copy(
            @ShellOption(value = {"fromYear", "-y"}, help = "Copy from year.")
            @NotNull
            Short fromYear,

            @ShellOption(value = {"fromMonth", "-m"}, help = "Copy from month.")
            @NotNull
            Short fromMonth,

            @ShellOption(value = {"toYear", "-a"}, help = "To year.")
            @NotNull
            Short toYear,

            @ShellOption(value = {"toMonth", "-b"}, help = "To month.")
            @NotNull
            Short toMonth
    ) {
        service.copy(CopyBudgetDto.of(fromYear, fromMonth, toYear, toMonth));
        return shellHelper.success("Budget copied from %d/%02d to %d/%02d.".formatted(fromYear, fromMonth, toYear, toMonth));
    }

    @ShellMethod(key = "budget-delete-all", value = "Delete all budgets by year and month.")
    public String deleteAll(
            @ShellOption(value = {"refYear", "-y"}, help = "Reference year.")
            @NotNull
            Short refYear,

            @ShellOption(value = {"refMonth", "-m"}, help = "Reference month.")
            @NotNull
            Short refMonth
    ) {
        service.deleteAll(refYear, refMonth);
        return shellHelper.success("All budgets for %d/%02d deleted.".formatted(refYear, refMonth));
    }

    @Override
    protected SaveFlowParam preSaveFlow(@Nullable UUID id) {
        var billTypeItems = billTypeService.getItems(null);
        checkIfNotEmpty(billTypeItems).orElseThrow(Error.BILL_TYPE_REQUIRED);
        return SaveFlowParam.budget(billTypeItems);
    }
}
