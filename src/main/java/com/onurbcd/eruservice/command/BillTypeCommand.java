package com.onurbcd.eruservice.command;

import static com.onurbcd.eruservice.util.Constant.OPERATION_CANCELLED;

import java.util.Optional;
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
import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import com.onurbcd.eruservice.dto.billtype.BillTypePatchDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeSaveDto;
import com.onurbcd.eruservice.dto.filter.BillTypeFilter;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.factory.FlowFactory;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.BillTypeSaveFlowParam;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.service.CategoryService;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.ValidatorUtil;
import com.onurbcd.eruservice.validator.Action;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@ShellComponent
@ShellCommandGroup("Bill Type")
@RequiredArgsConstructor
public class BillTypeCommand {

    private final BillTypeService service;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final CategoryService categoryService;

    @ShellMethod(key = "bill-type-save", value = "Create or update a bill type.")
    public String save(@ShellOption(value = { "id",
            "-i" }, help = "The bill type's id.", defaultValue = ShellOption.NULL) UUID id) {

        var billTypeSaveDto = runSaveFlow(id);

        if (billTypeSaveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(billTypeSaveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = service.save(billTypeSaveDto, id);
        return shellHelper.success("Bill Type with id: '%s' saved with success.".formatted(returnId));
    }

    @ShellMethod(key = "bill-type-delete", value = "Delete bill type by id.")
    public String delete(@ShellOption(value = { "id", "-i" }, help = "The bill type's id.") @NotNull UUID id) {
        service.delete(id);
        return shellHelper.success("Bill Type with id: '%s' deleted with success.".formatted(id));
    }

    @ShellMethod(key = "bill-type-get", value = "Get bill type by id.")
    public String get(@ShellOption(value = { "id", "-i" }, help = "The bill type's id.") @NotNull UUID id)
            throws JsonProcessingException {

        return shellHelper.printJson(service.getById(id));
    }

    @ShellMethod(key = "bill-type-get-all", value = "Get bill type's list.")
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
                    "-f" }, help = "Filter's search option.", defaultValue = ShellOption.NULL) String search) {

        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        BillTypeFilter.of(active, search)),
                EruTable.BILL_TYPE);
    }

    @ShellMethod(key = "bill-type-update", value = "Update bill type's status by id.")
    public String update(@ShellOption(value = { "id", "-i" }, help = "The bill type's id.") @NotNull UUID id,

            @ShellOption(value = { "active",
                    "-a" }, help = "The bill type's status.", defaultValue = "false") Boolean active) {

        service.update(BillTypePatchDto.of(active), id);
        return shellHelper.success("Bill Type with id: '%s' updated with success.".formatted(id));
    }

    @Nullable
    private BillTypeSaveDto runSaveFlow(@Nullable UUID id) {
        var categoryItems = categoryService.getCategoryItems(null, true);
        Action.checkIfNotEmpty(categoryItems).orElseThrow(Error.CATEGORY_REQUIRED);
        var billType = Optional.ofNullable(id).map(i -> (BillTypeDto) service.getById(i)).orElse(null);
        var params = BillTypeSaveFlowParam.of(billType, categoryItems);
        var flowResult = FlowUtil.runFlowSafely(FlowFactory.createBillTypeSaveFlow(flowBuilder, params));
        return flowResult != null ? BillTypeSaveDto.of(flowResult.getContext(), billType) : null;
    }
}
