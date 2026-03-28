package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.SOURCE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@ShellComponent
@ShellCommandGroup(SOURCE)
public class SourceCommand extends BaseCommand {

    private final SourceService service;
    private final IncomeSourceService incomeSourceService;

    public SourceCommand(SourceService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper,
                         IncomeSourceService incomeSourceService) {

        super(service, flowBuilder, shellHelper, SOURCE, EruTable.SOURCE);
        this.service = service;
        this.incomeSourceService = incomeSourceService;
    }

    @ShellMethod(key = "source-save", value = "Create or update a source.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @ShellMethod(key = "source-delete", value = "Delete source by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "source-get", value = "Get source by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "source-get-all", value = "Get source's list.")
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

            @ShellOption(value = {"incomeSourceId", "-i"}, help = "Filter's income source id.", defaultValue = ShellOption.NULL)
            UUID incomeSourceId,

            @ShellOption(value = {"sourceType", "-t"}, help = "Filter's source type.", defaultValue = ShellOption.NULL)
            SourceType sourceType,

            @ShellOption(value = {"currencyType", "-c"}, help = "Filter's currency type.", defaultValue = ShellOption.NULL)
            CurrencyType currencyType,

            @ShellOption(value = {"properties", "-p"}, help = "The page's sort properties.", defaultValue = ShellOption.NULL)
            String... properties
    ) {
        var filter = SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @ShellMethod(key = "source-update", value = "Update source's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The source's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(SourcePatchDto.of(active), id);
    }

    @ShellMethod(key = "source-balance-sum", value = "Get source's balance sum.")
    public String getBalanceSum(
            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search,

            @ShellOption(value = {"incomeSourceId", "-i"}, help = "Filter's income source id.", defaultValue = ShellOption.NULL)
            UUID incomeSourceId,

            @ShellOption(value = {"sourceType", "-t"}, help = "Filter's source type.", defaultValue = ShellOption.NULL)
            SourceType sourceType,

            @ShellOption(value = {"currencyType", "-c"}, help = "Filter's currency type.", defaultValue = ShellOption.NULL)
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
