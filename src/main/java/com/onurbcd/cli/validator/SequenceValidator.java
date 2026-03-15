package com.onurbcd.cli.validator;

import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.model.SequenceParam;

public interface SequenceValidator {

    void validate(SequenceParam currentParam, SequenceParam targetParam, Direction direction);

    void validateSwapPosition(SequenceParam param);
}
