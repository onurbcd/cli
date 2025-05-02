package com.onurbcd.eruservice.validator;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BudgetRepository;

@PrimeService(Domain.BUDGET_SEQUENCE_VALIDATION)
public class BudgetSequenceValidator extends AbstractSequenceValidator {

    public BudgetSequenceValidator(BudgetRepository repository) {
        super(repository);
    }
}
