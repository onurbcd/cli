package com.onurbcd.cli.persistency.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Entityable {

    void setActive(Boolean active);

    void setId(UUID id);

    UUID getId();

    void setCreatedDate(LocalDateTime createdDate);

    LocalDateTime getCreatedDate();
}
