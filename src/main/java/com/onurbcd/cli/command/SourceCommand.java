package com.onurbcd.cli.command;

import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.dto.source.SourcePatchDto;
import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.factory.FlowFactory;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.SourceSaveFlowParam;
import com.onurbcd.cli.service.IncomeSourceService;
import com.onurbcd.cli.service.SourceService;
import com.onurbcd.cli.util.FlowUtil;
import com.onurbcd.cli.util.ValidatorUtil;
import com.onurbcd.cli.validator.Action;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

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
            @ShellOption(value = { "id", "-i" }, help = "The source's id.", defaultValue = ShellOption.NULL) UUID id) {

        var sourceSaveDto = runSaveFlow(id);

        if (sourceSaveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(sourceSaveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = service.save(sourceSaveDto, id);
        return shellHelper.success("Source with id: '%s' saved with success.".formatted(returnId));
    }

    @ShellMethod(key = "source-delete", value = "Delete source by id.")
    public String delete(@ShellOption(value = { "id", "-i" }, help = "The source's id.") @NotNull UUID id) {
        service.delete(id);
        return shellHelper.success("Source with id: '%s' deleted with success.".formatted(id));
    }

    @ShellMethod(key = "source-get", value = "Get source by id.")
    public String get(@ShellOption(value = { "id", "-i" }, help = "The source's id.") @NotNull UUID id)
            throws JsonProcessingException {

        return shellHelper.printJson(service.getById(id));
    }

    @ShellMethod(key = "source-get-all", value = "Get source's list.")
    public String getAll(
            @ShellOption(value = { "pageNumber",
                    "-n" }, help = "The page's number.", defaultValue = "1") @Min(1) Integer pageNumber,

            @ShellOption(value = { "pageSize",
                    "-s" }, help = "The page's size.", defaultValue = "10") @Min(1) Integer pageSize,

            @ShellOption(value = { "direction",
                    "-d" }, help = "The page's sort direction.", defaultValue = "ASC") Sort.Direction direction,

            @ShellOption(value = { "property",
                    "-p" }, help = "The page's sort property.", defaultValue = "name") String property,

            @ShellOption(value = { "active",
                    "-a" }, help = "Filter's active option.", defaultValue = ShellOption.NULL) Boolean active,

            @ShellOption(value = { "search",
                    "-f" }, help = "Filter's search option.", defaultValue = ShellOption.NULL) String search,

            @ShellOption(value = { "incomeSourceId",
                    "-i" }, help = "Filter's income source id.", defaultValue = ShellOption.NULL) UUID incomeSourceId,

            @ShellOption(value = { "sourceType",
                    "-t" }, help = "Filter's source type.", defaultValue = ShellOption.NULL) SourceType sourceType,

            @ShellOption(value = { "currencyType",
                    "-c" }, help = "Filter's currency type.", defaultValue = ShellOption.NULL) CurrencyType currencyType) {

        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType)),
                EruTable.SOURCE);
    }

    @ShellMethod(key = "source-update", value = "Update source's status by id.")
    public String update(@ShellOption(value = { "id", "-i" }, help = "The source's id.") @NotNull UUID id,

            @ShellOption(value = { "active",
                    "-a" }, help = "The source's status.", defaultValue = "false") Boolean active) {

        service.update(SourcePatchDto.of(active), id);
        return shellHelper.success("Source with id: '%s' updated with success.".formatted(id));
    }

    @ShellMethod(key = "source-balance-sum", value = "Get source's balance sum.")
    public String getBalanceSum(
            @ShellOption(value = { "active",
                    "-a" }, help = "Filter's active option.", defaultValue = ShellOption.NULL) Boolean active,

            @ShellOption(value = { "search",
                    "-f" }, help = "Filter's search option.", defaultValue = ShellOption.NULL) String search,

            @ShellOption(value = { "incomeSourceId",
                    "-i" }, help = "Filter's income source id.", defaultValue = ShellOption.NULL) UUID incomeSourceId,

            @ShellOption(value = { "sourceType",
                    "-t" }, help = "Filter's source type.", defaultValue = ShellOption.NULL) SourceType sourceType,

            @ShellOption(value = { "currencyType",
                    "-c" }, help = "Filter's currency type.", defaultValue = ShellOption.NULL) CurrencyType currencyType) {

        var balanceSumDto = service
                .getBalanceSum(SourceFilter.of(active, search, incomeSourceId, sourceType, currencyType));

        return shellHelper.printTable(Set.of(balanceSumDto), EruTable.SOURCE_BALANCE_SUM);
    }

    @Nullable
    private SourceSaveDto runSaveFlow(@Nullable UUID id) {
        var incomeSourceItems = incomeSourceService.getItems(null);
        Action.checkIfNotEmpty(incomeSourceItems).orElseThrow(Error.INCOME_SOURCE_REQUIRED);
        var source = Optional.ofNullable(id).map(i -> (SourceDto) service.getById(i)).orElse(null);
        var params = SourceSaveFlowParam.of(source, incomeSourceItems);
        var flowResult = FlowUtil.runFlowSafely(FlowFactory.createSourceSaveFlow(flowBuilder, params));
        return flowResult != null ? SourceSaveDto.of(flowResult.getContext(), source) : null;
    }
}
