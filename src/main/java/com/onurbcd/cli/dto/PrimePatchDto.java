package com.onurbcd.cli.dto;

import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
public abstract class PrimePatchDto implements Dtoable {

    private Boolean active;

    @Override
    public Boolean isActive() {
        return active;
    }
}
