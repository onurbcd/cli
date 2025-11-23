package com.onurbcd.eruservice.util;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.exception.ApiException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Converter {

    public static UUID toUUID(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ApiException(Error.INVALID_UUID, e);
        }
    }
}
