package com.onurbcd.eruservice.service.validation;

import com.onurbcd.eruservice.dto.budget.BudgetDto;
import com.onurbcd.eruservice.dto.budget.BudgetSaveDto;
import com.onurbcd.eruservice.dto.budget.CopyBudgetDto;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.persistency.entity.Budget;
import com.onurbcd.eruservice.persistency.predicate.BudgetPredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BudgetRepository;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetValidationService {

    private final BudgetRepository repository;

    public void validate(BudgetSaveDto budgetSaveDto, @Nullable Budget budget, @Nullable UUID id) {
        Action.checkIf(id == null || budget != null).orElseThrowNotFound(id);
        var sequence = budget != null ? budget.getSequence() : null;

        Action.checkIf(id == null || NumberUtil.equals(budgetSaveDto.getSequence(), sequence))
                .orElseThrow(com.onurbcd.eruservice.enums.Error.SEQUENCE_CHANGED, sequence, budgetSaveDto.getSequence());

        Action.checkIf(id == null || equalRef(budgetSaveDto, budget)).orElseThrow(com.onurbcd.eruservice.enums.Error.REFERENCE_CHANGED);
    }

    public List<BudgetDto> validateCopy(CopyBudgetDto copyBudgetDto) {
        validateCopyBudget(copyBudgetDto);
        validateMonth(copyBudgetDto);
        return getFromBudget(copyBudgetDto);
    }

    private boolean equalRef(BudgetSaveDto budgetSaveDto, @Nullable Budget budget) {
        if (budget == null) {
            return false;
        }

        return DateUtil.equalMonth(budgetSaveDto.getRefYear(), budgetSaveDto.getRefMonth(), budget.getRefYear(),
                budget.getRefMonth());
    }

    private void validateCopyBudget(CopyBudgetDto copyBudgetDto) {
        Action.checkIfNotNull(copyBudgetDto.getFrom()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_FROM_IS_NULL);
        Action.checkIfNotNull(copyBudgetDto.getTo()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_TO_IS_NULL);
        Action.checkIfNotNull(copyBudgetDto.getFrom().getYear()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_FROM_YEAR_IS_NULL);
        Action.checkIfNotNull(copyBudgetDto.getFrom().getMonth()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_FROM_MONTH_IS_NULL);
        Action.checkIfNotNull(copyBudgetDto.getTo().getYear()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_TO_YEAR_IS_NULL);
        Action.checkIfNotNull(copyBudgetDto.getTo().getMonth()).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_TO_MONTH_IS_NULL);
    }

    private void validateMonth(CopyBudgetDto copyBudgetDto) {
        var equalMonth = DateUtil.equalMonth(copyBudgetDto.getFrom().getYear(), copyBudgetDto.getFrom().getMonth(),
                copyBudgetDto.getTo().getYear(), copyBudgetDto.getTo().getMonth());

        Action.checkIfNot(equalMonth).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_EQUAL_MONTH);
    }

    private List<BudgetDto> getFromBudget(CopyBudgetDto copyBudgetDto) {
        var fromBudget = repository.getAll(BudgetPredicateBuilder.ref(copyBudgetDto.getFrom()));

        Action.checkIfNotEmpty(fromBudget).orElseThrow(com.onurbcd.eruservice.enums.Error.COPY_BUDGET_FROM_IS_EMPTY,
                copyBudgetDto.getFrom().getMonth(), copyBudgetDto.getFrom().getYear());

        var toBudgetExists = repository.exists(BudgetPredicateBuilder.ref(copyBudgetDto.getTo()));

        Action.checkIfNot(toBudgetExists).orElseThrow(Error.COPY_BUDGET_TO_ALREADY_EXISTS,
                copyBudgetDto.getTo().getMonth(), copyBudgetDto.getTo().getYear());

        return fromBudget;
    }
}
