package com.onurbcd.eruservice.validation;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BudgetRepository;
import com.onurbcd.eruservice.service.validation.AbstractSequenceValidationService;

@PrimeService(Domain.BUDGET_SEQUENCE_VALIDATION)
public class BudgetSequenceValidationService extends AbstractSequenceValidationService {

    public BudgetSequenceValidationService(BudgetRepository repository) {
        super(repository);
    }
}
