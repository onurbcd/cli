package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.enums.Direction;
import com.onurbcd.eruservice.persistency.param.SequenceParam;

public interface SequenceService {

    Short getNextSequence(SequenceParam sequenceParam);

    void swapSequence(SequenceParam currentParam, Direction direction);

    void updateNextSequences(SequenceParam sequenceParam);

    void swapPosition(SequenceParam param);
}
