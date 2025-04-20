package com.onurbcd.eruservice.command;

import com.onurbcd.eruservice.command.helper.ShellHelper;
import com.onurbcd.eruservice.dto.budget.BudgetDto;
import com.onurbcd.eruservice.dto.budget.BudgetSaveDto;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.service.BudgetService;
import lombok.RequiredArgsConstructor;
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
                result.get("refYear", String.class),
                result.get("refMonth", String.class),
                result.get("billTypeId", String.class),
                result.get("amount", String.class),
                result.get("paid", Boolean.class)
        );
    }
}
