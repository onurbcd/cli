package com.onurbcd.cli.dto.incomesource;

import com.onurbcd.cli.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IncomeSourcePatchDto extends PrimePatchDto {

    public static IncomeSourcePatchDto of(Boolean active) {
        return IncomeSourcePatchDto
                .builder()
                .active(active)
                .build();
    }
}
