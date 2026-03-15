package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.Direction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface Sequenceable {

    @Transactional
    void updateSequence(UUID id, Direction direction);

    @Transactional
    void swapPosition(UUID id, Short targetSequence);
}
