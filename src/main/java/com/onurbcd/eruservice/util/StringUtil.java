package com.onurbcd.eruservice.util;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static String normalizeSpace(@Nullable String value) {
        return StringUtils.isNotBlank(value) && !Objects.equals(value, ShellOption.NULL)
                ? StringUtils.normalizeSpace(value)
                : null;
    }
}
