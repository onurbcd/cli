package com.onurbcd.cli.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.factory.PredicateBuilderFactory;
import com.onurbcd.cli.mapper.EntityMapper;
import com.onurbcd.cli.persistency.entity.Entityable;
import com.onurbcd.cli.persistency.predicate.BasePredicateBuilder;
import com.onurbcd.cli.persistency.repository.EruRepository;
import com.onurbcd.cli.validator.Action;
import com.querydsl.core.types.Predicate;

public abstract class AbstractCrudService<E extends Entityable, D extends Dtoable, P extends BasePredicateBuilder, S extends Dtoable>
        implements CrudService {

    private final EruRepository<E, D> repository;
    private Function<E, D> toDtoMapper;
    private final EntityMapper<S, E> toEntityMapper;
    private final QueryType queryType;
    private final Class<P> predicateClass;

    protected AbstractCrudService(EruRepository<E, D> repository, Function<E, D> toDtoMapper,
            EntityMapper<S, E> toEntityMapper, QueryType queryType, Class<P> predicateClass) {

        this.repository = repository;
        this.toDtoMapper = toDtoMapper;
        this.toEntityMapper = toEntityMapper;
        this.queryType = queryType;
        this.predicateClass = predicateClass;
    }

    protected AbstractCrudService(EruRepository<E, D> repository, EntityMapper<S, E> toEntityMapper,
            QueryType queryType, Class<P> predicateClass) {

        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.queryType = queryType;
        this.predicateClass = predicateClass;
    }

    @Override
    public String save(Dtoable dto, @Nullable UUID id) {
        var currentEntity = id != null ? repository.findById(id).orElse(null) : null;
        validate(dto, currentEntity, id);
        @SuppressWarnings("unchecked")
        var newEntity = (E) fillValues(dto, currentEntity);
        newEntity = repository.save(newEntity);
        return newEntity.getId().toString();
    }

    @Override
    public void validate(Dtoable dto, Entityable entity, UUID id) {
        Action.checkIf(id == null || entity != null).orElseThrowNotFound(id);
    }

    @Override
    public Entityable fillValues(Dtoable dto, Entityable entity) {
        @SuppressWarnings("unchecked")
        var newEntity = toEntityMapper.apply((S) dto);
        newEntity.setId(entity != null ? entity.getId() : null);
        newEntity.setCreatedDate(entity != null ? entity.getCreatedDate() : null);
        return newEntity;
    }

    @Override
    public Dtoable getById(UUID id) {
        if (QueryType.JPA.equals(queryType)) {
            var entity = findByIdOrElseThrow(id);
            return toDtoMapper.apply(entity);
        }

        var dto = repository.getSingle(id);
        Action.checkIfNotNull(dto).orElseThrowNotFound(id);
        return dto;
    }

    @Override
    public void delete(UUID id) {
        var deletedRowsCount = repository.deleteUsingId(id);
        Action.checkIf(deletedRowsCount == 1).orElseThrowNotFound(id);
    }

    @Override
    public void update(Dtoable dto, UUID id) {
        var updatedRowsCount = repository.updateActive(id, dto.isActive());
        Action.checkIf(updatedRowsCount == 1).orElseThrowNotFound(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Dtoable> getAll(Pageable pageable, Filterable filter) {
        var predicate = getPredicate(filter);

        return QueryType.JPA.equals(queryType)
                ? repository.findAll(predicate, pageable).map(toDtoMapper)
                : (Page<Dtoable>) repository.getAll(predicate, pageable);
    }

    @Override
    public List<SelectItem> getItems(UUID id) {
        return repository
                .getItems(id)
                .stream()
                .map(categoryItem -> SelectItem.of(categoryItem.getName(), categoryItem.getId().toString()))
                .toList();
    }

    protected E findByIdOrElseThrow(UUID id) {
        var entity = repository.findById(id).orElse(null);
        Action.checkIfNotNull(entity).orElseThrowNotFound(id);
        return entity;
    }

    protected Predicate getPredicate(Filterable filter) {
        var predicate = PredicateBuilderFactory.init(predicateClass);
        return predicate.search(filter.getSearch()).active(filter.isActive()).build();
    }

    protected E getOrElseThrow(UUID id) {
        var entity = repository.get(id).orElse(null);
        Action.checkIfNotNull(entity).orElseThrowNotFound(id);
        return entity;
    }
}
