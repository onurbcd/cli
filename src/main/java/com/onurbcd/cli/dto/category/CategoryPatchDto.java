package com.onurbcd.cli.dto.category;

import com.onurbcd.cli.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CategoryPatchDto extends PrimePatchDto {

    public static CategoryPatchDto of(Boolean active) {
        return CategoryPatchDto
                .builder()
                .active(active)
                .build();
    }
}
