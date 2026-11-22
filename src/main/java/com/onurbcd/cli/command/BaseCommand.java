package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.factory.FlowFactory;
import com.onurbcd.cli.factory.SaveDtoFactory;
import com.onurbcd.cli.factory.SaveFlowParamFactory;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.CrudService;
import com.onurbcd.cli.util.FlowUtil;
import com.onurbcd.cli.util.ValidatorUtil;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;

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
    private final Validator validator;

    protected abstract SaveFlowParam preSaveFlow(CommandParam params);

    protected String baseSave(CommandParam params) {
        var saveDto = runSaveFlow(params);

        if (saveDto == null) {
            return shellHelper.warning(OPERATION_CANCELLED);
        }

        var violations = ValidatorUtil.validate(validator, saveDto);

        if (violations != null) {
            return shellHelper.error(violations);
        }

        var returnId = crudService.save(saveDto, params.getId());
        return shellHelper.success(SAVE_SUCCESS.formatted(name, returnId));
    }

    protected String baseDelete(UUID id) {
        crudService.delete(id);
        return shellHelper.success(DELETE_SUCCESS.formatted(name, id));
    }

    protected String baseGet(UUID id) {
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
    private PrimeSaveDto runSaveFlow(CommandParam commandParam) {
        var dto = (PrimeDto) Optional.ofNullable(commandParam.getId()).map(crudService::getById).orElse(null);
        commandParam.setDto(dto);
        var preParams = preSaveFlow(commandParam);
        var params = SaveFlowParamFactory.create(dto, preParams);
        var flow = FlowFactory.create(flowBuilder, params);
        var result = FlowUtil.runFlowSafely(flow);
        return result != null ? SaveDtoFactory.create(result.getContext(), dto, params.getType()) : null;
    }
}
