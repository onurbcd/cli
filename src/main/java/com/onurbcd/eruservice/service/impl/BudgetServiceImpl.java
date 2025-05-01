package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.budget.*;
import com.onurbcd.eruservice.dto.filter.BudgetFilter;
import com.onurbcd.eruservice.dto.filter.Filterable;
import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.entity.Budget;
import com.onurbcd.eruservice.persistency.entity.Entityable;
import com.onurbcd.eruservice.model.SequenceParam;
import com.onurbcd.eruservice.persistency.predicate.BudgetPredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BudgetRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.service.BudgetService;
import com.onurbcd.eruservice.service.SequenceService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.exception.ApiException;
import com.onurbcd.eruservice.mapper.BudgetToEntityMapper;
import com.onurbcd.eruservice.validation.Action;
import com.onurbcd.eruservice.validation.BudgetValidationService;
import com.onurbcd.eruservice.util.CollectionUtil;
import com.onurbcd.eruservice.util.NumberUtil;
import com.querydsl.core.types.Predicate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class BudgetServiceImpl extends AbstractCrudService<Budget, BudgetDto, BudgetPredicateBuilder, BudgetSaveDto>
        implements BudgetService {

    private final BudgetRepository repository;

    private final BudgetToEntityMapper toEntityMapper;

    private final BudgetValidationService validationService;

    private final SequenceService sequenceService;

    private final SourceService sourceService;

    public BudgetServiceImpl(BudgetRepository repository, BudgetToEntityMapper toEntityMapper,
                             BudgetValidationService validationService,
                             @PrimeService(Domain.BUDGET_SEQUENCE) SequenceService sequenceService,
                             SourceService sourceService) {

        super(repository, toEntityMapper, QueryType.CUSTOM, BudgetPredicateBuilder.class);
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.validationService = validationService;
        this.sequenceService = sequenceService;
        this.sourceService = sourceService;
    }

    @Override
    public void validate(Dtoable dto, @Nullable Entityable entity, @Nullable UUID id) {
        validationService.validate((BudgetSaveDto) dto, (Budget) entity, id);
    }

    @Override
    public Entityable fillValues(Dtoable dto, Entityable entity) {
        var budget = (Budget) super.fillValues(dto, entity);
        budget.setSequence(getSequence((Budget) entity, budget));
        return budget;
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return BudgetPredicateBuilder.all((BudgetFilter) filter);
    }

    @Override
    public void update(Dtoable dto, UUID id) {
        var patchDto = (BudgetPatchDto) dto;
        var updatedRowsCount = 0;

        if (patchDto.isActive() != null && patchDto.getPaid() != null) {
            updatedRowsCount = repository.updateActiveAndPaid(id, patchDto.isActive(), patchDto.getPaid(), LocalDateTime.now());
        } else if (patchDto.isActive() != null) {
            updatedRowsCount = repository.updateActive(id, patchDto.isActive());
        } else if (patchDto.getPaid() != null) {
            updatedRowsCount = repository.updatePaid(id, patchDto.getPaid(), LocalDateTime.now());
        }

        Action.checkIf(updatedRowsCount == 1).orElseThrowNotFound(id);
    }

    @Override
    public void updateSequence(UUID id, Direction direction) {
        var budget = findByIdOrElseThrow(id);
        var sequenceParam = SequenceParam.of(budget, null);
        sequenceService.swapSequence(sequenceParam, direction);
    }

    @Override
    public void delete(UUID id) {
        var budget = findByIdOrElseThrow(id);
        repository.deleteById(budget.getId());
        var sequenceParam = SequenceParam.of(budget, null);
        sequenceService.updateNextSequences(sequenceParam);
    }

    @Override
    public List<SumDto> getSumByMonth(BudgetFilter filter) {
        Action.checkIfNotNull(filter.getRefYear()).orElseThrow(Error.BUDGET_REF_YEAR_IS_NULL);
        Action.checkIfNotNull(filter.getRefMonth()).orElseThrow(Error.BUDGET_REF_MONTH_IS_NULL);
        var sumSet = repository.getSumByMonth(filter.getRefYear(), filter.getRefMonth());
        var paidSum = CollectionUtil.getValue(sumSet, BudgetSumDto::getPaid, BudgetSumDto::getAmount);
        var unpaidSum = CollectionUtil.getValue(sumSet, p -> !p.getPaid(), BudgetSumDto::getAmount);
        var totalSum = NumberUtil.add(paidSum, unpaidSum);
        var usableBalanceSum = sourceService.getUsableBalanceSum();
        var balance = NumberUtil.subtract(usableBalanceSum, unpaidSum);
        return List.of(SumDto.total(totalSum), SumDto.paid(paidSum), SumDto.unpaid(unpaidSum), SumDto.balance(balance));
    }

    @Override
    public void copy(CopyBudgetDto copyBudgetDto) {
        var fromBudget = validationService.validateCopy(copyBudgetDto);

        var toBudget = fromBudget
                .stream()
                .map(b -> toEntityMapper.copy(b, copyBudgetDto.getTo().getYear(), copyBudgetDto.getTo().getMonth()))
                .toList();

        repository.saveAll(toBudget);
    }

    @Override
    public void swapPosition(UUID id, Short targetSequence) {
        var budget = findByIdOrElseThrow(id);
        var sequenceParam = SequenceParam.of(budget, targetSequence);
        sequenceService.swapPosition(sequenceParam);
    }

    @Override
    public void deleteAll(Short refYear, Short refMonth) {
        var count = repository.deleteAll(refYear, refMonth);
        Action.checkIf(count > 0).orElseThrow(Error.NO_ROWS_DELETED);
    }

    @Override
    public BudgetValuesDto getBudgetValues(UUID id) {
        return repository
                .getBudgetValues(id)
                .orElseThrow(ApiException.notFound(id));
    }

    private Short getSequence(Budget current, Budget next) {
        return Optional
                .ofNullable(current)
                .map(Budget::getSequence)
                .orElseGet(getNextSequence(next));
    }

    private Supplier<Short> getNextSequence(Budget budget) {
        var sequenceParam = SequenceParam.of(budget);
        return () -> sequenceService.getNextSequence(sequenceParam);
    }
}
