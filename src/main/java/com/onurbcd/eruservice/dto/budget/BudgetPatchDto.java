package com.onurbcd.eruservice.dto.budget;

import com.onurbcd.eruservice.dto.PrimePatchDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class BudgetPatchDto extends PrimePatchDto {

    private Boolean paid;

    public static BudgetPatchDto of(Boolean paid) {
        return BudgetPatchDto
                .builder()
                .paid(paid)
                .build();
    }

    public static BudgetPatchDto of(Boolean active, Boolean paid) {
        return BudgetPatchDto
                .builder()
                .active(active)
                .paid(paid)
                .build();
    }
}
