package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.budget.*;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BudgetToEntityMapper;
import com.onurbcd.cli.model.SequenceParam;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.entity.Entityable;
import com.onurbcd.cli.persistency.predicate.BudgetPredicateBuilder;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import com.onurbcd.cli.util.CollectionUtil;
import com.onurbcd.cli.util.NumberUtil;
import com.onurbcd.cli.validator.Action;
import com.onurbcd.cli.validator.BudgetValidator;
import com.querydsl.core.types.Predicate;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.onurbcd.cli.util.DateUtil.orCurrentMonth;
import static com.onurbcd.cli.util.DateUtil.orCurrentYear;

@Service
public class BudgetService extends AbstractCrudService<Budget, BudgetDto, BudgetPredicateBuilder, BudgetSaveDto>
        implements Sequenceable {

    private final BudgetRepository repository;
    private final BudgetToEntityMapper toEntityMapper;
    private final BudgetValidator validationService;
    private final SequenceService sequenceService;
    private final SourceService sourceService;

    public BudgetService(BudgetRepository repository, BudgetToEntityMapper toEntityMapper,
                         BudgetValidator validationService,
                         @PrimeService(Domain.BUDGET_SEQUENCE) SequenceService sequenceService,
                         SourceService sourceService) {

        super(repository, toEntityMapper, QueryType.CUSTOM, BudgetPredicateBuilder.class);
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.validationService = validationService;
        this.sequenceService = sequenceService;
        this.sourceService = sourceService;
    }

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

    public void copy(CopyBudgetDto copyBudgetDto) {
        var fromBudget = validationService.validateCopy(copyBudgetDto);

        var toBudget = fromBudget
                .stream()
                .map(b -> toEntityMapper.copy(b, copyBudgetDto.getTo().getYear(), copyBudgetDto.getTo().getMonth()))
                .toList();

        repository.saveAll(toBudget);
    }

    @Transactional
    public void deleteAll(Short refYear, Short refMonth) {
        var count = repository.deleteAll(refYear, refMonth);
        Action.checkIf(count > 0).orElseThrow(Error.NO_ROWS_DELETED);
    }

    public BudgetValuesDto getBudgetValues(UUID id) {
        return repository
                .getBudgetValues(id)
                .orElseThrow(ApiException.notFound(id));
    }

    public List<SelectItem> getMonthlyBudget(@Nullable Short refYear, @Nullable Short refMonth) {
        return repository
                .getMonthlyBudget(orCurrentYear(refYear), orCurrentMonth(refMonth))
                .stream()
                .map(monthlyBudgetDto -> SelectItem.of(monthlyBudgetDto.compoundName(),
                        monthlyBudgetDto.id().toString()))
                .toList();
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
            updatedRowsCount = repository.updateActiveAndPaid(id, patchDto.isActive(), patchDto.getPaid(),
                    LocalDateTime.now());
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
    public void swapPosition(UUID id, Short targetSequence) {
        var budget = findByIdOrElseThrow(id);
        var sequenceParam = SequenceParam.of(budget, targetSequence);
        sequenceService.swapPosition(sequenceParam);
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
