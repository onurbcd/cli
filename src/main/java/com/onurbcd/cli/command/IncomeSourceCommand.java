package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.filter.IncomeSourceFilter;
import com.onurbcd.cli.dto.incomesource.IncomeSourcePatchDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.IncomeSourceService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.INCOME_SOURCE;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = INCOME_SOURCE)
public class IncomeSourceCommand extends BaseCommand {

    public IncomeSourceCommand(IncomeSourceService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper) {
        super(service, flowBuilder, shellHelper, INCOME_SOURCE, EruTable.INCOME_SOURCE);
    }

    @Command(name = "income-source-save", description = "Create or update an income source.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The income source's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "income-source-delete", description = "Delete income source by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The income source's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "income-source-get", description = "Get income source by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The income source's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "income-source-get-all", description = "Get income source's list.")
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

            @Option(shortName = 'p', longName = "properties", description = "The page's sort properties.")
            String... properties
    ) {
        var filter = IncomeSourceFilter.of(active, search);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @Command(name = "income-source-update", description = "Update income-source's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The income-source's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The income-source's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(IncomeSourcePatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        return SaveFlowParam.noArgs(FlowType.INCOME_SOURCE);
    }
}
