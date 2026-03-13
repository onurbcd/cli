package com.onurbcd.eruservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    @Nullable
    public static String normalizeSpace(@Nullable String value) {
        return StringUtils.isNotBlank(value) && !Objects.equals(value, ShellOption.NULL)
                ? StringUtils.normalizeSpace(value)
                : null;
    }
}
