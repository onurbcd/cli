package com.onurbcd.cli.command;

import com.onurbcd.cli.annotation.MaxYear;
import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.BillService;
import com.onurbcd.cli.service.BudgetService;
import com.onurbcd.cli.service.SourceService;
import com.onurbcd.cli.util.DateUtil;
import com.onurbcd.cli.validator.Action;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.BILL;

@Component
@CommandGroup(name = BILL)
public class BillCommand extends BaseCommand {

    private final BillService service;
    private final BudgetService budgetService;
    private final SourceService sourceService;
    private final AdminProperties config;

    public BillCommand(BillService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                       BudgetService budgetService, SourceService sourceService, AdminProperties config) {

        super(service, flowBuilder, shellHelper, BILL, EruTable.BILL);
        this.service = service;
        this.budgetService = budgetService;
        this.sourceService = sourceService;
        this.config = config;
    }

    @Command(name = "bill-save", description = "Update a bill.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The bill's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseSave(CommandParam.of(FlowType.BILL, id));
    }

    @Command(name = "bill-get", description = "Get bill by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The bill's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "bill-open", description = "Open a bill.")
    public String openBill(
            @Option(shortName = 'y', longName = "year", description = "The reference year.")
            @MinYear
            @MaxYear
            Short year,

            @Option(shortName = 'm', longName = "month", description = "The reference month.")
            @Min(1)
            @Max(12)
            Short month
    ) {
        return baseSave(CommandParam.of(FlowType.BILL_OPEN, year, month));
    }

    @Command(name = "bill-close", description = "Close a bill.")
    public String closeBill(
            @Option(shortName = 'y', longName = "year", description = "The reference year.")
            @MinYear
            @MaxYear
            Short year,

            @Option(shortName = 'm', longName = "month", description = "The reference month.")
            @Min(1)
            @Max(12)
            Short month
    ) {
        return baseSave(CommandParam.of(FlowType.BILL_CLOSE, year, month));
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        switch (params.getFlowType()) {
            case BILL -> {
                var dto = (BillDto) params.getDto();
                Short year = (short) dto.getReferenceDayCalendarDate().getYear();
                Short month = (short) dto.getReferenceDayCalendarDate().getMonthValue();
                var budgetItems = budgetService.getMonthlyBudget(year, month);
                Action.checkIfNotEmpty(budgetItems).orElseThrow(Error.BUDGET_REQUIRED, month, year);
                var sourceItems = sourceService.getItems(null);
                Action.checkIfNotEmpty(sourceItems).orElseThrow(Error.SOURCE_REQUIRED);
                return SaveFlowParam.bill(budgetItems, sourceItems, config.getFilesPath());
            }
            case BILL_OPEN -> {
                var year = DateUtil.orCurrentYear(params.getYear());
                var month = DateUtil.orCurrentMonth(params.getMonth());
                var budgetItems = budgetService.getMonthlyBudget(year, month);
                Action.checkIfNotEmpty(budgetItems).orElseThrow(Error.BUDGET_REQUIRED, month, year);
                return SaveFlowParam.billOpen(budgetItems, config.getFilesPath());
            }
            case BILL_CLOSE -> {
                var year = DateUtil.orCurrentYear(params.getYear());
                var month = DateUtil.orCurrentMonth(params.getMonth());
                var billItems = service.getOpenBills(year, month);
                Action.checkIfNotEmpty(billItems).orElseThrow(Error.OPEN_BILLS_REQUIRED, month, year);
                var sourceItems = sourceService.getItems(null);
                Action.checkIfNotEmpty(sourceItems).orElseThrow(Error.SOURCE_REQUIRED);
                return SaveFlowParam.billClose(billItems, sourceItems, config.getFilesPath());
            }
            default -> throw new IllegalStateException("Unexpected value: " + params.getFlowType());
        }
    }
}
