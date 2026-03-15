package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.persistency.repository.BalanceRepository;

@PrimeService(Domain.BALANCE_SEQUENCE_VALIDATION)
public class BalanceSequenceValidator extends AbstractSequenceValidator {

    public BalanceSequenceValidator(BalanceRepository repository) {
        super(repository);
    }
}
