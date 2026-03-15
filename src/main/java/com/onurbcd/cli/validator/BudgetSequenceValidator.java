package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.persistency.repository.BudgetRepository;

@PrimeService(Domain.BUDGET_SEQUENCE_VALIDATION)
public class BudgetSequenceValidator extends AbstractSequenceValidator {

    public BudgetSequenceValidator(BudgetRepository repository) {
        super(repository);
    }
}
