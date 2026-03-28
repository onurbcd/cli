package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.param.SequenceParam;

public interface SequenceService {

    Short getNextSequence(SequenceParam sequenceParam);

    void swapSequence(SequenceParam currentParam, Direction direction);

    void updateNextSequences(SequenceParam sequenceParam);

    void swapPosition(SequenceParam param);
}
