package com.onurbcd.cli.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PrimeDto extends AuditDto implements Dtoable {

    private UUID id;
    private String name;
    private Boolean active;

    @Override
    public Boolean isActive() {
        return active;
    }
}
