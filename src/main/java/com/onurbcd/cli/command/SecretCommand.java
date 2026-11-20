package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.filter.SecretFilter;
import com.onurbcd.cli.dto.secret.SecretPatchDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.SecretService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.SECRET;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@Component
@CommandGroup(name = SECRET)
public class SecretCommand extends BaseCommand {

    public SecretCommand(SecretService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper) {
        super(service, flowBuilder, shellHelper, SECRET, EruTable.SECRET);
    }

    @Command(name = "secret-save", description = "Create or update a secret.")
    public String save(
            @Option(shortName = 'i', longName = "id", description = "The secret's id.")
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @Command(name = "secret-delete", description = "Delete secret by id.")
    public String delete(
            @Option(shortName = 'i', longName = "id", description = "The secret's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @Command(name = "secret-get", description = "Get secret by id.")
    public String get(
            @Option(shortName = 'i', longName = "id", description = "The secret's id.", required = true)
            @NotNull
            UUID id
    ) {
        return baseGet(id);
    }

    @Command(name = "secret-get-all", description = "Get secret's list.")
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
        var filter = SecretFilter.of(active, search);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @Command(name = "secret-update", description = "Update secret's status by id.")
    public String update(
            @Option(shortName = 'i', longName = "id", description = "The secret's id.", required = true)
            @NotNull
            UUID id,

            @Option(shortName = 'a', longName = "active", description = "The secret's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(SecretPatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        return SaveFlowParam.noArgs(FlowType.SECRET);
    }
}
