package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.budget.CopyBudgetDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.BillTypeService;
import com.onurbcd.cli.service.BudgetService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.BUDGET;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;
import static com.onurbcd.cli.validator.Action.checkIfNotEmpty;

@Component
@CommandGroup(name = BUDGET)
public class BudgetCommand extends BaseCommand {

    private final BudgetService service;
    private final BillTypeService billTypeService;

    public BudgetCommand(BudgetService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                         BillTypeService billTypeService) {

        super(service, flowBuilder, shellHelper, BUDGET, EruTable.BUDGET);
        this.service = service;
        this.billTypeService = billTypeService;
    }

    @Command(name = "budget-save", description = "Create or update a budget.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "budget-delete", description = "Delete budget by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "budget-get", description = "Get budget by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "budget-get-all", description = "Get budget's list.")
    public String getAll(
            @Option(shortName = 'n', longName = "pageNumber", description = "The page's number.", defaultValue = "1", required = true)
            @Min(1)
            Integer pageNumber,

            @Option(shortName = 's', longName = "pageSize", description = "The page's size.", defaultValue = "10", required = true)
            @Min(1)
            Integer pageSize,

            @Option(shortName = 'd', longName = "direction", description = "The page's sort direction.", defaultValue = "ASC", required = true)
            Sort.Direction direction,

            @Option(shortName = 'a', longName = "active", description = "Filter's active option.")
            Boolean active,

            @Option(shortName = 'f', longName = "search", description = "Filter's search option.")
            String search,

            @Option(shortName = 'y', longName = "refYear", description = "Filter's reference year.")
            Short refYear,

            @Option(shortName = 'm', longName = "refMonth", description = "Filter's reference month.")
            Short refMonth,

            @Option(shortName = 'i', longName = "billTypeId", description = "Filter's bill type id.")
            UUID billTypeId,

            @Option(shortName = 'e', longName = "paid", description = "Filter's paid option.")
            Boolean paid,

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = BudgetFilter.of(active, search, refYear, refMonth, billTypeId, paid);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "sequence"));
    }

    @Command(name = "budget-update", description = "Update budget's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The budget's active status.")
            Boolean active,

            @Option(shortName = 'p', longName = "paid", description = "The budget's paid status.")
            Boolean paid
    ) {
        return baseUpdate(BudgetPatchDto.of(active, paid), id);
    }

    @Command(name = "budget-update-sequence", description = "Update budget's sequence by id.")
    public String updateSequence(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'd', longName = "direction", description = "The sequence's direction.", required = true)
            @NotNull
            Direction direction
    ) {
        service.updateSequence(id, direction);
        return shellHelper.success("Budget with id: '%s' updated with success (sequence).".formatted(id));
    }

    @Command(name = "budget-swap-position", description = "Swap budget's position by id.")
    public String swapPosition(
            @Option(shortName = 'i', longName = "id", description = "The budget's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 't', longName = "targetSequence", description = "The target sequence.", required = true)
            @NotNull
            Short targetSequence
    ) {
        service.swapPosition(id, targetSequence);
        return shellHelper.success("Budget with id: '%s' updated with success (position).".formatted(id));
    }

    @Command(name = "budget-sum-month", description = "Get budget's sum by month.")
    public String getSumByMonth(
            @Option(shortName = 'a', longName = "active", description = "Filter's active option.")
            Boolean active,

            @Option(shortName = 's', longName = "search", description = "Filter's search option.")
            String search,

            @Option(shortName = 'y', longName = "refYear", description = "Filter's reference year.")
            Short refYear,

            @Option(shortName = 'm', longName = "refMonth", description = "Filter's reference month.")
            Short refMonth,

            @Option(shortName = 'b', longName = "billTypeId", description = "Filter's bill type id.")
            UUID billTypeId,

            @Option(shortName = 'p', longName = "paid", description = "Filter's paid option.")
            Boolean paid
    ) {
        return shellHelper.printTable(
                service.getSumByMonth(BudgetFilter.of(active, search, refYear, refMonth, billTypeId, paid)),
                EruTable.BUDGET_SUM_MONTH
        );
    }

    @Command(name = "budget-copy", description = "Copy budget.")
    public String copy(
            @Option(shortName = 'y', longName = "fromYear", description = "Copy from year.", required = true)
            @NotNull
            Short fromYear,

            @Option(shortName = 'm', longName = "fromMonth", description = "Copy from month.", required = true)
            @NotNull
            Short fromMonth,

            @Option(shortName = 'a', longName = "toYear", description = "To year.", required = true)
            @NotNull
            Short toYear,

            @Option(shortName = 'b', longName = "toMonth", description = "To month.", required = true)
            @NotNull
            Short toMonth
    ) {
        service.copy(CopyBudgetDto.of(fromYear, fromMonth, toYear, toMonth));
        return shellHelper.success("Budget copied from %d/%02d to %d/%02d.".formatted(fromYear, fromMonth, toYear, toMonth));
    }

    @Command(name = "budget-delete-all", description = "Delete all budgets by year and month.")
    public String deleteAll(
            @Option(shortName = 'y', longName = "refYear", description = "Reference year.", required = true)
            @NotNull
            Short refYear,

            @Option(shortName = 'm', longName = "refMonth", description = "Reference month.", required = true)
            @NotNull
            Short refMonth
    ) {
        service.deleteAll(refYear, refMonth);
        return shellHelper.success("All budgets for %d/%02d deleted.".formatted(refYear, refMonth));
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var billTypeItems = billTypeService.getItems(null);
        checkIfNotEmpty(billTypeItems).orElseThrow(Error.BILL_TYPE_REQUIRED);
        return SaveFlowParam.budget(billTypeItems);
    }
}
