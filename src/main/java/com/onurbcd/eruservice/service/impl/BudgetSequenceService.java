package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BudgetRepository;
import com.onurbcd.eruservice.service.AbstractSequenceService;
import com.onurbcd.eruservice.validation.SequenceValidationService;

@PrimeService(Domain.BUDGET_SEQUENCE)
public class BudgetSequenceService extends AbstractSequenceService {

    public BudgetSequenceService(
            BudgetRepository repository,
            @PrimeService(Domain.BUDGET_SEQUENCE_VALIDATION) SequenceValidationService validationService) {

        super(repository, validationService);
    }
}
