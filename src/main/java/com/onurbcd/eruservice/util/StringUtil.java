package com.onurbcd.eruservice.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static String normalizeSpace(@Nullable String value) {
        return StringUtils.isNotBlank(value) ? StringUtils.normalizeSpace(value) : null;
    }
}
