package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.dto.filter.SecretFilter;
import com.onurbcd.eruservice.dto.secret.SecretDto;
import com.onurbcd.eruservice.dto.secret.SecretPatchDto;
import com.onurbcd.eruservice.dto.secret.SecretSaveDto;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.factory.FlowFactory;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.SecretSaveFlowParam;
import com.onurbcd.eruservice.service.SecretService;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.ValidatorUtil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.OPERATION_CANCELLED;

@ShellComponent
@ShellCommandGroup("Secret")
@RequiredArgsConstructor
public class SecretCommand {

    private final SecretService service;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;

    @ShellMethod(key = "secret-save", value = "Create or update a secret.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var secretSaveDto = runSaveFlow(id);

        if (secretSaveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(secretSaveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = service.save(secretSaveDto, id);
        return shellHelper.success("Secret with id: '%s' saved with success.".formatted(returnId));
    }

    @ShellMethod(key = "secret-delete", value = "Delete secret by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id
    ) {
        service.delete(id);
        return shellHelper.success("Secret with id: '%s' deleted with success.".formatted(id));
    }

    @ShellMethod(key = "secret-get", value = "Get secret by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return shellHelper.printJson(service.getById(id));
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

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "name")
            String property,

            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search
    ) {
        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        SecretFilter.of(active, search)
                ),
                EruTable.SECRET
        );
    }

    @ShellMethod(key = "secret-update", value = "Update secret's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The secret's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The secret's status.", defaultValue = "false")
            Boolean active
    ) {
        service.update(SecretPatchDto.of(active), id);
        return shellHelper.success("Secret with id: '%s' updated with success.".formatted(id));
    }

    @Nullable
    private SecretSaveDto runSaveFlow(@Nullable UUID id) {
        var secret = (SecretDto) Optional.ofNullable(id).map(service::getById).orElse(null);
        var params = SecretSaveFlowParam.of(secret);
        var flow = FlowFactory.createSecretSaveFlow(flowBuilder, params);
        var result = FlowUtil.runFlowSafely(flow);
        return result != null ? SecretSaveDto.of(result.getContext(), secret) : null;
    }
}
