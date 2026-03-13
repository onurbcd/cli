package com.onurbcd.eruservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowParamUtil {

    public static <T> String getLocalDate(@Nullable T input, Function<T, LocalDate> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(DateUtil::formatDate)
                .orElse(ShellOption.NULL);
    }

    public static <T> String getString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(ShellOption.NULL);
    }

    @Nullable
    public static <T> String getNullString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    public static <T> String getBigDecimal(@Nullable T input, Function<T, BigDecimal> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(BigDecimal::toString)
                .orElse(ShellOption.NULL);
    }

    public static <T> String getEnum(@Nullable T input, Function<T, ? extends Enum<?>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(Enum::name)
                .orElse(ShellOption.NULL);
    }

    public static <T> List<String> getUUIDCollection(@Nullable T input, Function<T, Collection<UUID>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(ids -> ids.stream().map(UUID::toString).toList())
                .orElseGet(ArrayList::new);
    }

    public static <T> Boolean getBoolean(@Nullable T input, Function<T, Boolean> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(Boolean.TRUE);
    }
}
