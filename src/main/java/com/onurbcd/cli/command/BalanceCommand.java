package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.balance.BalancePatchDto;
import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.enums.*;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.SaveFlowParam;
import com.onurbcd.cli.service.BalanceService;
import com.onurbcd.cli.service.CategoryService;
import com.onurbcd.cli.service.SourceService;
import com.onurbcd.cli.validator.Action;
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

import static com.onurbcd.cli.util.Constant.BALANCE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@ShellComponent
@ShellCommandGroup(BALANCE)
public class BalanceCommand extends BaseCommand {

    private final BalanceService service;
    private final SourceService sourceService;
    private final CategoryService categoryService;
    private final AdminProperties config;

    public BalanceCommand(BalanceService service, SourceService sourceService, CategoryService categoryService,
                          ComponentFlow.Builder flowBuilder, ShellHelper shellHelper, AdminProperties config) {

        super(service, flowBuilder, shellHelper, BALANCE, EruTable.BALANCE);
        this.service = service;
        this.sourceService = sourceService;
        this.categoryService = categoryService;
        this.config = config;
    }

    @ShellMethod(key = "balance-save", value = "Create or update a balance.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(id);
    }

    @ShellMethod(key = "balance-delete", value = "Delete balance by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "balance-get", value = "Get balance by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "balance-get-all", value = "Get balance's list.")
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

            @ShellOption(value = {"sourceId", "-i"}, help = "Filter's source id.", defaultValue = ShellOption.NULL)
            UUID sourceId,

            @ShellOption(value = {"categoryId", "-c"}, help = "Filter's category id.", defaultValue = ShellOption.NULL)
            UUID categoryId,

            @ShellOption(value = {"balanceType", "-b"}, help = "Filter's balance type.", defaultValue = ShellOption.NULL)
            BalanceType balanceType,

            @ShellOption(value = {"paymentType", "-t"}, help = "Filter's payemnt type.", defaultValue = ShellOption.NULL)
            PaymentType paymentType,

            @ShellOption(value = {"year", "-y"}, help = "Filter's year.", defaultValue = ShellOption.NULL)
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "Filter's month.", defaultValue = ShellOption.NULL)
            Short month,

            @ShellOption(value = {"day", "-g"}, help = "Filter's day.", defaultValue = ShellOption.NULL)
            Short day,

            @ShellOption(value = {"properties", "-p"}, help = "The page's sort properties.", defaultValue = ShellOption.NULL)
            String... properties
    ) {
        var filter = BalanceFilter.of(active, search, sourceId, categoryId, balanceType, paymentType).and(year, month, day);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "sequence"));
    }

    @ShellMethod(key = "balance-update", value = "Update balance's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The balance's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(BalancePatchDto.of(active), id);
    }

    @ShellMethod(key = "balance-update-sequence", value = "Update balance's sequence by id.")
    public String updateSequence(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"direction", "-d"}, help = "The sequence's direction.")
            @NotNull
            Direction direction
    ) {
        service.updateSequence(id, direction);
        return shellHelper.success("Balance with id: '%s' updated with success (sequence).".formatted(id));
    }

    @ShellMethod(key = "balance-swap-position", value = "Swap balance's position by id.")
    public String swapPosition(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"targetSequence", "-t"}, help = "The target sequence.")
            @NotNull
            Short targetSequence
    ) {
        service.swapPosition(id, targetSequence);
        return shellHelper.success("Balance with id: '%s' updated with success (position).".formatted(id));
    }

    @ShellMethod(key = "balance-sum", value = "Get balance's sum.")
    public String getSum(
            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search,

            @ShellOption(value = {"sourceId", "-i"}, help = "Filter's source id.", defaultValue = ShellOption.NULL)
            UUID sourceId,

            @ShellOption(value = {"categoryId", "-c"}, help = "Filter's category id.", defaultValue = ShellOption.NULL)
            UUID categoryId,

            @ShellOption(value = {"balanceType", "-b"}, help = "Filter's balance type.", defaultValue = ShellOption.NULL)
            BalanceType balanceType,

            @ShellOption(value = {"paymentType", "-t"}, help = "Filter's payemnt type.", defaultValue = ShellOption.NULL)
            PaymentType paymentType,

            @ShellOption(value = {"year", "-y"}, help = "Filter's year.", defaultValue = ShellOption.NULL)
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "Filter's month.", defaultValue = ShellOption.NULL)
            Short month,

            @ShellOption(value = {"day", "-g"}, help = "Filter's day.", defaultValue = ShellOption.NULL)
            Short day
    ) {
        return shellHelper.printTable(
                service.getSum(
                        BalanceFilter.of(active, search, sourceId, categoryId, balanceType, paymentType).and(year, month, day)),
                EruTable.BALANCE_SUM);
    }

    @Override
    protected SaveFlowParam preSaveFlow(@Nullable UUID id) {
        var sourceItems = sourceService.getItems(null);
        Action.checkIfNotEmpty(sourceItems).orElseThrow(Error.SOURCE_REQUIRED);
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(Error.CATEGORY_REQUIRED);
        return SaveFlowParam.balance(sourceItems, categoryItems, config.getFilesPath());
    }
}
