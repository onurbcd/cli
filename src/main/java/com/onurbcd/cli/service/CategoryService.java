package com.onurbcd.cli.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.transaction.annotation.Transactional;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.mapper.CategoryToEntityMapper;
import com.onurbcd.cli.persistency.entity.Category;
import com.onurbcd.cli.persistency.predicate.CategoryPredicateBuilder;
import com.onurbcd.cli.persistency.repository.CategoryRepository;
import com.onurbcd.cli.validator.CategoryValidator;
import com.querydsl.core.types.Predicate;

@PrimeService(Domain.CATEGORY)
public class CategoryService
        extends AbstractCrudService<Category, CategoryDto, CategoryPredicateBuilder, CategorySaveDto> {

    private final CategoryRepository repository;
    private final CategoryToEntityMapper toEntityMapper;
    private final CategoryValidator validationService;

    public CategoryService(CategoryRepository repository, CategoryToEntityMapper toEntityMapper,
            CategoryValidator validationService) {

        super(repository, toEntityMapper, QueryType.CUSTOM, CategoryPredicateBuilder.class);
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public String save(Dtoable dto, UUID id) {
        var categorySaveDto = (CategorySaveDto) dto;
        var current = id != null ? (CategoryDto) getById(id) : null;
        validationService.validate(categorySaveDto, current, id);
        var parent = id == null ? (CategoryDto) getById(categorySaveDto.getParentId()) : null;
        var category = fillValues(categorySaveDto, current, parent);
        category = repository.save(category);

        if (id == null && parent != null && Boolean.TRUE.equals(parent.getLastBranch())) {
            repository.updateLastBranch(Boolean.FALSE, parent.getId());
        }

        return category.getId().toString();
    }

    @Override
    public void delete(UUID id) {
        var category = (CategoryDto) getById(id);
        validationService.validateDelete(category);
        repository.deleteById(id);
        var childrenCount = repository.countChildren(category.getParentId());

        if (childrenCount == 0) {
            repository.updateLastBranch(Boolean.TRUE, category.getParentId());
        }
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return CategoryPredicateBuilder.all((CategoryFilter) filter);
    }

    public List<SelectItem> getCategoryItems(UUID id, boolean excludeAtar) {
        return repository
                .getCategoryItems(id, excludeAtar)
                .stream()
                .map(categoryItem -> SelectItem.of(categoryItem.getName(), categoryItem.getId().toString()))
                .toList();
    }

    private Category fillValues(CategorySaveDto categorySaveDto, @Nullable CategoryDto current,
            @Nullable CategoryDto parent) {

        var category = toEntityMapper.apply(categorySaveDto);
        category.setCreatedDate(current != null ? current.getCreatedDate() : null);
        category.setId(current != null ? current.getId() : null);
        category.setParent(new Category(current != null ? current.getParentId() : categorySaveDto.getParentId()));
        category.setLevel(
                current != null ? current.getLevel() : (short) (Objects.requireNonNull(parent).getLevel() + 1));
        category.setLastBranch(current != null ? current.getLastBranch() : Boolean.TRUE);
        return category;
    }
}
