package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.dto.filter.Filterable;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.service.CrudService;
import com.onurbcd.eruservice.util.ValidatorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;

import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.*;

@RequiredArgsConstructor
public abstract class BaseCommand {

    private final CrudService crudService;
    protected final ComponentFlow.Builder flowBuilder;
    protected final ShellHelper shellHelper;
    private final String name;
    private final EruTable table;

    @Nullable
    protected abstract PrimeSaveDto runSaveFlow(@Nullable UUID id);

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

    protected String baseGetAll(Integer pageNumber, Integer pageSize, Sort.Direction direction, String property,
                                Filterable filter) {

        var pageable = PageRequest.of(pageNumber - 1, pageSize, direction, property);
        var page = crudService.getAll(pageable, filter);
        return shellHelper.printTable(page, table);
    }

    protected String baseUpdate(Dtoable dto, UUID id) {
        crudService.update(dto, id);
        return shellHelper.success(UPDATE_SUCCESS.formatted(name, id));
    }
}
