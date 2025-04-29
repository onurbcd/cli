package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.command.helper.ShellHelper;
import com.onurbcd.eruservice.dto.filter.IncomeSourceFilter;
import com.onurbcd.eruservice.dto.incomesource.IncomeSourcePatchDto;
import com.onurbcd.eruservice.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.eruservice.service.impl.IncomeSourceService;
import com.onurbcd.eruservice.util.Extension;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.UUID;

@ShellComponent
@ShellCommandGroup("Income Source")
@RequiredArgsConstructor
@ExtensionMethod({Extension.class})
public class IncomeSourceCommand {

    private final IncomeSourceService service;
    private final ShellHelper shellHelper;

    @ShellMethod(key = "income-source-save", value = "Create or update an income source.")
    public String save(
            @ShellOption(value = {"name", "-n"}, help = "The unique name of the income source.")
            @NotBlank
            @Size(min = 3, max = 50)
            String name,

            @ShellOption(value = {"active", "-a"}, help = "The income source's status.", defaultValue = "true")
            Boolean active,

            @ShellOption(value = {"id", "-i"}, help = "The income source's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var returnId = service.save(IncomeSourceSaveDto.of(name.normalizeSpace(), active), id);
        return String.format("Income Source with id: '%s' saved with success.", returnId);
    }

    @ShellMethod(key = "income-source-delete", value = "Delete income source by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The income source's id.")
            @NotNull
            UUID id
    ) {
        service.delete(id);
        return String.format("Income Source with id: '%s' deleted with success.", id);
    }

    @ShellMethod(key = "income-source-get", value = "Get income source by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The income source's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return shellHelper.printJson(service.getById(id));
    }

    @ShellMethod(key = "income-source-get-all", value = "Get income source's list.")
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
                        IncomeSourceFilter.of(active, search)
                ),
                EruTable.INCOME_SOURCE
        );
    }

    @ShellMethod(key = "income-source-update", value = "Update income-source's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The income-source's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The income-source's status.", defaultValue = "false")
            Boolean active
    ) {
        service.update(IncomeSourcePatchDto.of(active), id);
        return String.format("Income Source with id: '%s' updated with success.", id);
    }
}
