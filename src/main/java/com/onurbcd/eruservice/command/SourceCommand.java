package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.dto.enums.CurrencyType;
import com.onurbcd.eruservice.dto.enums.SourceType;
import com.onurbcd.eruservice.dto.filter.SourceFilter;
import com.onurbcd.eruservice.dto.source.SourceDto;
import com.onurbcd.eruservice.dto.source.SourcePatchDto;
import com.onurbcd.eruservice.dto.source.SourceSaveDto;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.service.impl.IncomeSourceService;
import com.onurbcd.eruservice.util.EnumUtil;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.eruservice.command.CommandConstants.NAME;
import static com.onurbcd.eruservice.command.CommandConstants.NAME_LABEL;

@ShellComponent
@ShellCommandGroup("Source")
@RequiredArgsConstructor
public class SourceCommand {

    private final SourceService service;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final IncomeSourceService incomeSourceService;

    @ShellMethod(key = "source-save", value = "Create or update a source.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var sourceSaveDto = runSaveFlow(id);
        var returnId = service.save(sourceSaveDto, id);
        return "Source with id: '%s' saved with success.".formatted(returnId);
    }

    @ShellMethod(key = "source-delete", value = "Delete source by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id
    ) {
        service.delete(id);
        return "Source with id: '%s' deleted with success.".formatted(id);
    }

    @ShellMethod(key = "source-get", value = "Get source by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return shellHelper.printJson(service.getById(id));
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

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "name")
            String property,

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
        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType)
                ),
                EruTable.SOURCE
        );
    }

    @ShellMethod(key = "source-update", value = "Update source's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The source's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The source's status.", defaultValue = "false")
            Boolean active
    ) {
        service.update(SourcePatchDto.of(active), id);
        return String.format("Source with id: '%s' updated with success.", id);
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
        var balanceSumDto = service.getBalanceSum(SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType));
        return shellHelper.printTable(Set.of(balanceSumDto), EruTable.SOURCE_BALANCE_SUM);
    }

    private SourceSaveDto runSaveFlow(@Nullable UUID id) {
        var source = Optional.ofNullable(id).map(i -> (SourceDto) service.getById(i)).orElse(null);

        var name = Optional.ofNullable(source).map(SourceDto::getName).orElse(null);
        var incomeSource = Optional.ofNullable(source).map(SourceDto::getIncomeSourceName).orElse(null);
        var sourceType = Optional.ofNullable(source).map(SourceDto::getSourceType).map(SourceType::name).orElse(null);
        var currencyType = Optional.ofNullable(source).map(SourceDto::getCurrencyType).map(CurrencyType::name).orElse(null);
        var balance = Optional.ofNullable(source).map(SourceDto::getBalance).map(BigDecimal::toString).orElse(null);

        var incomeSourceItems = incomeSourceService.getItems(null);
        var sourceTypeItems = EnumUtil.getItems(SourceType.values());
        var currencyTypeItems = EnumUtil.getItems(CurrencyType.values());

        var result = flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(name).and()
                .withSingleItemSelector("incomeSourceId").name("* Income Source").selectItems(incomeSourceItems).defaultSelect(incomeSource).max(incomeSourceItems.size()).and()
                .withSingleItemSelector("sourceType").name("* Source Type").selectItems(sourceTypeItems).defaultSelect(sourceType).max(sourceTypeItems.size()).and()
                .withSingleItemSelector("currencyType").name("* Currency Type").selectItems(currencyTypeItems).defaultSelect(currencyType).max(currencyTypeItems.size()).and()
                .withStringInput("balance").name("* Balance").defaultValue(balance).and()
                .build().run().getContext();

        return SourceSaveDto.of(result.get(NAME, String.class),
                Optional.ofNullable(source).map(SourceDto::isActive).orElse(Boolean.TRUE),
                result.get("incomeSourceId", String.class), result.get("sourceType", String.class),
                result.get("currencyType", String.class), result.get("balance", String.class));
    }
}
