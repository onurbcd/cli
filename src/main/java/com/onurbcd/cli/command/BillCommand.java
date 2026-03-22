package com.onurbcd.cli.command;

import com.onurbcd.cli.annotation.MaxYear;
import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.CommandParam;
import com.onurbcd.cli.model.SaveFlowParam;
import com.onurbcd.cli.service.BillService;
import com.onurbcd.cli.service.BudgetService;
import com.onurbcd.cli.util.DateUtil;
import com.onurbcd.cli.validator.Action;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import static com.onurbcd.cli.util.Constant.BILL;

@ShellComponent
@ShellCommandGroup(BILL)
public class BillCommand extends BaseCommand {

    private final BudgetService budgetService;
    private final AdminProperties config;

    public BillCommand(BillService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                       BudgetService budgetService, AdminProperties config) {

        super(service, flowBuilder, shellHelper, BILL, EruTable.BILL);
        this.budgetService = budgetService;
        this.config = config;
    }

    @ShellMethod(key = "bill-open", value = "Open a bill.")
    public String openBill(
            @ShellOption(value = {"year", "-y"}, help = "The reference year.", defaultValue = ShellOption.NULL)
            @MinYear
            @MaxYear
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "The reference month.", defaultValue = ShellOption.NULL)
            @Min(1)
            @Max(12)
            Short month
    ) {
        return baseSave(CommandParam.of(year, month));
    }

    // 7c0383ed-57d4-49d2-bdce-79fd5f4f10fc

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var year = DateUtil.orCurrentYear(params.getYear());
        var month = DateUtil.orCurrentMonth(params.getMonth());
        var budgetItems = budgetService.getMonthlyBudget(year, month);
        Action.checkIfNotEmpty(budgetItems).orElseThrow(Error.BUDGET_REQUIRED, month, year);
        return SaveFlowParam.billOpen(budgetItems, config.getFilesPath());
    }
}
