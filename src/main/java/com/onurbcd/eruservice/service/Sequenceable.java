package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.enums.Direction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface Sequenceable {

    @Transactional
    void updateSequence(UUID id, Direction direction);

    @Transactional
    void swapPosition(UUID id, Short targetSequence);
}
