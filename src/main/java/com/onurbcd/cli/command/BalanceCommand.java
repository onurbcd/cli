package com.onurbcd.cli.command;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.balance.BalancePatchDto;
import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.enums.*;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.BalanceService;
import com.onurbcd.cli.service.CategoryService;
import com.onurbcd.cli.service.SourceService;
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

import static com.onurbcd.cli.util.Constant.BALANCE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = BALANCE)
public class BalanceCommand extends BaseCommand {

    private final BalanceService service;
    private final SourceService sourceService;
    private final CategoryService categoryService;
    private final AdminProperties config;

    public BalanceCommand(BalanceService service, SourceService sourceService, CategoryService categoryService,
                          ComponentFlow.Builder flowBuilder, ShellHelper shellHelper, AdminProperties config,
                          Validator validator) {

        super(service, flowBuilder, shellHelper, BALANCE, EruTable.BALANCE, validator);
        this.service = service;
        this.sourceService = sourceService;
        this.categoryService = categoryService;
        this.config = config;
    }

    @Command(name = "balance-save", description = "Create or update a balance.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "balance-delete", description = "Delete balance by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.", required = true)
            @NotNull UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "balance-get", description = "Get balance by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "balance-get-all", description = "Get balance's list.")
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

            @Option(shortName = 'i', longName = "sourceId", description = "Filter's source id.")
            UUID sourceId,

            @Option(shortName = 'c', longName = "categoryId", description = "Filter's category id.")
            UUID categoryId,

            @Option(shortName = 'b', longName = "balanceType", description = "Filter's balance type.")
            BalanceType balanceType,

            @Option(shortName = 't', longName = "paymentType", description = "Filter's payemnt type.")
            PaymentType paymentType,

            @Option(shortName = 'y', longName = "year", description = "Filter's year.")
            Short year,

            @Option(shortName = 'm', longName = "month", description = "Filter's month.")
            Short month,

            @Option(shortName = 'g', longName = "day", description = "Filter's day.")
            Short day,

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = BalanceFilter.of(active, search, sourceId, categoryId, balanceType, paymentType).and(year, month, day);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "sequence"));
    }

    @Command(name = "balance-update", description = "Update balance's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The balance's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(BalancePatchDto.of(active), id);
    }

    @Command(name = "balance-update-sequence", description = "Update balance's sequence by id.")
    public String updateSequence(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'd', longName = "direction", description = "The sequence's direction.", required = true)
            @NotNull
            Direction direction
    ) {
        service.updateSequence(id, direction);
        return shellHelper.success("Balance with id: '%s' updated with success (sequence).".formatted(id));
    }

    @Command(name = "balance-swap-position", description = "Swap balance's position by id.")
    public String swapPosition(
            @Option(shortName = 'i', longName = "id", description = "The balance's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 't', longName = "targetSequence", description = "The target sequence.", required = true)
            @NotNull
            Short targetSequence
    ) {
        service.swapPosition(id, targetSequence);
        return shellHelper.success("Balance with id: '%s' updated with success (position).".formatted(id));
    }

    @Command(name = "balance-sum", description = "Get balance's sum.")
    public String getSum(
            @Option(shortName = 'a', longName = "active", description = "Filter's active option.")
            Boolean active,

            @Option(shortName = 'f', longName = "search", description = "Filter's search option.")
            String search,

            @Option(shortName = 'i', longName = "sourceId", description = "Filter's source id.")
            UUID sourceId,

            @Option(shortName = 'c', longName = "categoryId", description = "Filter's category id.")
            UUID categoryId,

            @Option(shortName = 'b', longName = "balanceType", description = "Filter's balance type.")
            BalanceType balanceType,

            @Option(shortName = 't', longName = "paymentType", description = "Filter's payemnt type.")
            PaymentType paymentType,

            @Option(shortName = 'y', longName = "year", description = "Filter's year.")
            Short year,

            @Option(shortName = 'm', longName = "month", description = "Filter's month.")
            Short month,

            @Option(shortName = 'g', longName = "day", description = "Filter's day.")
            Short day
    ) {
        return shellHelper.printTable(
                service.getSum(
                        BalanceFilter.of(active, search, sourceId, categoryId, balanceType, paymentType).and(year, month, day)),
                EruTable.BALANCE_SUM);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var sourceItems = sourceService.getItems(null);
        Action.checkIfNotEmpty(sourceItems).orElseThrow(Error.SOURCE_REQUIRED);
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(Error.CATEGORY_REQUIRED);
        return SaveFlowParam.balance(sourceItems, categoryItems, config.getFilesPath());
    }
}
