package com.onurbcd.eruservice.dto.secret;

import com.onurbcd.eruservice.dto.PrimePatchDto;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SecretPatchDto extends PrimePatchDto {

    public static SecretPatchDto of(Boolean active) {
        return SecretPatchDto.builder()
                .active(active)
                .build();
    }
}
