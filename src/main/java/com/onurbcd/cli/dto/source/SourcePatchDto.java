package com.onurbcd.cli.dto.source;

import com.onurbcd.cli.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SourcePatchDto extends PrimePatchDto {

    public static SourcePatchDto of(Boolean active) {
        return SourcePatchDto
                .builder()
                .active(active)
                .build();
    }
}
