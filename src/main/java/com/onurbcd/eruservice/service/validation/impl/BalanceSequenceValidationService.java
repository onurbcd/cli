package com.onurbcd.eruservice.service.validation.impl;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BalanceRepository;
import com.onurbcd.eruservice.service.validation.AbstractSequenceValidationService;

@PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION)
public class BalanceSequenceValidationService extends AbstractSequenceValidationService {

    public BalanceSequenceValidationService(BalanceRepository repository) {
        super(repository);
    }
}
