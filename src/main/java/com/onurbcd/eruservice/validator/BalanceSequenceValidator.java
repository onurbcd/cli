package com.onurbcd.eruservice.validator;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.persistency.repository.BalanceRepository;

@PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION)
public class BalanceSequenceValidator extends AbstractSequenceValidator {

    public BalanceSequenceValidator(BalanceRepository repository) {
        super(repository);
    }
}
