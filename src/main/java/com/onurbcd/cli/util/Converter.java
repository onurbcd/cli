package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Converter {

    @Nullable
    public static UUID toUUID(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
