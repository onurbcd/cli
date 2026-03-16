package com.onurbcd.cli.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.factory.FlowFactory;
import com.onurbcd.cli.factory.SaveDtoFactory;
import com.onurbcd.cli.factory.SaveFlowParamFactory;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.model.SaveFlowParam;
import com.onurbcd.cli.service.CrudService;
import com.onurbcd.cli.util.FlowUtil;
import com.onurbcd.cli.util.ValidatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;

import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.*;

@RequiredArgsConstructor
public abstract class BaseCommand {

    private final CrudService crudService;
    private final ComponentFlow.Builder flowBuilder;
    protected final ShellHelper shellHelper;
    private final String name;
    private final EruTable table;

    protected abstract SaveFlowParam preSaveFlow(@Nullable UUID id);

    protected String baseSave(@Nullable UUID id) {
        var saveDto = runSaveFlow(id);

        if (saveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        String violations;

        if ((violations = ValidatorUtil.validate(saveDto)) != null) {
            return shellHelper.error(violations);
        }

        var returnId = crudService.save(saveDto, id);
        return shellHelper.success(SAVE_SUCCESS.formatted(name, returnId));
    }

    protected String baseDelete(UUID id) {
        crudService.delete(id);
        return shellHelper.success(DELETE_SUCCESS.formatted(name, id));
    }

    protected String baseGet(UUID id) throws JsonProcessingException {
        var dto = crudService.getById(id);
        return shellHelper.printJson(dto);
    }

    protected String baseGetAll(Filterable filter, Integer pageNumber, Integer pageSize, Sort.Direction direction,
                                String... properties) {

        var pageable = PageRequest.of(pageNumber - 1, pageSize, direction, properties);
        var page = crudService.getAll(pageable, filter);
        return shellHelper.printTable(page, table);
    }

    protected String baseUpdate(Dtoable dto, UUID id) {
        crudService.update(dto, id);
        return shellHelper.success(UPDATE_SUCCESS.formatted(name, id));
    }

    @Nullable
    private PrimeSaveDto runSaveFlow(@Nullable UUID id) {
        var preParams = preSaveFlow(id);
        var dto = (PrimeDto) Optional.ofNullable(id).map(crudService::getById).orElse(null);
        var params = SaveFlowParamFactory.create(dto, preParams);
        var flow = FlowFactory.create(flowBuilder, params);
        var result = FlowUtil.runFlowSafely(flow);
        return result != null ? SaveDtoFactory.create(result.getContext(), dto, params.getType()) : null;
    }
}
