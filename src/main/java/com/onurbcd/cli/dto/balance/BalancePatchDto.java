package com.onurbcd.cli.dto.balance;

import com.onurbcd.cli.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BalancePatchDto extends PrimePatchDto {

    public static BalancePatchDto of(Boolean active) {
        return BalancePatchDto
                .builder()
                .active(active)
                .build();
    }
}
