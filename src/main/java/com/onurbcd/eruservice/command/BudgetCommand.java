package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.command.enums.EruTable;
import com.onurbcd.eruservice.command.helper.ShellHelper;
import com.onurbcd.eruservice.dto.budget.BudgetDto;
import com.onurbcd.eruservice.dto.budget.BudgetPatchDto;
import com.onurbcd.eruservice.dto.budget.BudgetSaveDto;
import com.onurbcd.eruservice.dto.enums.Direction;
import com.onurbcd.eruservice.dto.filter.BudgetFilter;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.service.BudgetService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.eruservice.command.CommandConstants.NAME;
import static com.onurbcd.eruservice.command.CommandConstants.NAME_LABEL;

@ShellComponent
@ShellCommandGroup("Budget")
@RequiredArgsConstructor
public class BudgetCommand {

    private final BudgetService service;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final BillTypeService billTypeService;

    @ShellMethod(key = "budget-save", value = "Create or update a budget.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var budgetSaveDto = runSaveFlow(id);
        var returnId = service.save(budgetSaveDto, id);
        return "Budget with id: '%s' saved with success.".formatted(returnId);
    }

    @ShellMethod(key = "budget-delete", value = "Delete budget by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id
    ) {
        service.delete(id);
        return "Budget with id: '%s' deleted with success.".formatted(id);
    }

    @ShellMethod(key = "budget-get", value = "Get budget by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The budget's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return shellHelper.printJson(service.getById(id));
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

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "name")
            String property,

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
            Boolean paid
    ) {
        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        BudgetFilter.of(active, search, refYear, refMonth, billTypeId, paid)
                ),
                EruTable.BUDGET
        );
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
        service.update(BudgetPatchDto.of(active, paid), id);
        return String.format("Budget with id: '%s' updated with success.", id);
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
        return "Budget with id: '%s' updated with success (sequence).".formatted(id);
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
        return "Budget with id: '%s' updated with success (position).".formatted(id);
    }

    private BudgetSaveDto runSaveFlow(@Nullable UUID id) {
        var budget = Optional.ofNullable(id).map(i -> (BudgetDto) service.getById(i)).orElse(null);

        var name = Optional.ofNullable(budget).map(BudgetDto::getName).orElse(null);
        var refYear = Optional.ofNullable(budget).map(BudgetDto::getRefYear).map(ref -> Short.toString(ref)).orElse(null);
        var refMonth = Optional.ofNullable(budget).map(BudgetDto::getRefMonth).map(ref -> Short.toString(ref)).orElse(null);
        var billType = Optional.ofNullable(budget).map(BudgetDto::getBillTypeName).orElse(null);
        var amount = Optional.ofNullable(budget).map(BudgetDto::getAmount).map(BigDecimal::toString).orElse(null);
        var paid = Optional.ofNullable(budget).map(BudgetDto::getPaid).orElse(Boolean.FALSE);

        var billTypeItems = billTypeService.getItems(null);

        var result = flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(name).and()
                .withStringInput("refYear").name("* Reference Year:").defaultValue(refYear).and()
                .withStringInput("refMonth").name("* Reference Month:").defaultValue(refMonth).and()
                .withSingleItemSelector("billTypeId").name("* Bill Type:").selectItems(billTypeItems).defaultSelect(billType).max(billTypeItems.size()).and()
                .withStringInput("amount").name("* Amount:").defaultValue(amount).and()
                .withConfirmationInput("paid").name("* Paid:").defaultValue(paid).and()
                .build().run().getContext();

        return BudgetSaveDto.of(
                result.get(NAME, String.class),
                Optional.ofNullable(budget).map(BudgetDto::isActive).orElse(Boolean.TRUE),
                Optional.ofNullable(budget).map(BudgetDto::getSequence).orElse(null),
                result.get("refYear", String.class),
                result.get("refMonth", String.class),
                result.get("billTypeId", String.class),
                result.get("amount", String.class),
                result.get("paid", Boolean.class)
        );
    }
}
