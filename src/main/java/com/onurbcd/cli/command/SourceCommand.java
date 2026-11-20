package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.dto.source.SourcePatchDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.IncomeSourceService;
import com.onurbcd.cli.service.SourceService;
import com.onurbcd.cli.validator.Action;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.SOURCE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = SOURCE)
public class SourceCommand extends BaseCommand {

    private final SourceService service;
    private final IncomeSourceService incomeSourceService;

    public SourceCommand(SourceService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                         IncomeSourceService incomeSourceService) {

        super(service, flowBuilder, shellHelper, SOURCE, EruTable.SOURCE);
        this.service = service;
        this.incomeSourceService = incomeSourceService;
    }

    @Command(name = "source-save", description = "Create or update a source.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The source's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "source-delete", description = "Delete source by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The source's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "source-get", description = "Get source by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The source's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "source-get-all", description = "Get source's list.")
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

            @Option(shortName = 'i', longName = "incomeSourceId", description = "Filter's income source id.")
            UUID incomeSourceId,

            @Option(shortName = 't', longName = "sourceType", description = "Filter's source type.")
            SourceType sourceType,

            @Option(shortName = 'c', longName = "currencyType", description = "Filter's currency type.")
            CurrencyType currencyType,

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @Command(name = "source-update", description = "Update source's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The source's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The source's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(SourcePatchDto.of(active), id);
    }

    @Command(name = "source-balance-sum", description = "Get source's balance sum.")
    public String getBalanceSum(
            @Option(shortName = 'a', longName = "active", description = "Filter's active option.")
            Boolean active,

            @Option(shortName = 'f', longName = "search", description = "Filter's search option.")
            String search,

            @Option(shortName = 'i', longName = "incomeSourceId", description = "Filter's income source id.")
            UUID incomeSourceId,

            @Option(shortName = 't', longName = "sourceType", description = "Filter's source type.")
            SourceType sourceType,

            @Option(shortName = 'c', longName = "currencyType", description = "Filter's currency type.")
            CurrencyType currencyType
    ) {
        var filter = SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType);
        var balanceSumDto = service.getBalanceSum(filter);
        return shellHelper.printTable(Set.of(balanceSumDto), EruTable.SOURCE_BALANCE_SUM);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        var incomeSourceItems = incomeSourceService.getItems(null);
        Action.checkIfNotEmpty(incomeSourceItems).orElseThrow(Error.INCOME_SOURCE_REQUIRED);
        return SaveFlowParam.source(incomeSourceItems);
    }
}
