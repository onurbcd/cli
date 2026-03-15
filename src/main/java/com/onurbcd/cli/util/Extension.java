package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Extension {

    public static String normalizeSpace(String in) {
        return StringUtils.normalizeSpace(in);
    }

    public static String defaultToNull(@Nullable String in) {
        return StringUtils.isBlank(in) || Objects.equals(in, ShellOption.NULL) ? null : StringUtils.normalizeSpace(in);
    }

    public static Short orIfNullCurrentYear(@Nullable Short year) {
        return year == null ? (short) LocalDate.now().getYear() : year;
    }

    public static Short orIfNullCurrentMonth(@Nullable Short month) {
        return month == null ? (short) LocalDate.now().getMonthValue() : month;
    }
}
