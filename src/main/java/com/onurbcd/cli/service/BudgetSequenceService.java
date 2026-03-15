package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.persistency.repository.BudgetRepository;
import com.onurbcd.cli.validator.SequenceValidator;

@PrimeService(Domain.BUDGET_SEQUENCE)
public class BudgetSequenceService extends AbstractSequenceService {

    public BudgetSequenceService(
            BudgetRepository repository,
            @PrimeService(Domain.BUDGET_SEQUENCE_VALIDATION) SequenceValidator validationService) {

        super(repository, validationService);
    }
}
