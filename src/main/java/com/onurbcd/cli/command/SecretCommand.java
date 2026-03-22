package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.dto.filter.SecretFilter;
import com.onurbcd.cli.dto.secret.SecretPatchDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.CommandParam;
import com.onurbcd.cli.model.SaveFlowParam;
import com.onurbcd.cli.service.SecretService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.SECRET;
import static com.onurbcd.cli.util.ParamUtil.getSortProps;

@ShellComponent
@ShellCommandGroup(SECRET)
public class SecretCommand extends BaseCommand {

    public SecretCommand(SecretService service, ComponentFlow.Builder flowBuilder, ShellHelper shellHelper) {
        super(service, flowBuilder, shellHelper, SECRET, EruTable.SECRET);
    }

    @ShellMethod(key = "secret-save", value = "Create or update a secret.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        return baseSave(CommandParam.of(id));
    }

    @ShellMethod(key = "secret-delete", value = "Delete secret by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id
    ) {
        return baseDelete(id);
    }

    @ShellMethod(key = "secret-get", value = "Get secret by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return baseGet(id);
    }

    @ShellMethod(key = "secret-get-all", value = "Get secret's list.")
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

            @ShellOption(value = {"properties", "-p"}, help = "The page's sort properties.", defaultValue = ShellOption.NULL)
            String... properties
    ) {
        var filter = SecretFilter.of(active, search);
        return baseGetAll(filter, pageNumber, pageSize, direction, getSortProps(properties, "name"));
    }

    @ShellMethod(key = "secret-update", value = "Update secret's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The secret's status.", defaultValue = "false")
            Boolean active
    ) {
        return baseUpdate(SecretPatchDto.of(active), id);
    }

    @Override
    protected SaveFlowParam preSaveFlow(CommandParam params) {
        return SaveFlowParam.noArgs(FlowType.SECRET);
    }
}
