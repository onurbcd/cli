package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.param.SequenceParam;
import org.springframework.data.repository.query.Param;

public interface SequenceRepository {

    Short getMaxSequence(@Param("sequenceParam") SequenceParam sequenceParam);

    Boolean existsSequence(@Param("sequenceParam") SequenceParam sequenceParam);

    void updateSequence(@Param("sequenceParam") SequenceParam sequenceParam);

    long countNextSequences(@Param("sequenceParam") SequenceParam sequenceParam);
}
