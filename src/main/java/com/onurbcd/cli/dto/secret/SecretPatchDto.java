package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SecretPatchDto extends PrimePatchDto {

    public static SecretPatchDto of(Boolean active) {
        return SecretPatchDto.builder()
                .active(active)
                .build();
    }
}
