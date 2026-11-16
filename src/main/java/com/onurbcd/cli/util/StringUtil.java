package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    @Nullable
    public static String normalizeSpace(@Nullable String value) {
        return StringUtils.isNotBlank(value)
                ? StringUtils.normalizeSpace(value)
                : null;
    }
}
