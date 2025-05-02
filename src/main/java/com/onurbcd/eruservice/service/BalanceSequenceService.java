package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BalanceRepository;
import com.onurbcd.eruservice.validation.SequenceValidationService;

@PrimeService(Domain.BALANCE_SEQUENCE)
public class BalanceSequenceService extends AbstractSequenceService {

    public BalanceSequenceService(
            BalanceRepository repository,
            @PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION) SequenceValidationService validationService) {

        super(repository, validationService);
    }
}
