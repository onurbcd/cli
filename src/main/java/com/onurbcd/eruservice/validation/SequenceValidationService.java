package com.onurbcd.eruservice.validation;

import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.model.SequenceParam;

public interface SequenceValidationService {

    void validate(SequenceParam currentParam, SequenceParam targetParam, Direction direction);

    void validateSwapPosition(SequenceParam param);
}
