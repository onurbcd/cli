package com.onurbcd.eruservice.validator;

import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.model.SequenceParam;

public interface SequenceValidator {

    void validate(SequenceParam currentParam, SequenceParam targetParam, Direction direction);

    void validateSwapPosition(SequenceParam param);
}
