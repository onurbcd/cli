package com.onurbcd.eruservice.validation;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BalanceRepository;

@PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION)
public class BalanceSequenceValidationService extends AbstractSequenceValidationService {

    public BalanceSequenceValidationService(BalanceRepository repository) {
        super(repository);
    }
}
