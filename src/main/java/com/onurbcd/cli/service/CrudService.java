package com.onurbcd.cli.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.transaction.annotation.Transactional;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.persistency.entity.Entityable;

public interface CrudService {

    String save(Dtoable dto, @Nullable UUID id);

    void validate(Dtoable dto, @Nullable Entityable entity, @Nullable UUID id);

    Entityable fillValues(Dtoable dto, Entityable entity);

    @Transactional
    void delete(UUID id);

    Dtoable getById(UUID id);

    Page<Dtoable> getAll(Pageable pageable, Filterable filter);

    @Transactional
    void update(Dtoable dto, UUID id);

    List<SelectItem> getItems(@Nullable UUID id);
}
