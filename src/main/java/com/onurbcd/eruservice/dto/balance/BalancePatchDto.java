package com.onurbcd.eruservice.dto.balance;

import com.onurbcd.eruservice.dto.PrimePatchDto;
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
