package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.dto.balance.BalancePatchDto;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.filter.BalanceFilter;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.factory.FlowFactory;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.BalanceSaveFlowParam;
import com.onurbcd.eruservice.service.BalanceService;
import com.onurbcd.eruservice.service.CategoryService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.ValidatorUtil;
import com.onurbcd.eruservice.validator.Action;
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
@ShellCommandGroup("Balance")
@RequiredArgsConstructor
public class BalanceCommand {

    private final BalanceService service;
    private final SourceService sourceService;
    private final CategoryService categoryService;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final AdminProperties config;

    @ShellMethod(key = "balance-save", value = "Create or update a balance.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var balanceSaveDto = runSaveFlow(id);

        if (balanceSaveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(balanceSaveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = service.save(balanceSaveDto, id);
        return shellHelper.success("Balance with id: '%s' saved with success.".formatted(returnId));
    }

    @ShellMethod(key = "balance-delete", value = "Delete balance by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull UUID id
    ) {
        service.delete(id);
        return shellHelper.success("Balance with id: '%s' deleted with success.".formatted(id));
    }

    @ShellMethod(key = "balance-get", value = "Get balance by id.")
    public String get(@ShellOption(value = {"id", "-i"}, help = "The balance's id.") @NotNull UUID id)
            throws JsonProcessingException {

        return shellHelper.printJson(service.getById(id));
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

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "sequence")
            String property,

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

            @ShellOption(value = {"year", "-y"}, help = "Filter's year.", defaultValue = ShellOption.NULL)
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "Filter's month.", defaultValue = ShellOption.NULL)
            Short month,

            @ShellOption(value = {"day", "-g"}, help = "Filter's day.", defaultValue = ShellOption.NULL)
            Short day
    ) {
        return shellHelper.printTable(
                service.getAll(PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        BalanceFilter.of(active, search, sourceId, categoryId, balanceType).and(year, month, day)),
                EruTable.BALANCE);
    }

    @ShellMethod(key = "balance-update", value = "Update balance's status by id.")
    public String update(@ShellOption(value = {"id", "-i"}, help = "The balance's id.") @NotNull UUID id,

                         @ShellOption(value = {"active",
                                 "-a"}, help = "The balance's status.", defaultValue = "false") Boolean active) {

        service.update(BalancePatchDto.of(active), id);
        return shellHelper.success("Balance with id: '%s' updated with success.".formatted(id));
    }

    @ShellMethod(key = "balance-update-sequence", value = "Update balance's sequence by id.")
    public String updateSequence(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.") @NotNull UUID id,

            @ShellOption(value = {"direction",
                    "-d"}, help = "The sequence's direction.") @NotNull Direction direction) {

        service.updateSequence(id, direction);
        return shellHelper.success("Balance with id: '%s' updated with success (sequence).".formatted(id));
    }

    @ShellMethod(key = "balance-swap-position", value = "Swap balance's position by id.")
    public String swapPosition(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.") @NotNull UUID id,

            @ShellOption(value = {"targetSequence",
                    "-t"}, help = "The target sequence.") @NotNull Short targetSequence) {

        service.swapPosition(id, targetSequence);
        return shellHelper.success("Balance with id: '%s' updated with success (position).".formatted(id));
    }

    @ShellMethod(key = "balance-sum", value = "Get balance's sum.")
    public String getSum(
            @ShellOption(value = {"active",
                    "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL) Boolean active,

            @ShellOption(value = {"search",
                    "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL) String search,

            @ShellOption(value = {"sourceId",
                    "-i"}, help = "Filter's source id.", defaultValue = ShellOption.NULL) UUID sourceId,

            @ShellOption(value = {"categoryId",
                    "-c"}, help = "Filter's category id.", defaultValue = ShellOption.NULL) UUID categoryId,

            @ShellOption(value = {"balanceType",
                    "-b"}, help = "Filter's balance type.", defaultValue = ShellOption.NULL) BalanceType balanceType,

            @ShellOption(value = {"year", "-y"}, help = "Filter's year.", defaultValue = ShellOption.NULL) Short year,

            @ShellOption(value = {"month",
                    "-m"}, help = "Filter's month.", defaultValue = ShellOption.NULL) Short month,

            @ShellOption(value = {"day", "-g"}, help = "Filter's day.", defaultValue = ShellOption.NULL) Short day) {

        return shellHelper.printTable(
                service.getSum(
                        BalanceFilter.of(active, search, sourceId, categoryId, balanceType).and(year, month, day)),
                EruTable.BALANCE_SUM);
    }

    @Nullable
    private BalanceSaveDto runSaveFlow(@Nullable UUID id) {
        var sourceItems = sourceService.getItems(null);
        Action.checkIfNotEmpty(sourceItems).orElseThrow(Error.SOURCE_REQUIRED);
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(Error.CATEGORY_REQUIRED);
        var balance = Optional.ofNullable(id).map(service::getById).orElse(null);
        var params = BalanceSaveFlowParam.of(balance, sourceItems, categoryItems, config.getFilesPath());
        var flowResult = FlowUtil.runFlowSafely(FlowFactory.createBalanceSaveFlow(flowBuilder, params));
        return flowResult != null ? BalanceSaveDto.of(flowResult.getContext(), balance) : null;
    }
}
