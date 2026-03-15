package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.persistency.repository.BalanceRepository;
import com.onurbcd.cli.validator.SequenceValidator;

@PrimeService(Domain.BALANCE_SEQUENCE)
public class BalanceSequenceService extends AbstractSequenceService {

    public BalanceSequenceService(
            BalanceRepository repository,
            @PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION) SequenceValidator validationService) {

        super(repository, validationService);
    }
}
